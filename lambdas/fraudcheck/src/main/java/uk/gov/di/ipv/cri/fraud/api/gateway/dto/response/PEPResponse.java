package uk.gov.di.ipv.cri.fraud.api.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true, value = "originalRequestData")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PEPResponse extends IdentityVerificationResponse {}
