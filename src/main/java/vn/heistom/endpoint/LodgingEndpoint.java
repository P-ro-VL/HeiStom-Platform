package vn.heistom.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.heistom.api.ApiCallResult;
import vn.heistom.api.ApiExecutorService;
import vn.heistom.api.ApiResponse;
import vn.heistom.dto.request.CreateLodgingRequest;
import vn.heistom.dto.response.LodgingResponse;
import vn.heistom.repository.LodgingRepository;

import java.util.UUID;

@RestController
@RequestMapping(path = "/v1/lodging", name = "Lodging Endpoints")
@AllArgsConstructor
public class LodgingEndpoint {

    private final ApiExecutorService apiExecutorService;
    private final LodgingRepository lodgingRepository;

    @PostMapping(path = "")
    public ResponseEntity<ApiResponse<LodgingResponse>> createLodgingEndpoint(@RequestBody CreateLodgingRequest request, HttpServletRequest httpServletRequest) {
        return apiExecutorService.execute(httpServletRequest, () -> new ApiCallResult<>(lodgingRepository.createLodging(request)));
    }

    @GetMapping(path = "/{code}")
    public ResponseEntity<ApiResponse<LodgingResponse>> getLodgingEndpoint(@PathVariable UUID code, HttpServletRequest httpServletRequest) {
        return apiExecutorService.execute(httpServletRequest, () -> new ApiCallResult<>(lodgingRepository.getLodging(code)));
    }
    
}
