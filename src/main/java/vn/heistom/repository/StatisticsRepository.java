package vn.heistom.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.heistom.datasource.BookingDataSource;
import vn.heistom.datasource.LodgingDataSource;
import vn.heistom.dto.request.StatisticsRequest;
import vn.heistom.model.BookingModel;
import vn.heistom.model.LodgingModel;

import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsRepository {

    private final BookingDataSource bookingDataSource;
    private final LodgingDataSource lodgingDataSource;

    public Map<String, Double> calculateRevenue(StatisticsRequest request) {
        UUID lodgingId = request.getLodgingId();
        String type = request.getType().toUpperCase();
        long startDateMillis = request.getStartDate();

        LodgingModel lodging = lodgingDataSource.findById(lodgingId)
                .orElseThrow(() -> new IllegalArgumentException("Lodging not found"));
        double dayPrice = lodging.getDayPrice();
        List<BookingModel> bookings = bookingDataSource.findAllByLodgingId(lodgingId);

        LocalDate startDate = Instant.ofEpochMilli(startDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        Map<String, Double> result = new LinkedHashMap<>();

        switch (type) {
            case "TOTAL" -> {
                double total = bookings.stream()
                        .mapToDouble(booking -> calculateBookingRevenue(booking, dayPrice))
                        .sum();
                result.put("value", total);
            }

            case "DAY" -> {
                double dayRevenue = bookings.stream()
                        .filter(booking -> isBookingCoversDate(booking, startDate))
                        .mapToDouble(booking -> dayPrice * booking.getNumOfRoom())
                        .sum();
                result.put("value", dayRevenue);
            }

            case "WEEK" -> {
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                LocalDate weekStart = startDate.with(weekFields.dayOfWeek(), 1);
                LocalDate weekEnd = startDate.with(weekFields.dayOfWeek(), 7);

                // Initialize keys Mon to Sun with 0.0
                for (int i = 0; i < 7; i++) {
                    LocalDate day = weekStart.plusDays(i);
                    String dayLabel = day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                    result.put(dayLabel, 0.0);
                }

                for (BookingModel booking : bookings) {
                    List<LocalDate> bookingDates = getBookingDatesInRange(booking);
                    for (LocalDate date : bookingDates) {
                        if (!date.isBefore(weekStart) && !date.isAfter(weekEnd)) {
                            String dayLabel = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                            double revenue = booking.getNumOfRoom() * dayPrice;
                            result.put(dayLabel, result.getOrDefault(dayLabel, 0.0) + revenue);
                        }
                    }
                }
            }

            default -> throw new IllegalArgumentException("Invalid type: " + type);
        }

        return result;
    }

    private List<LocalDate> getBookingDatesInRange(BookingModel booking) {
        LocalDate checkInDate = Instant.ofEpochMilli(booking.getCheckInAt())
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate checkOutDate = Instant.ofEpochMilli(booking.getCheckOutAt())
                .atZone(ZoneId.systemDefault()).toLocalDate();

        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate date = checkInDate; date.isBefore(checkOutDate); date = date.plusDays(1)) {
            dates.add(date);
        }
        return dates;
    }

    private boolean isBookingCoversDate(BookingModel booking, LocalDate date) {
        LocalDate checkInDate = Instant.ofEpochMilli(booking.getCheckInAt())
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate checkOutDate = Instant.ofEpochMilli(booking.getCheckOutAt())
                .atZone(ZoneId.systemDefault()).toLocalDate();

        return !date.isBefore(checkInDate) && date.isBefore(checkOutDate); // exclusive of checkout
    }

    private double calculateBookingRevenue(BookingModel booking, double dayPrice) {
        long checkIn = booking.getCheckInAt();
        long checkOut = booking.getCheckOutAt();

        long days = Math.max(1, (checkOut - checkIn) / (1000 * 60 * 60 * 24));
        return days * dayPrice * booking.getNumOfRoom();
    }


}
