package vn.heistom.datasource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.heistom.model.BookingModel;

@Repository
public interface BookingDataSource extends JpaRepository<BookingModel, String> {
}
