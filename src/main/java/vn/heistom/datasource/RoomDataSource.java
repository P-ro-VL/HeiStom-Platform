package vn.heistom.datasource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.heistom.model.RoomModel;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomDataSource extends JpaRepository<RoomModel, String> {

    List<RoomModel> findAllByLodgingId(UUID uuid);

}
