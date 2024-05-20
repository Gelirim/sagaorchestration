package app.gelirim.sagaorchestration;


import app.gelirim.sagaorchestration.step.StepExecutionLogType;

public abstract class AbstractConfiguredSagaOrchestrationClass {
    protected String groupName;
    protected boolean customInitializeStep;
    protected boolean startupStep;
    protected boolean executionLog;
    protected StepExecutionLogType stepExecutionLogType;
    protected String logTable;

    public AbstractConfiguredSagaOrchestrationClass() {
        checkOrchestrated();
    }

    private void checkOrchestrated() {
        if (!this.getClass().isAnnotationPresent(SagaOrchestration.class))
            throw SagaOrchestrationException.exceptionMustAnnotated(this.getClass().getName(), "@SagaOrchestration");
        SagaOrchestration sagaOrchestration = this.getClass().getAnnotation(SagaOrchestration.class);
        if (sagaOrchestration == null || sagaOrchestration.groupName() == null || sagaOrchestration.groupName().isEmpty())
            throw SagaOrchestrationException.sagaOrchestrationException("The field 'groupName' in the annotation @SagaOrchestration on class " + this.getClass().getName() + " must be set");
        if (this.getClass().isAnnotationPresent(SagaStepsExecutionLog.class)) {
            SagaStepsExecutionLog sagaStepsExecutionLog = this.getClass().getAnnotation(SagaStepsExecutionLog.class);
            if (sagaStepsExecutionLog.logType() == StepExecutionLogType.DB && (sagaStepsExecutionLog.logTable() == null || sagaStepsExecutionLog.logTable().isEmpty())) {
                throw SagaOrchestrationException.sagaOrchestrationException("If annotation @SagaStepsExecutionLog as type StepExecutionLogType.DB, the value for executionLogTable must be provided.");
            }
        }
        groupName = sagaOrchestration.groupName();
        customInitializeStep = sagaOrchestration.customInitializeStep();
        startupStep = sagaOrchestration.startupStep();
        checkExecutionLog();
    }

    private void checkExecutionLog() {
        executionLog = this.getClass().isAnnotationPresent(SagaStepsExecutionLog.class);
        if (executionLog) {
            SagaStepsExecutionLog sagaStepsExecutionLog = this.getClass().getAnnotation(SagaStepsExecutionLog.class);
            stepExecutionLogType = sagaStepsExecutionLog.logType();
            logTable = sagaStepsExecutionLog.logTable();
            if (stepExecutionLogType == StepExecutionLogType.DB && (logTable == null || logTable.isEmpty())) {
                throw SagaOrchestrationException.sagaOrchestrationException("StepExecutionLogType DB tipinde belirlenirse bir logTable tanımlanmak zorunda!!");
            }
        }
    }
}
