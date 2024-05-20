package app.gelirim.sagaorchestration;


import app.gelirim.sagaorchestration.step.StepExecutionLogType;
import app.gelirim.sagaorchestration.step.StepLogInsertType;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SagaStepsExecutionLog {
    StepExecutionLogType logType() default StepExecutionLogType.DB;

    String logTable() default "";

    StepLogInsertType logInsertType() default StepLogInsertType.SYNC;
}
