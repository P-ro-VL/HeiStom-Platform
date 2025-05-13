package vn.heistom.dto.response;

import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {

    UUID uuid;

    String name;

    String email;

    String address;

    String phoneNumber;

    String avatar;

    String type;

}
