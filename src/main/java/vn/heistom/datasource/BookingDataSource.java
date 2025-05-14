package vn.heistom.datasource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.heistom.model.BookingModel;
import vn.heistom.model.RoomModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingDataSource extends JpaRepository<BookingModel, String> {

    Optional<BookingModel> findByBookingId(UUID bookingId);

    List<BookingModel> findAllByRoomId(UUID roomId);

    List<BookingModel> findAllByLodgingId(UUID lodgingId);

    List<BookingModel> findAllByUserId(UUID userId);

}
