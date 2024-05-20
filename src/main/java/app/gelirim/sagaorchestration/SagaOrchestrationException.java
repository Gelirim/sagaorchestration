package app.gelirim.sagaorchestration;

public class SagaOrchestrationException extends RuntimeException{
    private static final String BASE_MESSAGE = "Saga Orchestration Exception:";
    public SagaOrchestrationException(String message) {
        super(message);
    }
    public static  SagaOrchestrationException sagaOrchestrationException(String message){
        return new SagaOrchestrationException(BASE_MESSAGE+message);
    }
    public static  SagaOrchestrationException exceptionMustAnnotated(String className, String annotationName){
        String message = BASE_MESSAGE+"The class " + className + " must be annotated with "+annotationName;
        return new SagaOrchestrationException(message);
    }
}
