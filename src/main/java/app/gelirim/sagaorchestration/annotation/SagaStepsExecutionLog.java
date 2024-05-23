package app.gelirim.sagaorchestration.annotation;


import app.gelirim.sagaorchestration.executionlog.StepExecutionLogType;
import app.gelirim.sagaorchestration.executionlog.StepLogInsertType;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SagaStepsExecutionLog {
    StepExecutionLogType logType() default StepExecutionLogType.DB;

    String logTable() default "";

    StepLogInsertType logInsertType() default StepLogInsertType.SYNC;
}
