package quantasma.core.timeseries;

import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.num.Num;
import quantasma.core.BarPeriod;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public interface UniversalTimeSeries<B extends Bar> {

    /**
     * Simple {@see org.ta4j.core.Bar} typed TimeSeries useful when passing further to ta4j inner logic
     *
     * @implSpec Returns unmodifiable TimeSeries
     *
     * @return TimeSeries
     */
    TimeSeries timeSeries();

    BarPeriod getBarPeriod();

    String getSymbol();

    B getBar(int i);

    default B getFirstBar() {
        return getBar(getBeginIndex());
    }

    default B getLastBar() {
        return getBar(getEndIndex());
    }

    default void addBar(B bar) {
        addBar(bar, false);
    }

    void addBar(B bar, boolean replace);

    void addBar(Duration timePeriod, ZonedDateTime endTime);

    default void addTrade(Number tradeVolume, Number tradePrice) {
        addTrade(numOf(tradeVolume), numOf(tradePrice));
    }

    default void addTrade(String tradeVolume, String tradePrice) {
        addTrade(numOf(new BigDecimal(tradeVolume)), numOf(new BigDecimal(tradePrice)));
    }

    void addTrade(Num tradeVolume, Num tradePrice);

    void addPrice(Num price);

    default void addPrice(String price) {
        addPrice(new BigDecimal(price));
    }

    default void addPrice(Number price) {
        addPrice(numOf(price));
    }

    String getName();

    int getBarCount();

    default boolean isEmpty() {
        return getBarCount() == 0;
    }

    int getBeginIndex();

    int getEndIndex();

    default String getSeriesPeriodDescription() {
        StringBuilder sb = new StringBuilder();
        if (!isEmpty()) {
            Bar firstBar = getFirstBar();
            Bar lastBar = getLastBar();
            sb.append(firstBar.getEndTime().format(DateTimeFormatter.ISO_DATE_TIME))
              .append(" - ")
              .append(lastBar.getEndTime().format(DateTimeFormatter.ISO_DATE_TIME));
        }
        return sb.toString();
    }

    void setMaximumBarCount(int maximumBarCount);

    int getMaximumBarCount();

    int getRemovedBarsCount();

    Num numOf(Number number);

    Function<Number, Num> function();
}