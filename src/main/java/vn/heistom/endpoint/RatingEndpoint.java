package vn.heistom.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.heistom.api.ApiCallExecutor;
import vn.heistom.api.ApiCallResult;
import vn.heistom.api.ApiExecutorService;
import vn.heistom.api.ApiResponse;
import vn.heistom.dto.request.LodgingRatingRequest;
import vn.heistom.dto.response.LodgingRatingResponse;
import vn.heistom.dto.response.RatingResponse;
import vn.heistom.repository.RatingRepository;

import java.util.UUID;

@RestController
@RequestMapping(path = "/v1/rating")
@AllArgsConstructor
public class RatingEndpoint {

    private final ApiExecutorService apiExecutorService;

    final RatingRepository ratingRepository;

    @PostMapping()
    public ResponseEntity<ApiResponse<RatingResponse>> postRating(@RequestParam UUID lodgingId, @RequestBody LodgingRatingRequest request, HttpServletRequest httpServletRequest) {
        return apiExecutorService.execute(httpServletRequest, () -> new ApiCallResult<>(ratingRepository.addRating(lodgingId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<LodgingRatingResponse>> getLodgingResponse(@RequestParam UUID lodgingId, HttpServletRequest request) {
        return apiExecutorService.execute(request, () -> new ApiCallResult<>(ratingRepository.getLodgingRatings(lodgingId)));
    }

}
