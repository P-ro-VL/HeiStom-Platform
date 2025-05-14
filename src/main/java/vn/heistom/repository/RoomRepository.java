package vn.heistom.repository;

import lombok.AllArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import vn.heistom.datasource.BookingDataSource;
import vn.heistom.datasource.RoomDataSource;
import vn.heistom.datasource.UserDataSource;
import vn.heistom.dto.request.CreateRoomRequest;
import vn.heistom.dto.response.RoomResponse;
import vn.heistom.dto.response.RoomStatusResponse;
import vn.heistom.dto.response.UserResponse;
import vn.heistom.model.BookingModel;
import vn.heistom.model.RoomModel;
import vn.heistom.model.UserModel;

import java.util.*;

@Service
@AllArgsConstructor
public class RoomRepository {

    final RoomDataSource roomDataSource;
    private final BookingDataSource bookingDataSource;
    private final UserDataSource userDataSource;

    public List<RoomModel> createRooms(UUID lodgingId, List<CreateRoomRequest> requests) {
        List<RoomModel> rooms = new ArrayList<>();

        for(CreateRoomRequest request : requests) {
            rooms.add(
                    RoomModel.builder()
                            .id(UUID.randomUUID())
                            .capacity(request.getCapacity())
                            .roomName(request.getRoomName())
                            .status("AVAILABLE")
                            .lodgingId(lodgingId)
                            .renter(null)
                            .build()
            );
        }

        roomDataSource.saveAll(rooms);

        return rooms;
    }

    public Optional<BookingModel> findFirstEndBooking(UUID roomId) {
        List<BookingModel> bookings = bookingDataSource.findAllByRoomId(roomId);

        BookingModel firstAvailableBooking = bookings.stream()
                .filter(b -> b.getCheckOutAt() != 0)
                .min(Comparator.comparingLong(BookingModel::getCheckOutAt))
                .orElse(null);
        return Optional.ofNullable(firstAvailableBooking);
    }

    public List<RoomStatusResponse> getRoomStatus(UUID lodgingId) {
        List<RoomStatusResponse> result = new ArrayList<>();
        List<RoomModel> rooms = roomDataSource.findAllByLodgingId(lodgingId);

        for(RoomModel room : rooms) {
            List<BookingModel> bookings = bookingDataSource.findAllByRoomId(room.getId());
            for(BookingModel booking : bookings) {
                Optional<UserModel> userOpt = userDataSource.findByUuid(booking.getUserId());
                if(userOpt.isPresent()) {
                    UserModel user = userOpt.get();
                    result.add(RoomStatusResponse.builder()
                                    .room(RoomResponse.builder()
                                            .id(room.getId())
                                            .roomName(room.getRoomName())
                                            .status(room.getStatus())
                                            .capacity(room.getCapacity())
                                            .build())
                                    .user(UserResponse.builder()
                                            .uuid(user.getUuid())
                                            .address(user.getAddress())
                                            .avatar(user.getAvatar())
                                            .email(user.getEmail())
                                            .phoneNumber(user.getPhoneNumber())
                                            .name(user.getName())
                                            .build())
                                    .booking(booking)
                            .build());
                }
            }
        }

        return result;
    }
}
