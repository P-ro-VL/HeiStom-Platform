package vn.heistom.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDetailResponse {

    UUID bookingId;

    LodgingResponse lodging;

    UserResponse user;

    long checkInAt;

    long checkOutAt;

    boolean isBankTransfer;

    int numOfRoom;

}
