package vn.heistom.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.heistom.api.ApiCallResult;
import vn.heistom.api.ApiExecutorService;
import vn.heistom.api.ApiResponse;
import vn.heistom.dto.request.CreateBookingRequest;
import vn.heistom.dto.request.CreateLodgingRequest;
import vn.heistom.dto.request.SearchLodgingRequest;
import vn.heistom.dto.request.StatisticsRequest;
import vn.heistom.dto.response.BookingDetailResponse;
import vn.heistom.dto.response.BookingLodgingResponse;
import vn.heistom.dto.response.LodgingResponse;
import vn.heistom.model.LodgingModel;
import vn.heistom.model.RoomModel;
import vn.heistom.repository.LodgingRepository;
import vn.heistom.repository.StatisticsRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "/v1/lodging", name = "Lodging Endpoints")
@AllArgsConstructor
public class LodgingEndpoint {

    private final ApiExecutorService apiExecutorService;
    private final LodgingRepository lodgingRepository;
    private final StatisticsRepository statisticsRepository;

    @PostMapping(path = "")
    public ResponseEntity<ApiResponse<LodgingResponse>> createLodgingEndpoint(@RequestBody CreateLodgingRequest request, HttpServletRequest httpServletRequest) {
        return apiExecutorService.execute(httpServletRequest, () -> new ApiCallResult<>(lodgingRepository.createLodging(request)));
    }

    @GetMapping(path = "/{code}")
    public ResponseEntity<ApiResponse<LodgingResponse>> getLodgingEndpoint(@PathVariable UUID code, HttpServletRequest httpServletRequest) {
        return apiExecutorService.execute(httpServletRequest, () -> new ApiCallResult<>(lodgingRepository.getLodging(code)));
    }

    @PostMapping(path = "/search")
    public ResponseEntity<ApiResponse<List<LodgingResponse>>> searchLodging(@RequestBody SearchLodgingRequest request, HttpServletRequest httpServletRequest) {
        return apiExecutorService.execute(httpServletRequest, () -> new ApiCallResult<>(lodgingRepository.search(request)));
    }

    @PostMapping(path = "/book")
    public ResponseEntity<ApiResponse<List<RoomModel>>> bookLodging(@RequestBody CreateBookingRequest request, HttpServletRequest httpServletRequest) {
        return apiExecutorService.execute(httpServletRequest, () -> new ApiCallResult<>(lodgingRepository.book(request)));
    }

    @GetMapping(path = "/my")
    public ResponseEntity<ApiResponse<List<LodgingModel>>> getOwnerLodgings(@RequestParam UUID ownerId, HttpServletRequest httpServletRequest) {
        return apiExecutorService.execute(httpServletRequest, () -> new ApiCallResult<>(lodgingRepository.getOwnerLodgings(ownerId)));
    }

    @GetMapping(path = "/book")
    public ResponseEntity<ApiResponse<BookingDetailResponse>> getBookingDetail(@RequestParam UUID bookingId, HttpServletRequest httpServletRequest) {
        return apiExecutorService.execute(httpServletRequest, () -> new ApiCallResult<>(lodgingRepository.getBookingDetail(bookingId)));
    }

    @GetMapping(path = "/book-list")
    public ResponseEntity<ApiResponse<List<BookingLodgingResponse>>> getOwnerLodgingsBooking(@RequestParam UUID ownerId, HttpServletRequest httpServletRequest) {
        return apiExecutorService.execute(httpServletRequest, () -> new ApiCallResult<>(lodgingRepository.getOwnerLodgingBookings(ownerId)));
    }

    @GetMapping(path = "/user-book-list")
    public ResponseEntity<ApiResponse<List<BookingLodgingResponse>>> getUserLodgingsBooking(@RequestParam UUID userId, HttpServletRequest httpServletRequest) {
        return apiExecutorService.execute(httpServletRequest, () -> new ApiCallResult<>(lodgingRepository.getUserLodgingBookings(userId)));
    }

    @PostMapping(path = "/statistics")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getStatistics(@RequestBody StatisticsRequest request, HttpServletRequest httpServletRequest) {
        return apiExecutorService.execute(httpServletRequest, () -> new ApiCallResult<>(statisticsRepository.calculateRevenue(request)));
    }

}
