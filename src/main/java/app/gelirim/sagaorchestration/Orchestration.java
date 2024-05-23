package app.gelirim.sagaorchestration;

import app.gelirim.sagaorchestration.output.ProcessedFlowOutput;
import app.gelirim.sagaorchestration.output.RevertedFlowOutput;
import app.gelirim.sagaorchestration.step.StepInfo;

import java.util.LinkedHashMap;

public interface Orchestration<T> {
    T startOrchestration(T sagaDTO);

    ProcessedFlowOutput startProcess(LinkedHashMap<StepInfo, SagaFlow<T>> processSagaFlows, T sagaDTO);

    RevertedFlowOutput startRevert(LinkedHashMap<StepInfo, SagaFlow<T>> revertSagaFlows, T sagaDTO);
}
