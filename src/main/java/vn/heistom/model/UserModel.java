package vn.heistom.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {

    @Id
    UUID uuid;

    String name;

    String email;

    String password;

    String address;

    String phoneNumber;

    String avatar;

    String type;

}
