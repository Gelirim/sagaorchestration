package app.gelirim.sagaorchestration;

import app.gelirim.sagaorchestration.annotation.SagaFlowStep;
import app.gelirim.sagaorchestration.exception.SagaOrchestrationException;
import app.gelirim.sagaorchestration.executionlog.SagaStepExecutionLogOutput;
import app.gelirim.sagaorchestration.executionlog.SagaStepExecutionLogService;
import app.gelirim.sagaorchestration.executionlog.StepExecutionLogType;
import app.gelirim.sagaorchestration.output.ProcessedFlowOutput;
import app.gelirim.sagaorchestration.output.RevertedFlowOutput;
import app.gelirim.sagaorchestration.output.StepOutput;
import app.gelirim.sagaorchestration.step.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public abstract class AbstractConfiguredSagaOrchestration<T> extends AbstractConfiguredSagaOrchestrationClass<T> implements InitializingBean {
    protected final Log logger = LogFactory.getLog(this.getClass());
    private final Map<StepInfo, SagaFlow<T>> sagaFlowMap = new HashMap<>();
    private boolean demandSteps = false;


    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @SuppressWarnings("unchecked")

    public List<StepInfo> initializeStep() {
        return null;
    }


    public SagaStepExecutionLogService initializeSagaStepExecutionLogService() {
        return null;
    }

    @Override
    public final T startOrchestration(T sagaDTO) {
        Date start = new Date();
        LinkedHashMap<StepInfo, SagaFlow<T>> processSagaFlows = prepareProcessSagaFlows();
        ProcessedFlowOutput processedFlowOutput = startProcess(processSagaFlows, sagaDTO);
        RevertedFlowOutput revertedFlowOutput = null;
        if (!processedFlowOutput.isProcessStatus()) {
            LinkedHashMap<StepInfo, SagaFlow<T>> revertSagaFlows = prepareRevertSagaFlows(processSagaFlows, processedFlowOutput.getProcessingCount());
            revertedFlowOutput = startRevert(revertSagaFlows, sagaDTO);
        }
        Date end = new Date();
        executingLog(processedFlowOutput,revertedFlowOutput,start,end);
        return sagaDTO;
    }


    @SuppressWarnings("unchecked")
    @Override
    public final void afterPropertiesSet() throws Exception {
        Map<String, Object> allFlowStepBeans = applicationContext.getBeansWithAnnotation(SagaFlowStep.class);
        for (Map.Entry<String, Object> entry : allFlowStepBeans.entrySet()) {
            Object bean = entry.getValue();
            if (bean instanceof SagaFlow<?> instance) {
                SagaFlow<T> sagaFlow = (SagaFlow<T>) bean;
                fillSagaFlowMap(sagaFlow);
            }
        }
        checkStartSagaOrchestration();
        checkExecutingLogService();
    }

    private void fillSagaFlowMap(SagaFlow<T> sagaFlow) {
        SagaFlowStep sagaFlowStep = sagaFlow.getClass().getAnnotation(SagaFlowStep.class);
        if (sagaFlowStep != null && sagaFlowStep.groupName().equals(groupName)) {
            StepInfo stepInfo = new StepInfo();
            if (!customInitializeStep) {
                stepInfo = new StepInfo(sagaFlowStep.sequence(), groupName, sagaFlowStep.name(), sagaFlowStep.description(), sagaFlowStep.revertStep(), sagaFlowStep.startRevert());
                sagaFlowMap.put(stepInfo, sagaFlow);
            } else if (startupStep) {
                if (initializeStep() == null) {
                    throw SagaOrchestrationException.sagaOrchestrationException("If startup method is selected, the  initializeStepOnStartup method in the " + this.getClass().getName() + " class should return step information.");
                }
                stepInfo = initializeStep().stream().filter(step -> step.getName().equals(sagaFlowStep.name())).findFirst()
                        .orElseThrow(() -> SagaOrchestrationException.sagaOrchestrationException("Tanımlanan Step adına uygun step infosu bulunamadı!"));
                sagaFlowMap.put(stepInfo, sagaFlow);
            } else {
                demandSteps = true;
                sagaFlowMap.put(stepInfo, sagaFlow);
                logger.info("initializeStepOnDemand selected.");
            }
        }
    }

    private void checkStartSagaOrchestration() {
        if (!demandSteps) {
            checkUniqueSequence();
        }
    }

    private void checkSagaFlowMap() {
        if (!sagaFlowMap.isEmpty()) {
            throw SagaOrchestrationException.sagaOrchestrationException("!!Orchestrated saga flow map is empty!!");
        }
    }

    private void checkUniqueSequence() {
        Set<Integer> uniqueValues = new HashSet<>();
        sagaFlowMap.keySet().forEach(stepInfo -> {
            if (!uniqueValues.add(stepInfo.getSequence())) {
                throw SagaOrchestrationException.sagaOrchestrationException("!!Aynı sequence ait stepler tanımlanamaz!!");
            }
        });
    }

    private LinkedHashMap<StepInfo, SagaFlow<T>> prepareProcessSagaFlows() {
        if (demandSteps) {
            return prepareProcessDemandSagaFlows();
        }
        return prepareProcessStartupSagaFlows();
    }

    private LinkedHashMap<StepInfo, SagaFlow<T>> prepareRevertSagaFlows(LinkedHashMap<StepInfo, SagaFlow<T>> processingSagaFlows, int processingCount) {
        LinkedHashMap<StepInfo, SagaFlow<T>> revertSagaFlows = new LinkedHashMap<>();
        int count = 0;
        for (StepInfo stepInfo : processingSagaFlows.keySet()) {
            if (count < processingCount) {
                revertSagaFlows.put(stepInfo, processingSagaFlows.get(stepInfo));
            }
            count++;
        }
        revertSagaFlows.entrySet().removeIf(stepInfoSagaFlowEntry -> !stepInfoSagaFlowEntry.getKey().isRevertStep());
        List<Map.Entry<StepInfo, SagaFlow<T>>> entryList = new ArrayList<>(revertSagaFlows.entrySet());
        Collections.reverse(entryList);
        LinkedHashMap<StepInfo, SagaFlow<T>> reverseRevertSagaFlows = new LinkedHashMap<>();
        for (Map.Entry<StepInfo, SagaFlow<T>> entry : entryList) {
            reverseRevertSagaFlows.put(entry.getKey(), entry.getValue());
        }
        return reverseRevertSagaFlows;
    }

    private LinkedHashMap<StepInfo, SagaFlow<T>> prepareProcessStartupSagaFlows() {
        LinkedHashMap<StepInfo, SagaFlow<T>> processSagaFlows;
        Map<StepInfo, SagaFlow<T>> processFlowsMap = new HashMap<>(sagaFlowMap);
        return sortedSagaFlowMapForProcessing(processFlowsMap);
    }

    private LinkedHashMap<StepInfo, SagaFlow<T>> prepareProcessDemandSagaFlows() {
        LinkedHashMap<StepInfo, SagaFlow<T>> processSagaFlows = null;
        Map<StepInfo, SagaFlow<T>> processFlowsMap = new HashMap<>();
        List<StepInfo> stepInfoList = initializeStep();
        if (stepInfoList == null || stepInfoList.isEmpty()) {
            throw new RuntimeException("SagaOrchestration step list is empty!");
        }
        sagaFlowMap.values().forEach(sagaFlow -> {
            SagaFlowStep sagaFlowStep = sagaFlow.getClass().getAnnotation(SagaFlowStep.class);
            AtomicReference<StepInfo> stepInfo = new AtomicReference<>();
            stepInfoList.forEach(step -> {
                if (step.getName().equals(sagaFlowStep.name())) {
                    stepInfo.set(step);
                }
            });
            if (stepInfo.get() == null) {
                throw SagaOrchestrationException.sagaOrchestrationException("Tanımlanan Step adına uygun step infosu bulunamadı!");
            }
            processFlowsMap.put(stepInfo.get(), sagaFlow);
        });
        return sortedSagaFlowMapForProcessing(processFlowsMap);
    }

    private LinkedHashMap<StepInfo, SagaFlow<T>> sortedSagaFlowMapForProcessing(Map<StepInfo, SagaFlow<T>> processFlowsMap) {
        return processFlowsMap.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getKey().getSequence()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }


    private void checkExecutingLogService() {
        if (executionLog && stepExecutionLogType == StepExecutionLogType.CUSTOM && initializeSagaStepExecutionLogService() == null)
            throw new SagaOrchestrationException("Executing log service is null!");
    }

    private void executingLog(ProcessedFlowOutput processedFlowOutput, RevertedFlowOutput revertedFlowOutput, Date startDate, Date endDate) {
        if (executionLog) {
            SagaStepExecutionLogOutput sagaStepExecutionLogOutput = new SagaStepExecutionLogOutput();
            sagaStepExecutionLogOutput.setGroupName(groupName);
            sagaStepExecutionLogOutput.setSagaRunStartDate(startDate);
            sagaStepExecutionLogOutput.setSagaRunEndDate(endDate);
            sagaStepExecutionLogOutput.setProcessingCount(processedFlowOutput.getProcessingCount());
            sagaStepExecutionLogOutput.setSuccessfulProcessingCount(processedFlowOutput.getSuccessfulCount());
            sagaStepExecutionLogOutput.setProcessStatus(processedFlowOutput.isProcessStatus());
            sagaStepExecutionLogOutput.setStepOutputs(new ArrayList<>());
            processedFlowOutput.getFlowMassageMap().forEach((stepInfo, stepFlowMassage) -> {
                StepOutput stepOutput = new StepOutput();
                stepOutput.setName(stepInfo.getName());
                stepOutput.setDescription(stepInfo.getDescription());
                stepOutput.setFlowSequence(stepInfo.getSequence());
                stepOutput.setStatus(stepFlowMassage.getStatus() == StepReturnStatus.SUCCESSFUL);
                stepOutput.setMessage(stepFlowMassage.getMessage());
                stepOutput.setStartDate(stepFlowMassage.getStartDate());
                stepOutput.setEndDate(stepFlowMassage.getEndDate());
                stepOutput.setProcessStep(true);
                sagaStepExecutionLogOutput.getStepOutputs().add(stepOutput);
            });
            if (revertedFlowOutput!=null){
                sagaStepExecutionLogOutput.setRevertingCount(revertedFlowOutput.getRevertingCount());
                sagaStepExecutionLogOutput.setSuccessfulRevertingCount(revertedFlowOutput.getSuccessfulCount());
                revertedFlowOutput.getFlowMassageMap().forEach((stepInfo, stepFlowMassage) -> {
                    StepOutput stepOutput = new StepOutput();
                    stepOutput.setName(stepInfo.getName());
                    stepOutput.setDescription(stepInfo.getDescription());
                    stepOutput.setFlowSequence(stepInfo.getSequence());
                    stepOutput.setStatus(stepFlowMassage.getStatus() == StepReturnStatus.SUCCESSFUL);
                    stepOutput.setMessage(stepFlowMassage.getMessage());
                    stepOutput.setStartDate(stepFlowMassage.getStartDate());
                    stepOutput.setEndDate(stepFlowMassage.getEndDate());
                    sagaStepExecutionLogOutput.getStepOutputs().add(stepOutput);
                });
            }
            switch (stepExecutionLogType) {
                case CUSTOM: {
                    initializeSagaStepExecutionLogService().log(sagaStepExecutionLogOutput);
                }
                case DB: {
                    logger.info("Executing log DB");
                }
            }
        }
    }


}
