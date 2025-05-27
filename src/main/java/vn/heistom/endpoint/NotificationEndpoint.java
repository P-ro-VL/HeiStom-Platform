package vn.heistom.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.heistom.api.ApiCallResult;
import vn.heistom.api.ApiExecutorService;
import vn.heistom.api.ApiResponse;
import vn.heistom.dto.response.NotificationResponse;
import vn.heistom.repository.NotificationRepository;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/v1/notification", name = "Notification")
public class NotificationEndpoint {

    ApiExecutorService apiExecutorService;

    NotificationRepository repository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAllNotifications(@RequestParam UUID userId, HttpServletRequest request) {
        return apiExecutorService.execute(request, () -> new ApiCallResult<>(repository.getAllNotifications(userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> updateNotificationsStatus(@RequestParam UUID userId, HttpServletRequest httpServletRequest) {
        return apiExecutorService.execute(httpServletRequest, () -> new ApiCallResult<>(repository.updateAllNotificationsStatus(userId)));
    }

}
