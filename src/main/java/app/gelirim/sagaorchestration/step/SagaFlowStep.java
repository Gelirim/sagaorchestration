package app.gelirim.sagaorchestration.step;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface SagaFlowStep {

    String groupName();

    String name() default "";

    int sequence() default 1;

    boolean revertStep() default false;

    boolean startRevert() default true;

    String description() default "";
}
