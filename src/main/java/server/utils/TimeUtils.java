package server.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeUtils {

    // Convert LocalDateTime to UTC Timestamp (for storing in database)
    public static Timestamp toUtcTimestamp(LocalDateTime localDateTime) {
        // Convert to UTC by assuming it's in the system default time zone
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault()); // Local to ZonedDateTime
        ZonedDateTime utcZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC")); // Convert to UTC
        return Timestamp.from(utcZonedDateTime.toInstant()); // Convert to Timestamp
    }

    // Convert UTC Timestamp to LocalDateTime (for display purposes)
    public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        // Convert the UTC Timestamp to LocalDateTime in system default time zone
        ZonedDateTime utcZonedDateTime = timestamp.toInstant().atZone(ZoneId.of("UTC")); // From UTC
        ZonedDateTime localZonedDateTime = utcZonedDateTime.withZoneSameInstant(ZoneId.systemDefault()); // Convert to local time
        return localZonedDateTime.toLocalDateTime(); // Return as LocalDateTime
    }
}
