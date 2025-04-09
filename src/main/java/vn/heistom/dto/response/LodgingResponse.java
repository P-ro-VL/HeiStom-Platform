package vn.heistom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LodgingResponse {

    UUID id;

    String name;

    String address;

    double pricePerDay;

    double pricePerMonth;

    double area;

    String description;

    int views;

    long uploadDate;

    double lat;

    double lng;

    List<String> images;

    List<String> amenities;

    UserResponse owner;

}
