package quantasma.integrations.event;

public interface EventSink {
    void flush(Event<?> event);

    <E extends Event<D>, D> EventSink install(Class<E> type, EventPipe<E, D> eventHandler);

    static EventSink instance() {
        return new WorkingEventSink();
    }
}