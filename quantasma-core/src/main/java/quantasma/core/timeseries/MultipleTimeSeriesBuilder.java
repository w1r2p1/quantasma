package quantasma.core.timeseries;

import org.ta4j.core.TimeSeries;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class MultipleTimeSeriesBuilder {

    private final TimeSeriesDefinition baseTimeSeriesDefinition;
    private final Set<TimeSeriesDefinition.Group> aggregatedTimeSeriesDefinitions = new HashSet<>();
    private final Set<String> symbols = new HashSet<>();

    private UnaryOperator<TimeSeries> wrapper = timeSeries -> timeSeries;

    private MultipleTimeSeriesBuilder(TimeSeriesDefinition baseTimeSeriesDefinition) {
        this.baseTimeSeriesDefinition = baseTimeSeriesDefinition;
    }

    public static MultipleTimeSeriesBuilder basedOn(TimeSeriesDefinition timeSeriesDefinition) {
        return new MultipleTimeSeriesBuilder(timeSeriesDefinition);
    }

    public MultipleTimeSeriesBuilder aggregate(TimeSeriesDefinition.Group definitions) {
        this.aggregatedTimeSeriesDefinitions.add(definitions);
        return this;
    }

    public MultipleTimeSeriesBuilder symbols(String... symbols) {
        this.symbols.addAll(Arrays.asList(symbols));
        return this;
    }

    public MultipleTimeSeriesBuilder wrap(UnaryOperator<TimeSeries> wrapper) {
        this.wrapper = wrapper;
        return this;
    }

    public Collection<? extends MultipleTimeSeries> build() {
        final Map<String, MultipleTimeSeries> baseTimeSeries = symbols.stream()
                                                                      .map(symbol -> BaseMultipleTimeSeries.create(symbol, baseTimeSeriesDefinition, wrapper))
                                                                      .collect(Collectors.toMap(BaseMultipleTimeSeries::getSymbol, Function.identity()));

        for (TimeSeriesDefinition.Group groupDefinition : aggregatedTimeSeriesDefinitions) {
            for (String symbol : groupDefinition.getSymbols()) {
                for (TimeSeriesDefinition timeSeriesDefinition : groupDefinition.getTimeSeriesDefinitions()) {
                    if (!baseTimeSeries.containsKey(symbol)) {
                        throw new RuntimeException(String.format("Cannot aggregate undefined symbol [%s]", symbol));
                    }
                    baseTimeSeries.compute(symbol, (ignored, multipleTimeSeries) -> multipleTimeSeries.aggregate(timeSeriesDefinition));
                }
            }
        }

        return baseTimeSeries.values();
    }

}
