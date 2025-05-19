package vn.heistom.repository;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.heistom.api.ApiCallException;
import vn.heistom.datasource.*;
import vn.heistom.dto.request.*;
import vn.heistom.dto.response.BookingDetailResponse;
import vn.heistom.dto.response.BookingLodgingResponse;
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

    final RoomRepository roomRepository;
    private final BookingDataSource bookingDataSource;

    final RatingRepository ratingRepository;

    public LodgingResponse getLodging(UUID lodgingId) throws ApiCallException {
        Optional<LodgingModel> lodgingOpt = lodgingDataSource.findById(lodgingId);
        if(lodgingOpt.isEmpty()) throw new ApiCallException("Cannot find any lodging with id " + lodgingId, HttpStatus.NOT_FOUND);

        LodgingModel lodgingModel = lodgingOpt.get();

        Optional<List<LodgingImageModel>> imagesOpt = lodgingImageDataSource.getByLodgingId(lodgingId);
        Optional<List<LodgingAmenityModel>> amenitiesOpt = lodgingAmenityDataSource.getByLodgingId(lodgingId);

        if(imagesOpt.isEmpty()) throw new ApiCallException("Cannot find any images with lodging id " + lodgingId, HttpStatus.NOT_FOUND);
        if(amenitiesOpt.isEmpty()) throw new ApiCallException("Cannot find any amenities with lodging id " + lodgingId, HttpStatus.NOT_FOUND);

        List<LodgingImageModel> imageModels = imagesOpt.get();

        Optional<UserModel> ownerOpt = userDataSource.findByUuid(lodgingModel.getOwnerId());
        if(ownerOpt.isEmpty()) throw new ApiCallException("Cannot find any user with id " + lodgingModel.getOwnerId(), HttpStatus.NOT_FOUND);
        UserModel owner = ownerOpt.get();

        return LodgingResponse.builder()
                .id(lodgingModel.getId())
                .name(lodgingModel.getName())
                .address(lodgingModel.getAddress())
                .dayPrice(lodgingModel.getDayPrice())
                .hourPrice(lodgingModel.getHourPrice())
                .area(lodgingModel.getArea())
                .description(lodgingModel.getDescription())
                .uploadDate(lodgingModel.getUploadDate())
                .lat(lodgingModel.getLat())
                .lng(lodgingModel.getLng())
                .images(imageModels.stream().map(LodgingImageModel::getUrl).collect(Collectors.toList()))
                .amenities(lodgingModel.getAmenities() != null ? Arrays.asList(lodgingModel.getAmenities().toUpperCase().split(",")) : Arrays.asList())
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
                .review(
                        ratingRepository.getLodgingRatings(lodgingId)
                )
                .build();
    }

    public LodgingResponse createLodging(CreateLodgingRequest request) throws ApiCallException {

        LatLng latLng = GeocodingUtil.getLatLngFromAddress(request.getAddress());

        LodgingModel lodgingModel = LodgingModel.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .address(request.getAddress())
                .hourPrice(request.getHourPrice())
                .dayPrice(request.getDayPrice())
                .area(request.getArea())
                .description(request.getDescription())
                .uploadDate(System.currentTimeMillis())
                .lat(latLng != null ? latLng.getLatitude() : 0)
                .lng(latLng != null ? latLng.getLongitude() : 0)
                .ownerId(request.getOwnerId())
                .amenities(String.join(",", request.getAmenities()))
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

        for(CreateRoomRequest createRoomRequest : request.getRooms()) {
            RoomModel roomModel = RoomModel.builder()
                    .id(UUID.randomUUID())
                    .renter(null)
                    .lodgingId(lodgingModel.getId())
                    .status("AVAILABLE")
                    .capacity(createRoomRequest.getCapacity())
                    .roomName(createRoomRequest.getRoomName())
                    .build();
            roomDataSource.save(roomModel);
        }

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
                       room -> {
                           Optional<BookingModel> bookings = roomRepository.findFirstEndBooking(room.getId());
                           if(bookings.isEmpty()) return true;
                           BookingModel booking = bookings.get();
                           return booking.getCheckOutAt() <= request.getCheckIn();
                       }
               );
            });
        }

        if(request.getCheckOut() != 0L) {
            predicates.add(lodgingModel -> {
                List<RoomModel> rooms = roomDataSource.findAllByLodgingId(lodgingModel.getId());
                return rooms.stream().anyMatch(
                        room -> {
                            Optional<BookingModel> bookings = roomRepository.findFirstEndBooking(room.getId());
                            if(bookings.isEmpty()) return true;
                            BookingModel booking = bookings.get();
                            return booking.getCheckInAt() >= request.getCheckOut();
                        }

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

        if(request.getAmenities() != null) {
            predicates.add(lodgingModel -> {
               boolean result;
               for(String amenity : request.getAmenities()) {
                   result = lodgingModel.getAmenities().toLowerCase().contains(amenity.toLowerCase());
                   if(!result) return false;
               }
               return true;
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

    public Map<String, Object> book(CreateBookingRequest request) throws ApiCallException {
        Optional<LodgingModel> lodgingModelOpt = lodgingDataSource.findById(request.getLodgingId());
        if(lodgingModelOpt.isEmpty()) {
            throw new ApiCallException("Cannot find any lodging with id '" + request.getLodgingId() + "'", HttpStatus.NOT_FOUND);
        }

        LodgingModel lodgingModel = lodgingModelOpt.get();
        List<RoomModel> rooms = roomDataSource.findAllByLodgingId(lodgingModel.getId());

        if(request.getCriteria().getCheckIn() != 0L) {
            rooms = rooms.stream()
                    .filter(room -> {
                        Optional<BookingModel> bookings = roomRepository.findFirstEndBooking(room.getId());
                        if(bookings.isEmpty()) return true;
                        BookingModel booking = bookings.get();
                        return booking.getCheckOutAt() <= request.getCriteria().getCheckIn();
                    })
                    .toList();
        }

        if(request.getCriteria().getCheckOut() != 0L) {
            rooms = rooms.stream()
                    .filter(
                            room -> {
                                Optional<BookingModel> bookings = roomRepository.findFirstEndBooking(room.getId());
                                if(bookings.isEmpty()) return true;
                                BookingModel booking = bookings.get();
                                return booking.getCheckInAt() >= request.getCriteria().getCheckOut();
                            }
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

        if(result.isEmpty()) {
            throw new ApiCallException("Không có phòng trống", HttpStatus.BAD_REQUEST);
        }

        BookingModel bookingModel = BookingModel.builder()
                .bookingId(UUID.randomUUID())
                .lodgingId(request.getLodgingId())
                .roomId(result.get(0).getId())
                .checkInAt(request.getCriteria().getCheckIn())
                .checkOutAt(request.getCriteria().getCheckOut())
                .numOfRoom(result.size())
                .isBankTransfer(request.isBankTransfer())
                .userId(request.getUserId())
                .build();
        bookingDataSource.save(bookingModel);

        return new HashMap<>() {
            {
                put("rooms", result);
                put("bookingId", bookingModel.getBookingId());
                put("lodgingId", bookingModel.getLodgingId());
                put("checkInAt", bookingModel.getCheckInAt());
                put("checkOutAt", bookingModel.getCheckOutAt());
                put("numOfRoom", bookingModel.getNumOfRoom());
                put("numOfPeople", request.getCriteria().getNumOfPeople());
                put("isBankTransfer", bookingModel.isBankTransfer());
                put("userId", bookingModel.getUserId());
            }
        };
    }

    public List<LodgingResponse> getOwnerLodgings(UUID ownerId) {
        return lodgingDataSource.findAllByOwnerId(ownerId)
                .stream().map(e -> {
                    try {
                        return getLodging(e.getId());
                    } catch (ApiCallException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .toList();
    }

    public List<BookingLodgingResponse> getOwnerLodgingBookings(UUID ownerId) throws ApiCallException {
        List<BookingLodgingResponse> result = new ArrayList<>();
        List<LodgingModel> myLodgings = lodgingDataSource.findAllByOwnerId(ownerId);

        for(LodgingModel lodgingModel : myLodgings) {
            List<BookingModel> bookings = bookingDataSource.findAllByLodgingId(lodgingModel.getId());
            LodgingResponse lodgingResponse = getLodging(lodgingModel.getId());
            for(BookingModel booking : bookings) {
                result.add(BookingLodgingResponse.builder()
                                .id(booking.getBookingId())
                                .lodging(lodgingResponse)
                                .bookedAt(booking.getCheckInAt())
                        .build());
            }
        }

        return result;
    }

    public List<BookingLodgingResponse> getUserLodgingBookings(UUID userId) throws ApiCallException {
        List<BookingLodgingResponse> result = new ArrayList<>();
        List<BookingModel> bookings = bookingDataSource.findAllByUserId(userId);

        for(BookingModel booking : bookings) {
            result.add(BookingLodgingResponse.builder()
                    .lodging(getLodging(booking.getLodgingId()))
                    .bookedAt(booking.getCheckInAt())
                    .build());
        }

        return result;
    }

    public BookingDetailResponse getBookingDetail(UUID bookingId) throws ApiCallException {
        Optional<BookingModel> bookingOpt = bookingDataSource.findByBookingId(bookingId);
        if(bookingOpt.isEmpty()) {
            throw new ApiCallException("Cannot find booking with id '" + bookingId + "'", HttpStatus.NOT_FOUND);
        }
        BookingModel booking = bookingOpt.get();
        LodgingResponse lodgingResponse = getLodging(booking.getLodgingId());
        Optional<UserModel> userOpt = userDataSource.findByUuid(booking.getUserId());
        if(userOpt.isEmpty()) throw new ApiCallException("Cannot find user with id '" + booking.getUserId() + "'", HttpStatus.NOT_FOUND);
        UserModel user = userOpt.get();

        return BookingDetailResponse.builder()
                .bookingId(bookingId)
                .user(
                        UserResponse.builder()
                                .uuid(user.getUuid())
                                .name(user.getName())
                                .address(user.getAddress())
                                .email(user.getEmail())
                                .avatar(user.getAvatar())
                                .phoneNumber(user.getPhoneNumber())
                                .build()
                )
                .isBankTransfer(booking.isBankTransfer())
                .lodging(lodgingResponse)
                .numOfRoom(booking.getNumOfRoom())
                .checkInAt(booking.getCheckInAt())
                .checkOutAt(booking.getCheckOutAt())
                .build();
    }

}
