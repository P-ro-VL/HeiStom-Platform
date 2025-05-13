package vn.heistom.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingLodgingResponse {

    UUID id;

    LodgingResponse lodging;

    long bookedAt;

}
