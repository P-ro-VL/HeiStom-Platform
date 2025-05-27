package vn.heistom.datasource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.heistom.model.NotificationModel;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationDataSource extends JpaRepository<NotificationModel, String> {

    List<NotificationModel> getAllByReceiverId(UUID receiverId);

}
