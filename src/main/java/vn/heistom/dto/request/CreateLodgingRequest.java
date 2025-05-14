package vn.heistom.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateLodgingRequest {

    String name;

    String address;

    double dayPrice;

    double hourPrice;

    double area;

    String description;

    UUID ownerId;

    List<String> images;

    List<String> amenities;

    List<CreateRoomRequest> rooms;

}
