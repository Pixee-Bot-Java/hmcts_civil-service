package uk.gov.hmcts.reform.civil.enums;

import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public enum MultiPartyScenario {

    ONE_V_ONE,
    ONE_V_TWO_ONE_LEGAL_REP,
    ONE_V_TWO_TWO_LEGAL_REP;

    public static MultiPartyScenario getMultiPartyScenario(CaseData caseData) {

        if (caseData.getRespondent2() != null) {
            return  (caseData.getRespondent2SameLegalRepresentative() == null
                || caseData.getRespondent2SameLegalRepresentative().equals(NO))
                ? ONE_V_TWO_TWO_LEGAL_REP
                : ONE_V_TWO_ONE_LEGAL_REP;
        }

        return ONE_V_ONE;
    }
}
