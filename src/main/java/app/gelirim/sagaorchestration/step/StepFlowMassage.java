package app.gelirim.sagaorchestration.step;

import java.util.Date;

public class StepFlowMassage {
    private StepReturnStatus status;
    private Date startDate;
    private Date endDate;
    private String message;

    public StepReturnStatus getStatus() {
        return status;
    }

    public void setStatus(StepReturnStatus status) {
        this.status = status;
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
}
