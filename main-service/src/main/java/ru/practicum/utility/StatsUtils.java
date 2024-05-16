package ru.practicum.utility;

import ru.practicum.client.StatsClient;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.constants.Constants.FORMATTER;

public final class StatsUtils {
    private StatsUtils() {
    }

    public static Long getHits(Long eventId, StatsClient statsClient) {
        LocalDateTime start = LocalDateTime.parse("1900-01-01 00:00:00", FORMATTER);
        LocalDateTime end = LocalDateTime.parse("2999-01-01 00:00:00", FORMATTER);
        List<ViewStatsDto> stats = statsClient.getStats(start, end, List.of("/events/" + eventId), true);
        return stats.isEmpty() ? 0L : stats.get(0).getHits();
    }
}
