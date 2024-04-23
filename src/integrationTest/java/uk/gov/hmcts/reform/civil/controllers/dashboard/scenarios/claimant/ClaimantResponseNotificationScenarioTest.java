package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantResponseNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantResponseNotificationScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseNotificationHandler handler;

    @Test
    void shouldCreateNotification_forClaimantWhenClaimantProceedsCarm() throws Exception {
        String caseId = String.valueOf(System.currentTimeMillis());
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .ccdState(CaseState.IN_MEDIATION)
            .build();

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Your claim is now going to mediation"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">Your claim is now going to mediation."
                            + " You will be contacted within 28 days with details of your appointment. "
                            + "<br> If you do not attend your mediation appointment, the judge may issue a penalty.</p>"),
                jsonPath("$[0].titleCy").value("Your claim is now going to mediation"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">Your claim is now going to mediation."
                            + " You will be contacted within 28 days with details of your appointment. "
                            + "<br> If you do not attend your mediation appointment, the judge may issue a penalty.</p>"));
    }

    @Test
    void shouldCreateNotification_forClaimantWhenClaimantProceeds() throws Exception {
        String caseId = String.valueOf(System.currentTimeMillis());
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .ccdState(CaseState.IN_MEDIATION)
            .build();

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("You've rejected the defendant's response"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">You've both agreed to try mediation. "
                               + "Your mediation appointment will be arranged within 28 days.</p><p class=\"govuk-body\">"
                               + "<a href=\"https://www.gov.uk/guidance/small-claims-mediation-service\" "
                               + " rel=\"noopener noreferrer\" class=\"govuk-link\"> Find out more about how mediation works (opens in new tab)</a>.</p>"),
                jsonPath("$[0].titleCy").value("You've rejected the defendant's response"),
                jsonPath("$[0].descriptionCy")
                    .value("<p class=\"govuk-body\">You've both agreed to try mediation. "
                               + "Your mediation appointment will be arranged within 28 days.</p><p class=\"govuk-body\">"
                               + "<a href=\"https://www.gov.uk/guidance/small-claims-mediation-service\" "
                               + " rel=\"noopener noreferrer\" class=\"govuk-link\"> Find out more about how mediation works (opens in new tab)</a>.</p>"));
    }
}