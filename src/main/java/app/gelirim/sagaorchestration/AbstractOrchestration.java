package app.gelirim.sagaorchestration;

import app.gelirim.sagaorchestration.exception.ExceptionMessageGenerator;
import app.gelirim.sagaorchestration.exception.SagaOrchestrationException;
import app.gelirim.sagaorchestration.output.ProcessedFlowOutput;
import app.gelirim.sagaorchestration.output.RevertedFlowOutput;
import app.gelirim.sagaorchestration.step.StepFlowMassage;
import app.gelirim.sagaorchestration.step.StepInfo;
import app.gelirim.sagaorchestration.step.StepReturnStatus;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

public abstract class AbstractOrchestration<T> implements Orchestration<T> {

    @Override
    public ProcessedFlowOutput startProcess(LinkedHashMap<StepInfo, SagaFlow<T>> processSagaFlows, T sagaDTO) {
        ProcessedFlowOutput processedFlowOutput = new ProcessedFlowOutput();
        processedFlowOutput.setProcessStatus(true);
        processedFlowOutput.setFlowMassageMap(new HashMap<>());
        int processingCount = 0;
        int successfulCount = 0;
        boolean stopProcessing = false;
        for (StepInfo stepInfo : processSagaFlows.keySet()) {
            StepFlowMassage flowMassage = new StepFlowMassage();
            flowMassage.setStartDate(new Date());
            try {
                SagaFlow<T> sagaFlow = processSagaFlows.get(stepInfo);
                boolean status = sagaFlow.process(sagaDTO);
                if (!status)
                    throw new SagaOrchestrationException("Process returns false");
                flowMassage.setStatus(StepReturnStatus.SUCCESSFUL);
                successfulCount++;
            } catch (Exception exception) {
                flowMassage.setStatus(StepReturnStatus.FAILED);
                flowMassage.setMessage(ExceptionMessageGenerator.exceptionMessage(exception));
                if (stepInfo.isStartRevert())
                    stopProcessing = true;
            }
            flowMassage.setEndDate(new Date());
            processedFlowOutput.getFlowMassageMap().put(stepInfo, flowMassage);
            if (stopProcessing) {
                processedFlowOutput.setProcessStatus(false);
                break;
            }
            processingCount++;
        }
        processedFlowOutput.setSuccessfulCount(successfulCount);
        processedFlowOutput.setProcessingCount(processingCount);
        return processedFlowOutput;
    }

    @Override
    public RevertedFlowOutput startRevert(LinkedHashMap<StepInfo, SagaFlow<T>> revertSagaFlows, T sagaDTO) {
        RevertedFlowOutput revertedFlowOutput = new RevertedFlowOutput();
        revertedFlowOutput.setFlowMassageMap(new HashMap<>());
        int revertingCount = 0;
        int successfulCount = 0;
        for (StepInfo stepInfo : revertSagaFlows.keySet()) {
            StepFlowMassage flowMassage = new StepFlowMassage();
            flowMassage.setStartDate(new Date());
            try {
                SagaFlow<T> sagaFlow = revertSagaFlows.get(stepInfo);
                boolean status = sagaFlow.revert(sagaDTO);
                if (!status)
                    throw new SagaOrchestrationException("Revert returns false");
                flowMassage.setStatus(StepReturnStatus.SUCCESSFUL);
                successfulCount++;
            } catch (Exception exception) {
                flowMassage.setStatus(StepReturnStatus.FAILED);
                flowMassage.setMessage(ExceptionMessageGenerator.exceptionMessage(exception));
            }
            revertingCount++;
            flowMassage.setEndDate(new Date());
            revertedFlowOutput.getFlowMassageMap().put(stepInfo, flowMassage);
        }
        revertedFlowOutput.setSuccessfulCount(successfulCount);
        revertedFlowOutput.setRevertingCount(revertingCount);
        return revertedFlowOutput;
    }
}
