package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_BUSINESS_PROCESS;
import static uk.gov.hmcts.reform.civil.handler.tasks.EndBusinessProcessTaskHandler.NOT_RETRYABLE_MESSAGE;

@ExtendWith(MockitoExtension.class)
class EndBusinessProcessTaskHandlerTest {

    private static final String CASE_ID = "1234";
    public static final String PROCESS_INSTANCE_ID = "processInstanceId";

    @Mock
    private ExternalTask mockExternalTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    private EndBusinessProcessTaskHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);
        handler = new EndBusinessProcessTaskHandler(coreCaseDataService, caseDetailsConverter,
                                                    objectMapper);
    }

    @BeforeEach
    void init() {
        when(mockExternalTask.getTopicName()).thenReturn("test");
        when(mockExternalTask.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);

        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", CASE_ID,
                "caseEvent", END_BUSINESS_PROCESS
            ));
    }

    @Test
    void shouldNotTriggerEndBusinessProcessCCDEvent_whenCalledMoreThanOnceInSequence() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.FINISHED).build())
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        when(coreCaseDataService.startUpdate(CASE_ID, END_BUSINESS_PROCESS)).thenReturn(startEventResponse);

        CaseDataContent caseDataContentWithFinishedStatus = getCaseDataContent(caseDetails, startEventResponse);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, END_BUSINESS_PROCESS);
        verify(coreCaseDataService, never()).submitUpdate(CASE_ID, caseDataContentWithFinishedStatus);
        verify(externalTaskService).handleFailure(
            eq(mockExternalTask),
            eq(NOT_RETRYABLE_MESSAGE),
            anyString(),
            eq(0),
            eq(1000L)
        );
    }

    @Test
    void shouldTriggerEndBusinessProcessCCDEventAndUpdateBusinessProcessStatusToFinished_whenCalled() {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse startEventResponse = startEventResponse(caseDetails);

        when(coreCaseDataService.startUpdate(CASE_ID, END_BUSINESS_PROCESS)).thenReturn(startEventResponse);
        when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CASE_ID, END_BUSINESS_PROCESS);
        verify(coreCaseDataService).submitUpdate(CASE_ID, getCaseDataContent(caseDetails, startEventResponse));
        verify(externalTaskService).complete(mockExternalTask, null);
    }

    private StartEventResponse startEventResponse(CaseDetails caseDetails) {
        return StartEventResponse.builder()
            .token("1234")
            .eventId(END_BUSINESS_PROCESS.name())
            .caseDetails(caseDetails)
            .build();
    }

    private CaseDataContent getCaseDataContent(CaseDetails caseDetails, StartEventResponse value) {
        caseDetails.getData().put("businessProcess", BusinessProcess.builder()
            .status(BusinessProcessStatus.FINISHED)
            .build());

        return CaseDataContent.builder()
            .eventToken(value.getToken())
            .event(Event.builder()
                       .id(value.getEventId())
                       .build())
            .data(caseDetails.getData())
            .build();
    }
}
