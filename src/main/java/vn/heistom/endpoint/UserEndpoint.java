package vn.heistom.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.heistom.api.ApiCallResult;
import vn.heistom.api.ApiExecutorService;
import vn.heistom.api.ApiResponse;
import vn.heistom.dto.request.AuthenticationRequest;
import vn.heistom.dto.response.UserResponse;
import vn.heistom.repository.UserRepository;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/v1/user", name = "User Endpoints")
public class UserEndpoint {

    private final ApiExecutorService apiExecutorService;

    final UserRepository userRepository;

    @PostMapping(path = "/auth")
    public ResponseEntity<ApiResponse<UserResponse>> authEndpoint(@RequestBody AuthenticationRequest request, HttpServletRequest httpServletRequest) {
        return apiExecutorService.execute(httpServletRequest, () -> new ApiCallResult<>(userRepository.authenticate(request)));
    }

}
