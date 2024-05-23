package app.gelirim.sagaorchestration.output;



import app.gelirim.sagaorchestration.step.StepFlowMassage;
import app.gelirim.sagaorchestration.step.StepInfo;

import java.util.Map;

public class ProcessedFlowOutput {
    private boolean processStatus;
    private int processingCount;
    private int successfulCount;
    private Map<StepInfo, StepFlowMassage> flowMassageMap;

    public int getProcessingCount() {
        return processingCount;
    }

    public void setProcessingCount(int processingCount) {
        this.processingCount = processingCount;
    }

    public Map<StepInfo, StepFlowMassage> getFlowMassageMap() {
        return flowMassageMap;
    }

    public void setFlowMassageMap(Map<StepInfo, StepFlowMassage> flowMassageMap) {
        this.flowMassageMap = flowMassageMap;
    }

    public boolean isProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(boolean processStatus) {
        this.processStatus = processStatus;
    }

    public int getSuccessfulCount() {
        return successfulCount;
    }

    public void setSuccessfulCount(int successfulCount) {
        this.successfulCount = successfulCount;
    }
}
