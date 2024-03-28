package uk.gov.di.ipv.cri.fraud.library.metrics;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

public class Definitions {
    // These completed metrics record all escape routes from the lambdas.
    // OK for expected routes with ERROR being all others
    public static final String LAMBDA_IDENTITY_CHECK_COMPLETED_OK =
            "lambda_identity_check_completed_ok";
    public static final String LAMBDA_IDENTITY_CHECK_COMPLETED_ERROR =
            "lambda_identity_check_completed_error";
    public static final String LAMBDA_ISSUE_CREDENTIAL_COMPLETED_OK =
            "lambda_issue_credential_completed_ok";
    public static final String LAMBDA_ISSUE_CREDENTIAL_COMPLETED_ERROR =
            "lambda_issue_credential_completed_error";

    // Runtime Capture of colds starts as custom metric for monitoring
    public static final String LAMBDA_FRAUD_CHECK_FUNCTION_INIT_DURATION =
            "lambda_fraud_check_function_init_duration";
    public static final String LAMBDA_ISSUE_CREDENTIAL_FUNCTION_INIT_DURATION =
            "lambda_issue_credential_function_init_duration";

    // PersonIdentityValidator
    public static final String PERSON_DETAILS_VALIDATION_PASS = "person_details_validation_pass";
    public static final String PERSON_DETAILS_VALIDATION_FAIL = "person_details_validation_fail";

    // Fraud Check Request
    public static final String FRAUD_CHECK_REQUEST_SUCCEEDED = "fraud_check_request_succeeded";
    public static final String FRAUD_CHECK_REQUEST_FAILED = "fraud_check_request_failed";

    // PEP Check Request
    public static final String PEP_CHECK_REQUEST_SUCCEEDED = "pep_check_request_succeeded";
    public static final String PEP_CHECK_REQUEST_FAILED = "pep_check_request_failed";

    // OverallScore (Score is appended)
    public static final String IDENTITY_CHECK_SCORE_PREFIX = "identity_check_score_";

    // Per response Contra Indicators (CI is Appended)
    public static final String FRAUD_CHECK_CI_PREFIX = "fraud_check_ci_";
    public static final String PEP_CHECK_CI_PREFIX = "pep_check_ci_";

    // Third Party Response Type Fraud
    public static final String THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO =
            "third_party_fraud_response_type_info";
    public static final String THIRD_PARTY_FRAUD_RESPONSE_TYPE_ERROR =
            "third_party_fraud_response_type_error";
    public static final String THIRD_PARTY_FRAUD_RESPONSE_TYPE_UNKNOWN =
            "third_party_fraud_response_type_unknown";

    // Third Party Response Type PEP
    public static final String THIRD_PARTY_PEP_RESPONSE_TYPE_INFO =
            "third_party_pep_response_type_info";
    public static final String THIRD_PARTY_PEP_RESPONSE_TYPE_ERROR =
            "third_party_pep_response_type_error";
    public static final String THIRD_PARTY_PEP_RESPONSE_TYPE_UNKNOWN =
            "third_party_pep_response_type_unknown";

    // Third Party Response Latency
    public static final String THIRD_PARTY_FRAUD_RESPONSE_LATENCY_MILLIS =
            "third_party_fraud_response_latency";
    public static final String THIRD_PARTY_PEP_RESPONSE_LATENCY_MILLIS =
            "third_party_pep_response_latency";

    // IdentityVerificationInfoResponseValidator Fraud
    public static final String THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO_VALIDATION_PASS =
            "third_party_fraud_response_type_info_validation_pass";
    public static final String THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO_VALIDATION_FAIL =
            "third_party_fraud_response_type_info_validation_fail";

    // IdentityVerificationInfoResponseValidator PEP
    public static final String THIRD_PARTY_PEP_RESPONSE_TYPE_INFO_VALIDATION_PASS =
            "third_party_pep_response_type_info_validation_pass";
    public static final String THIRD_PARTY_PEP_RESPONSE_TYPE_INFO_VALIDATION_FAIL =
            "third_party_pep_response_type_info_validation_fail";

    @ExcludeFromGeneratedCoverageReport
    private Definitions() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }
}
