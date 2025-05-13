package vn.heistom.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingResponse {

    UUID id;

    UserResponse reviewer;

    double rating;

    String comment;

    long postAt;

}
