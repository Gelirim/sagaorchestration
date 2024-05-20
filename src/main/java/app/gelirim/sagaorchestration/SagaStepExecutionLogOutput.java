package app.gelirim.sagaorchestration;

import java.util.Date;
import java.util.List;

public class SagaStepExecutionLogOutput {
    private String groupName;
    private Date sagaRunStartDate;
    private Date sagaRunEndDate;
    private boolean processStatus;
    private int processingCount;
    private int successfulProcessingCount;
    private int revertingCount;
    private int successfulRevertingCount;
    private List<StepOutput> stepOutputs;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Date getSagaRunStartDate() {
        return sagaRunStartDate;
    }

    public void setSagaRunStartDate(Date sagaRunStartDate) {
        this.sagaRunStartDate = sagaRunStartDate;
    }

    public Date getSagaRunEndDate() {
        return sagaRunEndDate;
    }

    public void setSagaRunEndDate(Date sagaRunEndDate) {
        this.sagaRunEndDate = sagaRunEndDate;
    }

    public boolean isProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(boolean processStatus) {
        this.processStatus = processStatus;
    }

    public int getProcessingCount() {
        return processingCount;
    }

    public void setProcessingCount(int processingCount) {
        this.processingCount = processingCount;
    }

    public int getSuccessfulProcessingCount() {
        return successfulProcessingCount;
    }

    public void setSuccessfulProcessingCount(int successfulProcessingCount) {
        this.successfulProcessingCount = successfulProcessingCount;
    }

    public int getRevertingCount() {
        return revertingCount;
    }

    public void setRevertingCount(int revertingCount) {
        this.revertingCount = revertingCount;
    }

    public int getSuccessfulRevertingCount() {
        return successfulRevertingCount;
    }

    public void setSuccessfulRevertingCount(int successfulRevertingCount) {
        this.successfulRevertingCount = successfulRevertingCount;
    }

    public List<StepOutput> getStepOutputs() {
        return stepOutputs;
    }

    public void setStepOutputs(List<StepOutput> stepOutputs) {
        this.stepOutputs = stepOutputs;
    }
}
