package app.gelirim.sagaorchestration.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface SagaOrchestration {
    String groupName();

    boolean customInitializeStep() default false;

    boolean startupStep() default true;
}
