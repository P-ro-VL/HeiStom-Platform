package vn.heistom.datasource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.heistom.model.LodgingAmenityModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LodgingAmenityDataSource extends JpaRepository<LodgingAmenityModel, String> {

    Optional<List<LodgingAmenityModel>> getByLodgingId(UUID id);

}
