package vn.heistom.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import vn.heistom.datasource.NotificationDataSource;
import vn.heistom.dto.request.UpdateNotificationStatusRequest;
import vn.heistom.dto.response.NotificationResponse;
import vn.heistom.model.NotificationModel;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class NotificationRepository {

    NotificationDataSource notificationDataSource;

    public List<NotificationResponse> getAllNotifications(UUID userId) {
        List<NotificationModel> models = notificationDataSource.getAllByReceiverId(userId);
        return models.stream()
                .map(e -> NotificationResponse.builder()
                        .id(e.getId())
                        .title(e.getTitle())
                        .content(e.getContent())
                        .hasRead(e.isHasRead())
                        .build())
                .toList();
    }

    public boolean updateAllNotificationsStatus(UUID userId) {
        List<NotificationModel> models = notificationDataSource.getAllByReceiverId(userId);

        for(NotificationModel model : models) {
            model.setHasRead(true);
        }

        notificationDataSource.saveAll(models);
        return true;
    }

}
