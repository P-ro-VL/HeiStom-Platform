package vn.heistom.datasource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.heistom.model.RatingModel;

import java.util.List;
import java.util.UUID;

@Repository
public interface RatingDataSource extends JpaRepository<RatingModel, String> {

    List<RatingModel> findAllByLodgingId(UUID lodgingId);

}
