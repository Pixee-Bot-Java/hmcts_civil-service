package uk.gov.hmcts.reform.civil;

import au.com.dius.pact.consumer.PactVerificationResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.PrdAdminUserConfiguration;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataMaxEdgeCasesBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataMinEdgeCasesBuilder;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prd.client.OrganisationApi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.matcher.IsValidJson.validateJson;

@Slf4j
@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class,
    EventHistoryMapper.class,
    RoboticsDataMapper.class,
    RoboticsAddressMapper.class,
    OrganisationService.class
})
class RpaContinuousFeedConsumerTest extends BaseRpaTest {

    @Autowired
    RoboticsDataMapper roboticsDataMapper;

    @MockBean
    SendGridClient sendGridClient;
    @MockBean
    OrganisationApi organisationApi;
    @MockBean
    AuthTokenGenerator authTokenGenerator;
    @MockBean
    FeatureToggleService featureToggleService;
    @MockBean
    IdamClient idamClient;
    @MockBean
    PrdAdminUserConfiguration userConfig;

    @BeforeEach
    void setup() {
        when(featureToggleService.isRpaContinuousFeedEnabled()).thenReturn(true);
    }

    @Nested
    class CreateClaimRpaContinuousFeed {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenClaimIssuedAndContinuousFeedEnabled() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.CLAIM_ISSUED)
                .legacyCaseReference("100DC001")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data when claim issued in CCD";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenClaimIssuedAndContinuousFeedEnabled_WithMaximumData() {
            CaseData caseData = CaseDataMaxEdgeCasesBuilder.builder()
                .atStateClaimIssuedMaximumData()
                .legacyCaseReference("100DC002")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data when claim issued in CCD - max limit";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenClaimIssuedAndContinuousFeedEnabled_WithMinimumData() {
            CaseData caseData = CaseDataMinEdgeCasesBuilder.builder()
                .atStateClaimIssuedWithMinimalData()
                .legacyCaseReference("100DC003")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data when claim issued in CCD - min limit";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }

    @Nested
    class NotifyClaimRpaContinuousFeed {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenNotifyClaimAndContinuousFeedEnabled() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.CLAIM_NOTIFIED)
                .legacyCaseReference("100DC004")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data when claimant had notified the defendant";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenClaimAgainstUnregisteredDefendant_WithMaximumData() {
            CaseData caseData = CaseDataMaxEdgeCasesBuilder.builder()
                .atStateClaimNotifiedWithMaximumData()
                .legacyCaseReference("100DC005")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data when claimant had notified the defendant - max limit";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenClaimAgainstUnregisteredDefendantWithMinimumData() {
            CaseData caseData = CaseDataMinEdgeCasesBuilder.builder()
                .atStateClaimNotifiedWithMaximumData()
                .legacyCaseReference("100DC006")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data when claimant had notified the defendant - min limit";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }
}
