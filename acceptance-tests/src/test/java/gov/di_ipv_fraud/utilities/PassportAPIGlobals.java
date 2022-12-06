package gov.di_ipv_fraud.utilities;

import gov.di_ipv_fraud.service.ConfigurationService;

public class PassportAPIGlobals {

    public String passportAuthUrl;
    public String tokenPostUrl;
    public String credentialGetUrl;
    public String clientId;
    public String grant_type;

    public PassportAPIGlobals() {
        ConfigurationService configurationService =
                new ConfigurationService(System.getenv("ENVIRONMENT"));

        String passportCriUrl = configurationService.getPassportCriUrl();

        tokenPostUrl = configurationService.getPublicApiBaseUrl() + "/token";
        credentialGetUrl = configurationService.getPublicApiBaseUrl() + "/credential";
        grant_type = "authorization_code";
    }
}
