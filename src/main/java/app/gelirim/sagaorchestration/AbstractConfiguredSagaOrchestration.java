package app.gelirim.sagaorchestration;

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

public abstract class AbstractConfiguredSagaOrchestration<T> extends AbstractConfiguredSagaOrchestrationClass implements InitializingBean {
    protected final Log logger = LogFactory.getLog(this.getClass());
    private final Class<T> sagaDTOType;
    private final Map<StepInfo, SagaFlow<T>> sagaFlowMap = new HashMap<>();
    private boolean demandSteps = false;


    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    public AbstractConfiguredSagaOrchestration() {
        this.sagaDTOType = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public List<StepInfo> initializeStepOnStartup() {
        return null;
    }

    public List<StepInfo> initializeStepOnDemand() {
        return null;
    }

    public SagaStepExecutionLogService initializeSagaStepExecutionLogService() {
        return null;
    }

    public final T startSagaOrchestration(T sagaDTO) {
        LinkedHashMap<StepInfo, SagaFlow<T>> processSagaFlows = prepareProcessSagaFlows();
        ProcessedFlowOutput processedFlowOutput = startProcessSagaOrchestration(processSagaFlows, sagaDTO);
        RevertedFlowOutput revertedFlowOutput;
        if (processedFlowOutput.getProcessingCount() < processSagaFlows.size()) {
            LinkedHashMap<StepInfo, SagaFlow<T>> revertSagaFlows = prepareRevertSagaFlows(processSagaFlows, processedFlowOutput.getProcessingCount());
            revertedFlowOutput = startRevertSagaOrchestration(revertSagaFlows, sagaDTO);
        }

        return sagaDTO;
    }


    @SuppressWarnings("unchecked")
    @Override
    public final void afterPropertiesSet() throws Exception {
        Map<String, Object> allFlowStepBeans = applicationContext.getBeansWithAnnotation(SagaFlowStep.class);
        for (Map.Entry<String, Object> entry : allFlowStepBeans.entrySet()) {
            Object bean = entry.getValue();
            if (bean instanceof SagaFlow<?> instance) {
                if (this.sagaDTOType == ((ParameterizedType) instance.getClass().getGenericSuperclass()).getActualTypeArguments()[0]) {
                    SagaFlow<T> sagaFlow = (SagaFlow<T>) bean;
                    fillSagaFlowMap(sagaFlow);
                }
            }
        }
        checkStartSagaOrchestration();
        checkExecutingLogService();
    }

    private ProcessedFlowOutput startProcessSagaOrchestration(LinkedHashMap<StepInfo, SagaFlow<T>> processSagaFlows, T sagaDTO) {
        ProcessedFlowOutput processedFlowOutput = new ProcessedFlowOutput();
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
                flowMassage.setMessage(exceptionMessage(exception));
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

    private RevertedFlowOutput startRevertSagaOrchestration(LinkedHashMap<StepInfo, SagaFlow<T>> revertSagaFlows, T sagaDTO) {
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
            } catch (Exception e) {
                flowMassage.setStatus(StepReturnStatus.FAILED);
                flowMassage.setMessage(exceptionMessage(e));
            }
            revertingCount++;
            flowMassage.setEndDate(new Date());
            revertedFlowOutput.getFlowMassageMap().put(stepInfo, flowMassage);
        }
        revertedFlowOutput.setSuccessfulCount(successfulCount);
        revertedFlowOutput.setRevertingCount(revertingCount);
        return revertedFlowOutput;

    }


    private void fillSagaFlowMap(SagaFlow<T> sagaFlow) {
        SagaFlowStep sagaFlowStep = sagaFlow.getClass().getAnnotation(SagaFlowStep.class);
        if (sagaFlowStep != null && sagaFlowStep.groupName().equals(groupName)) {
            StepInfo stepInfo = new StepInfo();
            if (!customInitializeStep) {
                stepInfo = new StepInfo(sagaFlowStep.sequence(), groupName, sagaFlowStep.name(), sagaFlowStep.description(), sagaFlowStep.revertStep(), sagaFlowStep.startRevert());
                sagaFlowMap.put(stepInfo, sagaFlow);
            } else if (startupStep) {
                if (initializeStepOnStartup() == null) {
                    throw SagaOrchestrationException.sagaOrchestrationException("If startup method is selected, the  initializeStepOnStartup method in the " + this.getClass().getName() + " class should return step information.");
                }
                stepInfo = initializeStepOnStartup().stream().filter(step -> step.getName().equals(sagaFlowStep.name())).findFirst()
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
        List<StepInfo> stepInfoList = initializeStepOnDemand();
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

    private String exceptionMessage(Exception e) {
        String message = e.getMessage();
        if (e.getCause() != null) {
            message = e.getCause().getMessage();
        }
        return message;
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
            sagaStepExecutionLogOutput.setRevertingCount(revertedFlowOutput.getRevertingCount());
            sagaStepExecutionLogOutput.setSuccessfulRevertingCount(revertedFlowOutput.getSuccessfulCount());
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
                sagaStepExecutionLogOutput.getStepOutputs().add(stepOutput);
            });
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
