package vn.heistom.datasource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.heistom.model.LodgingImageModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LodgingImageDataSource extends JpaRepository<LodgingImageModel, String> {

    Optional<List<LodgingImageModel>> getByLodgingId(UUID lodgingId);

}
