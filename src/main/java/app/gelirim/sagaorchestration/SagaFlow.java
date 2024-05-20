package app.gelirim.sagaorchestration;


public interface SagaFlow<T> {
    boolean process(T sagaData);

    default boolean revert(T sagaData) {
        return true;
    }
}
