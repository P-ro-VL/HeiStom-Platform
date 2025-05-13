package vn.heistom.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.heistom.api.ApiCallResult;
import vn.heistom.api.ApiExecutorService;
import vn.heistom.api.ApiResponse;
import vn.heistom.dto.request.CreateRoomRequest;
import vn.heistom.dto.response.RoomStatusResponse;
import vn.heistom.model.RoomModel;
import vn.heistom.repository.RoomRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/v1/room", name = "Lodging Endpoints")
@AllArgsConstructor
public class RoomEndpoint {

    private final ApiExecutorService apiExecutorService;
    final RoomRepository roomRepository;

    @PostMapping(path = "/create")
    public ResponseEntity<ApiResponse<List<RoomModel>>> createRooms(@RequestParam UUID lodgingId, @RequestBody List<CreateRoomRequest> request, HttpServletRequest httpServletRequest) {
        return apiExecutorService.execute(httpServletRequest, () -> new ApiCallResult<>(roomRepository.createRooms(lodgingId, request)));
    }

    @GetMapping(path = "/status")
    public ResponseEntity<ApiResponse<List<RoomStatusResponse>>> getRoomsStatus(@RequestParam UUID lodgingId, HttpServletRequest httpServletRequest) {
        return apiExecutorService.execute(httpServletRequest, () -> new ApiCallResult<>(roomRepository.getRoomStatus(lodgingId)));
    }
}
