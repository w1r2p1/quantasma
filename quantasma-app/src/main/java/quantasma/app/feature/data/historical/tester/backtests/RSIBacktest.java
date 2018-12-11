package quantasma.app.feature.data.historical.tester.backtests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ta4j.core.Order;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import quantasma.app.service.OhlcvTickService;
import quantasma.core.BarPeriod;
import quantasma.core.BaseContext;
import quantasma.core.Context;
import quantasma.core.TestManager;
import quantasma.core.TestMarketData;
import quantasma.core.TradeStrategy;
import quantasma.core.analysis.StrategyBacktest;
import quantasma.core.analysis.TradeScenario;
import quantasma.core.analysis.parametrize.Producer;
import quantasma.core.analysis.parametrize.Variable;
import quantasma.core.analysis.parametrize.Variables;
import quantasma.core.timeseries.MultipleTimeSeriesBuilder;
import quantasma.core.timeseries.ReflectionManualIndexTimeSeries;
import quantasma.core.timeseries.TimeSeriesDefinition;
import quantasma.examples.RSIStrategy;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAmount;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static quantasma.core.analysis.parametrize.Ints.range;

@Component
public class RSIBacktest implements StrategyBacktest {

    private static final String SYMBOL = "EURUSD";
    private static final BarPeriod BASE_PERIOD = BarPeriod.M1;

    private final OhlcvTickService tickService;

    @Autowired
    public RSIBacktest(OhlcvTickService tickService) {
        this.tickService = tickService;
    }

    @Override
    public Class<? extends TradeStrategy> strategy() {
        return RSIStrategy.class;
    }

    @Override
    public List<TradeScenario> run(LocalDateTime from, TemporalAmount window) {
        final TestMarketData testMarketData = createTestMarketData();

        final Context context = new BaseContext.Builder()
                .withMarketData(testMarketData)
                .build();

        final Function<Variables, TradeStrategy> recipe = var -> {
            var._int("rsiPeriod").values(10, 14);
            var._int("rsiLowerBound").with(range(10, 40, 10));
            var._int("rsiUpperBound").with(range(90, 60, 10));
            var._String("tradeSymbol").with("EURUSD");
            return RSIStrategy.buildBullish(context, var.getParameters());
        };

        // implement strategies: close, open, 4 ticks ohlc
        tickService.findBySymbolAndDateBetweenOrderByDate(SYMBOL, from.toInstant(ZoneOffset.UTC), window)
                   .forEach(ohlcvTick -> testMarketData.add(ohlcvTick.getSymbol(),
                                                            ohlcvTick.getDate().atZone(ZoneOffset.UTC),
                                                            ohlcvTick.getBidClose(),
                                                            ohlcvTick.getAskClose()));

        final TestManager testManager = new TestManager(testMarketData);
        final Producer<TradeStrategy> producer = Producer.from(recipe);
        final List<TradeScenario> result = new LinkedList<>();
        while (producer.hasNext()) {
            final TradeStrategy tradeStrategy = producer.next();
            result.add(new TradeScenario(testManager.getMainTimeSeries(tradeStrategy),
                                         tradeStrategy.getParameters(),
                                         testManager.run(tradeStrategy, Order.OrderType.BUY)));
        }
        return result;
    }

    private static TestMarketData createTestMarketData() {
        return new TestMarketData(
                MultipleTimeSeriesBuilder.basedOn(TimeSeriesDefinition.unlimited(BASE_PERIOD))
                                         .symbols(SYMBOL)
                                         .wrap(ReflectionManualIndexTimeSeries::wrap)
                                         .build());
    }

}
