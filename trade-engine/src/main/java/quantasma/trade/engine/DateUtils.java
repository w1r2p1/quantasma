package quantasma.trade.engine;

import quantasma.model.CandlePeriod;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    public static ZonedDateTime createEndDate(ZonedDateTime date, CandlePeriod candlePeriod) {
        final int minute = date.getMinute();
        final int hour = date.getHour();
        switch (candlePeriod) {
            case M1:
                return date.truncatedTo(ChronoUnit.MINUTES)
                           .withMinute(minute)
                           .plus(1, ChronoUnit.MINUTES);
            case M5:
                return date.truncatedTo(ChronoUnit.MINUTES)
                           .withMinute(minute)
                           .plus(5 - minute % 5, ChronoUnit.MINUTES);
            case M15:
                return date.truncatedTo(ChronoUnit.MINUTES)
                           .withMinute(minute)
                           .plus(15 - minute % 15, ChronoUnit.MINUTES);
            case M30:
                return date.truncatedTo(ChronoUnit.MINUTES)
                           .withMinute(minute)
                           .plus(30 - minute % 30, ChronoUnit.MINUTES);
            case H1:
                return date.truncatedTo(ChronoUnit.HOURS)
                           .withHour(hour)
                           .plus(1, ChronoUnit.HOURS);
            case H4:
                return date.truncatedTo(ChronoUnit.HOURS)
                           .withHour(hour)
                           .plus(4 - hour % 4, ChronoUnit.HOURS);
            case D:
                final int day = date.getDayOfMonth();
                return date.truncatedTo(ChronoUnit.DAYS)
                           .withDayOfMonth(day)
                           .plus(1, ChronoUnit.DAYS);
        }
        throw new RuntimeException("Unsupported period: " + candlePeriod.getPeriod());
    }
}
