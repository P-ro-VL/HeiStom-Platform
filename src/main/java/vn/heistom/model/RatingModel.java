package vn.heistom.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "ratings")
public class RatingModel {

    @Id
    UUID id;

    UUID lodgingId;

    String reviewerId;

    double rating;

    String comment;

    long postAt;

}
