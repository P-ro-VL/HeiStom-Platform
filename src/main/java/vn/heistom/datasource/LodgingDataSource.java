package vn.heistom.datasource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.heistom.model.LodgingModel;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LodgingDataSource extends JpaRepository<LodgingModel, String> {

    Optional<LodgingModel> findById(UUID id);

}
