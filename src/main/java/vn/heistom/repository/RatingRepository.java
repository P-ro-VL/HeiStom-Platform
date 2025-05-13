package vn.heistom.repository;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.heistom.api.ApiCallException;
import vn.heistom.datasource.RatingDataSource;
import vn.heistom.datasource.UserDataSource;
import vn.heistom.dto.request.LodgingRatingRequest;
import vn.heistom.dto.response.LodgingRatingResponse;
import vn.heistom.dto.response.RatingResponse;
import vn.heistom.dto.response.UserResponse;
import vn.heistom.model.RatingModel;
import vn.heistom.model.UserModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RatingRepository {

    final RatingDataSource ratingDataSource;
    private final UserDataSource userDataSource;

    public RatingResponse addRating(UUID lodgingId, LodgingRatingRequest request) throws ApiCallException {
        RatingModel rating = ratingDataSource.save(
                RatingModel.builder()
                        .id(UUID.randomUUID())
                        .rating(request.getRating())
                        .comment(request.getComment())
                        .lodgingId(lodgingId)
                        .reviewerId(request.getUserId().toString())
                        .postAt(System.currentTimeMillis())
                        .build()
        );

        Optional<UserModel> userOpt = userDataSource.findByUuid(request.getUserId());
        if(userOpt.isEmpty()) throw new ApiCallException("User not found", HttpStatus.NOT_FOUND);
        UserModel user = userOpt.get();

        return RatingResponse.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .reviewer(
                        UserResponse.builder()
                                .uuid(user.getUuid())
                                .name(user.getName())
                                .address(user.getAddress())
                                .email(user.getEmail())
                                .avatar(user.getAvatar())
                                .phoneNumber(user.getPhoneNumber())
                                .build()
                )
                .postAt(rating.getPostAt())
                .build();
    }

    public LodgingRatingResponse getLodgingRatings(UUID lodgingId) {
        List<RatingModel> lodgingRatings = ratingDataSource.findAllByLodgingId(lodgingId);

        List<RatingResponse> result = lodgingRatings.stream()
                .map(model -> {
                    Optional<UserModel> userOpt = userDataSource.findByUuid(UUID.fromString(model.getReviewerId()));
                    if(userOpt.isEmpty()) try {
                        throw new ApiCallException("User not found", HttpStatus.NOT_FOUND);
                    } catch (ApiCallException e) {
                        throw new RuntimeException(e);
                    }
                    UserModel user = userOpt.get();
                    return RatingResponse.builder()
                        .rating(model.getRating())
                        .comment(model.getComment())
                        .reviewer(
                                UserResponse.builder()
                                        .uuid(user.getUuid())
                                        .name(user.getName())
                                        .address(user.getAddress())
                                        .email(user.getEmail())
                                        .avatar(user.getAvatar())
                                        .phoneNumber(user.getPhoneNumber())
                                        .build()
                        )
                        .postAt(model.getPostAt())
                        .build();})
                .toList();
        double avgRating = result.stream().map(RatingResponse::getRating)
                .reduce((x, y) -> (x + y)/2.0)
                .orElse(0.0);
        return LodgingRatingResponse.builder()
                .averageRating(avgRating)
                .ratings(result)
                .build();
    }

}
