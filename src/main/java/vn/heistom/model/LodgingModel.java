package vn.heistom.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "lodgings")
@AllArgsConstructor
@NoArgsConstructor
public class LodgingModel {

    @Id
    UUID id;

    String name;

    String address;

    double dayPrice;

    double hourPrice;

    double area;

    String description;

    long uploadDate;

    double lat;

    double lng;

    UUID ownerId;

}
