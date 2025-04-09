package vn.heistom.repository;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.heistom.api.ApiCallException;
import vn.heistom.datasource.LodgingAmenityDataSource;
import vn.heistom.datasource.LodgingDataSource;
import vn.heistom.datasource.LodgingImageDataSource;
import vn.heistom.datasource.UserDataSource;
import vn.heistom.dto.request.CreateLodgingRequest;
import vn.heistom.dto.response.LodgingResponse;
import vn.heistom.dto.response.UserResponse;
import vn.heistom.model.LodgingAmenityModel;
import vn.heistom.model.LodgingImageModel;
import vn.heistom.model.LodgingModel;
import vn.heistom.model.UserModel;
import vn.heistom.util.GeocodingUtil;
import vn.heistom.util.LatLng;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LodgingRepository {

    final LodgingDataSource lodgingDataSource;
    final LodgingImageDataSource lodgingImageDataSource;
    final LodgingAmenityDataSource lodgingAmenityDataSource;
    private final UserDataSource userDataSource;

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
                .pricePerDay(lodgingModel.getPricePerDay())
                .pricePerMonth(lodgingModel.getPricePerMonth())
                .area(lodgingModel.getArea())
                .description(lodgingModel.getDescription())
                .views(lodgingModel.getViews())
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
                .pricePerDay(request.getPricePerDay())
                .pricePerMonth(request.getPricePerMonth())
                .area(request.getArea())
                .description(request.getDescription())
                .views(request.getViews())
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

}
