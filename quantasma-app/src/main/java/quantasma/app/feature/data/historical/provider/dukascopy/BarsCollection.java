package quantasma.app.feature.data.historical.provider.dukascopy;

import com.dukascopy.api.IBar;
import lombok.Getter;
import quantasma.app.model.OhlcvTick;
import quantasma.core.BarPeriod;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

@Getter
class BarsCollection {
    private final Map<Long, Bar> bars = new TreeMap<>();
    private final String symbol;
    private final BarPeriod barPeriod;

    class Bar {
        private final String _symbol = symbol;
        private final BarPeriod _barPeriod = barPeriod;

        private IBar bidBar;
        private IBar askBar;

        OhlcvTick toOhlcv() {
            return new OhlcvTick(_barPeriod,
                                 Instant.ofEpochMilli(bidBar.getTime()),
                                 _symbol,
                                 bidBar.getOpen(),
                                 bidBar.getLow(),
                                 bidBar.getHigh(),
                                 bidBar.getClose(),
                                 askBar.getOpen(),
                                 askBar.getLow(),
                                 askBar.getHigh(),
                                 askBar.getClose(),
                                 (int) (bidBar.getVolume() + askBar.getVolume()));
        }
    }

    BarsCollection(String symbol, BarPeriod barPeriod) {
        this.symbol = symbol;
        this.barPeriod = barPeriod;
    }

    void insertBidBar(IBar bar) {
        final Bar nullIfAbsent = bars.computeIfPresent(bar.getTime(), (time, barBucket) -> {
            barBucket.bidBar = bar;
            return barBucket;
        });
        if (nullIfAbsent == null) {
            Bar barBucket = new Bar();
            barBucket.bidBar = bar;
            bars.putIfAbsent(bar.getTime(), barBucket);
        }
    }

    void insertAskBar(IBar bar) {
        final Bar nullIfAbsent = bars.computeIfPresent(bar.getTime(), (aLong, barBucket) -> {
            barBucket.askBar = bar;
            return barBucket;
        });
        if (nullIfAbsent == null) {
            Bar barBucket = new Bar();
            barBucket.askBar = bar;
            bars.putIfAbsent(bar.getTime(), barBucket);
        }
    }

    void verifyIntegrity() {
        bars.forEach((aLong, barBucket) -> {
            if (barBucket.bidBar == null || barBucket.askBar == null) {
                throw new RuntimeException();
            }
        });
    }

}