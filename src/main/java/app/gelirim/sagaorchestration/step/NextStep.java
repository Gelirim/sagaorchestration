package app.gelirim.sagaorchestration.step;

import java.util.List;

public class NextStep {

    private StepFactory builder;
    private StepInfo stepInfo;

    public NextStep(StepFactory builder) {
        this.stepInfo = new StepInfo();
        this.builder = builder;
        this.builder.setInfo(stepInfo);
    }

    public NextStep name(String name) {
        stepInfo.setName(name);
        setInfo();
        return this;
    }

    public NextStep sequence(int sequence) {
        stepInfo.setSequence(sequence);
        setInfo();
        return this;
    }

    public NextStep groupName(String groupName) {
        stepInfo.setGroupName(groupName);
        setInfo();
        return this;
    }

    public NextStep description(String description) {
        stepInfo.setDescription(description);
        setInfo();
        return this;
    }

    public NextStep revertStep(boolean revertStep) {
        stepInfo.setRevertStep(revertStep);
        setInfo();
        return this;
    }

    public NextStep startRevert(boolean startRevert) {
        stepInfo.setStartRevert(startRevert);
        setInfo();
        return this;
    }

    public List<StepInfo> build() {
        return builder.build();
    }

    public NextStep next() {
        return new NextStep(this.builder);
    }

    private void setInfo() {
        this.builder.setInfo(stepInfo);
    }
}
