package vn.heistom.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchLodgingRequest {

    String address;

    long checkIn;

    long checkOut;

    int numOfPeople;

    int numOfRoom;

    List<String> amenities;

}
