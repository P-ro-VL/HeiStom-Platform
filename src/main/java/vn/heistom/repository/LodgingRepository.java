package vn.heistom.repository;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.heistom.api.ApiCallException;
import vn.heistom.datasource.*;
import vn.heistom.dto.request.CreateBookingRequest;
import vn.heistom.dto.request.CreateLodgingRequest;
import vn.heistom.dto.request.SearchLodgingRequest;
import vn.heistom.dto.response.LodgingResponse;
import vn.heistom.dto.response.UserResponse;
import vn.heistom.model.*;
import vn.heistom.util.GeocodingUtil;
import vn.heistom.util.LatLng;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LodgingRepository {

    final LodgingDataSource lodgingDataSource;
    final LodgingImageDataSource lodgingImageDataSource;
    final LodgingAmenityDataSource lodgingAmenityDataSource;
    private final UserDataSource userDataSource;
    private final RoomDataSource roomDataSource;

    public LodgingResponse getLodging(UUID lodgingId) throws ApiCallException {
        Optional<LodgingModel> lodgingOpt = lodgingDataSource.findById(lodgingId);
        if(lodgingOpt.isEmpty()) throw new ApiCallException("Cannot find any lodging with id " + lodgingId, HttpStatus.NOT_FOUND);

        LodgingModel lodgingModel = lodgingOpt.get();

        Optional<List<LodgingImageModel>> imagesOpt = lodgingImageDataSource.getByLodgingId(lodgingId);
        Optional<List<LodgingAmenityModel>> amenitiesOpt = lodgingAmenityDataSource.getByLodgingId(lodgingId);

        if(imagesOpt.isEmpty()) throw new ApiCallException("Cannot find any images with lodging id " + lodgingId, HttpStatus.NOT_FOUND);
        if(amenitiesOpt.isEmpty()) throw new ApiCallException("Cannot find any amenities with lodging id " + lodgingId, HttpStatus.NOT_FOUND);

        List<LodgingImageModel> imageModels = imagesOpt.get();
        List<LodgingAmenityModel> amenitiesModels = amenitiesOpt.get();

        Optional<UserModel> ownerOpt = userDataSource.findByUuid(lodgingModel.getOwnerId());
        if(ownerOpt.isEmpty()) throw new ApiCallException("Cannot find any user with id " + lodgingModel.getOwnerId(), HttpStatus.NOT_FOUND);
        UserModel owner = ownerOpt.get();

        return LodgingResponse.builder()
                .id(lodgingModel.getId())
                .name(lodgingModel.getName())
                .address(lodgingModel.getAddress())
                .pricePerDay(lodgingModel.getDayPrice())
                .pricePerHour(lodgingModel.getHourPrice())
                .area(lodgingModel.getArea())
                .description(lodgingModel.getDescription())
                .uploadDate(lodgingModel.getUploadDate())
                .lat(lodgingModel.getLat())
                .lng(lodgingModel.getLng())
                .images(imageModels.stream().map(LodgingImageModel::getUrl).collect(Collectors.toList()))
                .amenities(amenitiesModels.stream().map(LodgingAmenityModel::getAmentity).collect(Collectors.toList()))
                .owner(
                        UserResponse.builder()
                                .uuid(owner.getUuid())
                                .name(owner.getName())
                                .address(owner.getAddress())
                                .email(owner.getEmail())
                                .avatar(owner.getAvatar())
                                .phoneNumber(owner.getPhoneNumber())
                                .build()
                )
                .build();
    }

    public LodgingResponse createLodging(CreateLodgingRequest request) throws ApiCallException {

        LatLng latLng = GeocodingUtil.getLatLngFromAddress(request.getAddress());

        if(latLng == null) throw new ApiCallException("Cannot parse address to lat and lng", HttpStatus.BAD_REQUEST);

        LodgingModel lodgingModel = LodgingModel.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .address(request.getAddress())
                .hourPrice(request.getPricePerHour())
                .dayPrice(request.getPricePerDay())
                .area(request.getArea())
                .description(request.getDescription())
                .uploadDate(System.currentTimeMillis())
                .lat(latLng.getLatitude())
                .lng(latLng.getLongitude())
                .ownerId(request.getOwnerId())
                .build();
        lodgingDataSource.save(lodgingModel);

        List<LodgingImageModel> lodgingImageModels = request.getImages().stream().map((e) -> {
            return LodgingImageModel.builder()
                    .id(UUID.randomUUID())
                    .lodgingId(lodgingModel.getId())
                    .url(e)
                    .build();
        }).toList();
        lodgingImageDataSource.saveAll(lodgingImageModels);

        List<LodgingAmenityModel> lodgingAmenityModels = request.getAmenities().stream().map((e) -> {
            return LodgingAmenityModel.builder()
                    .id(UUID.randomUUID())
                    .lodgingId(lodgingModel.getId())
                    .amentity(e)
                    .build();
        }).toList();
        lodgingAmenityDataSource.saveAll(lodgingAmenityModels);

        return getLodging(lodgingModel.getId());
    }

    public List<LodgingResponse> search(SearchLodgingRequest request) {
        List<LodgingModel> allLodgings = lodgingDataSource.findAll();

        List<Predicate<LodgingModel>> predicates = new ArrayList<>();

        if(request.getAddress() != null) {
            predicates.add(lodgingModel -> lodgingModel.getAddress().toLowerCase().contains(
                    request.getAddress().toLowerCase()
            ));
        }

        if(request.getCheckIn() != 0L) {
            predicates.add(lodgingModel -> {
               List<RoomModel> rooms = roomDataSource.findAllByLodgingId(lodgingModel.getId());
               return rooms.stream().anyMatch(
                       room -> room.getCheckOutAt() <= request.getCheckIn() || room.getCheckInAt() == 0L
               );
            });
        }

        if(request.getCheckOut() != 0L) {
            predicates.add(lodgingModel -> {
                List<RoomModel> rooms = roomDataSource.findAllByLodgingId(lodgingModel.getId());
                return rooms.stream().anyMatch(
                        room -> room.getCheckInAt() >= request.getCheckOut() || room.getCheckInAt() == 0L

                );
            });
        }

        if(request.getNumOfPeople() != 0) {
            predicates.add(lodgingModel -> {
                List<RoomModel> rooms = roomDataSource.findAllByLodgingId(lodgingModel.getId());
                if(rooms.size() == 1) {
                    return rooms.get(0).getCapacity() >= request.getNumOfPeople();
                }
                return rooms.stream()
                        .filter(room -> room.getStatus().equals("AVAILABLE"))
                        .map(RoomModel::getCapacity)
                        .reduce(Integer::sum)
                        .orElse(0) >= request.getNumOfPeople();
            });
        }

        if(request.getNumOfRoom() != 0) {
            predicates.add(lodgingModel -> {
                List<RoomModel> rooms = roomDataSource.findAllByLodgingId(lodgingModel.getId());
                return rooms.stream()
                        .filter(room -> room.getStatus().equals("AVAILABLE"))
                        .count() >= request.getNumOfRoom();
            });
        }

        return allLodgings.stream()
                .filter(lodgingModel -> {
                    AtomicInteger count = new AtomicInteger();
                    predicates.forEach(predicate -> {
                        if(predicate.test(lodgingModel))
                            count.addAndGet(1);
                    });
                    return count.get() == predicates.size();
                })
                .map(lodgingModel -> {
                    try {
                        return getLodging(lodgingModel.getId());
                    } catch (ApiCallException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
    }

    public List<RoomModel> book(CreateBookingRequest request) throws ApiCallException {
        Optional<LodgingModel> lodgingModelOpt = lodgingDataSource.findById(request.getLodgingId());
        if(lodgingModelOpt.isEmpty()) {
            throw new ApiCallException("Cannot find any lodging with id '" + request.getLodgingId() + "'", HttpStatus.NOT_FOUND);
        }

        LodgingModel lodgingModel = lodgingModelOpt.get();
        List<RoomModel> rooms = roomDataSource.findAllByLodgingId(lodgingModel.getId());

        if(request.getCriteria().getCheckIn() != 0L) {
            rooms = rooms.stream()
                    .filter(room -> room.getCheckOutAt() <= request.getCriteria().getCheckIn() || room.getCheckInAt() == 0)
                    .toList();
        }

        if(request.getCriteria().getCheckOut() != 0L) {
            rooms = rooms.stream()
                    .filter(
                        room -> room.getCheckInAt() >= request.getCriteria().getCheckOut() || room.getCheckInAt() == 0L
                    ).toList();
        }

        rooms = rooms.stream().sorted(Comparator.comparingInt(RoomModel::getCapacity)).toList();

        List<RoomModel> result = new ArrayList<>();
        int total = 0;
        for(RoomModel roomModel : rooms) {
            if(total < request.getCriteria().getNumOfPeople()) {
                total += roomModel.getCapacity();
                result.add(roomModel);
            }
        }

        return result;
    }
}
