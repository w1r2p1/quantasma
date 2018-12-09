package quantasma.app.service;

import quantasma.integrations.event.AccountEvent;
import quantasma.integrations.event.QuoteEvent;
import reactor.core.publisher.Flux;

public interface EventsService {
    Flux<QuoteEvent> quotes();

    void publish(QuoteEvent quoteEvent);

    void publish(AccountEvent accountEvent);
}
