package app.gelirim.sagaorchestration.exception;

public class ExceptionMessageGenerator {
    public static String exceptionMessage(Exception e) {
        String message = e.getMessage();
        if (e.getCause() != null) {
            message = e.getCause().getMessage();
        }
        return message;
    }
}
