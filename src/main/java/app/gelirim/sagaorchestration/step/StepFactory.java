package app.gelirim.sagaorchestration.step;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StepFactory {
    private Set<StepInfo> stepInfoList;

    private StepFactory() {
    }

    public static NextStep factory() {
        return new NextStep(new StepFactory());
    }

    public void setInfo(StepInfo stepInfo) {
        if (stepInfoList == null)
            stepInfoList = new HashSet<>();
        stepInfoList.add(stepInfo);
    }

    public List<StepInfo> build() {
        return new ArrayList<>(stepInfoList);
    }

}
