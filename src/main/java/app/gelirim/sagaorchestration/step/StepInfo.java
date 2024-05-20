package app.gelirim.sagaorchestration.step;

public class StepInfo {
    private int sequence;
    private String groupName;
    private String name;
    private String description;
    private boolean revertStep;
    private boolean startRevert;

    public StepInfo() {

    }

    public StepInfo(int sequence, boolean revertStep) {
        this.sequence = sequence;
        this.revertStep = revertStep;
    }

    public StepInfo(String name) {
        this.name = name;
    }

    public StepInfo(int sequence, String groupName, String name, String description, boolean revertStep, boolean startRevert) {
        this.sequence = sequence;
        this.groupName = groupName;
        this.name = name;
        this.description = description;
        this.revertStep = revertStep;
        this.startRevert = startRevert;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRevertStep(boolean revertStep) {
        this.revertStep = revertStep;
    }

    public void setStartRevert(boolean startRevert) {
        this.startRevert = startRevert;
    }

    public int getSequence() {
        return sequence;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRevertStep() {
        return revertStep;
    }

    public boolean isStartRevert() {
        return startRevert;
    }

    public String getGroupName() {
        return groupName;
    }
}
