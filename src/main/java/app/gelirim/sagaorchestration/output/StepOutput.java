package app.gelirim.sagaorchestration.output;

import java.util.Date;

public class StepOutput {
    private String name;
    private String description;
    private boolean status;
    private int flowSequence;
    private Date startDate;
    private Date endDate;
    private String message;
    private boolean processStep ;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getFlowSequence() {
        return flowSequence;
    }

    public void setFlowSequence(int flowSequence) {
        this.flowSequence = flowSequence;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isProcessStep() {
        return processStep;
    }

    public void setProcessStep(boolean processStep) {
        this.processStep = processStep;
    }
}
