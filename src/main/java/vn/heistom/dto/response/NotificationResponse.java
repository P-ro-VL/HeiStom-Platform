package vn.heistom.dto.response;

import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {

    @Id
    UUID id;

    String title;

    String content;

    boolean hasRead;

}
