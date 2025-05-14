package vn.heistom.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.heistom.model.BookingModel;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomStatusResponse {

    RoomResponse room;

    UserResponse user;

    BookingModel booking;

}
