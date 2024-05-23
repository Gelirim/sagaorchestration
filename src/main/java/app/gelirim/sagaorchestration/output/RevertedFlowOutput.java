package app.gelirim.sagaorchestration.output;


import app.gelirim.sagaorchestration.step.StepFlowMassage;
import app.gelirim.sagaorchestration.step.StepInfo;

import java.util.Map;

public class RevertedFlowOutput {
    private int revertingCount;
    private int successfulCount;
    private Map<StepInfo, StepFlowMassage> flowMassageMap;

    public int getRevertingCount() {
        return revertingCount;
    }

    public void setRevertingCount(int revertedCount) {
        this.revertingCount = revertedCount;
    }

    public Map<StepInfo, StepFlowMassage> getFlowMassageMap() {
        return flowMassageMap;
    }

    public void setFlowMassageMap(Map<StepInfo, StepFlowMassage> flowMassageMap) {
        this.flowMassageMap = flowMassageMap;
    }

    public int getSuccessfulCount() {
        return successfulCount;
    }

    public void setSuccessfulCount(int successfulCount) {
        this.successfulCount = successfulCount;
    }
}
