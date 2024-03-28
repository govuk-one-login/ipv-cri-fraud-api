package uk.gov.di.ipv.cri.fraud.api.service;

import uk.gov.di.ipv.cri.fraud.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.fraud.library.service.parameterstore.ParameterPrefix;

import java.util.Arrays;
import java.util.List;

public class FraudCheckConfigurationService {
    // Parameter Keys
    public static final String CONTRAINDICATION_MAPPINGS_PARAMETER_KEY = "contraindicationMappings";
    public static final String ZERO_SCORE_UCODES_PARAMETER_KEY = "zeroScoreUcodes";
    public static final String NO_FILE_FOUND_THRESHOLD_PARAMETER_KEY = "noFileFoundThreshold";

    private final String contraindicationMappings;

    private final List<String> zeroScoreUcodes;
    private final int noFileFoundThreshold;

    private final CrosscoreV2Configuration crosscoreV2Configuration;

    public FraudCheckConfigurationService(ParameterStoreService parameterStoreService) {
        // ****************************  Environment Parameters ****************************

        // None

        // **************************** Prefix Parameters ****************************

        this.contraindicationMappings =
                parameterStoreService.getParameterValue(
                        ParameterPrefix.OVERRIDE, CONTRAINDICATION_MAPPINGS_PARAMETER_KEY);

        final String zeroScoreUcodesParameterValue =
                parameterStoreService.getParameterValue(
                        ParameterPrefix.OVERRIDE, ZERO_SCORE_UCODES_PARAMETER_KEY);
        this.zeroScoreUcodes = Arrays.asList(zeroScoreUcodesParameterValue.split(","));

        this.noFileFoundThreshold =
                Integer.parseInt(
                        parameterStoreService.getParameterValue(
                                ParameterPrefix.OVERRIDE, NO_FILE_FOUND_THRESHOLD_PARAMETER_KEY));

        // *************************CrosscoreV2 Parameters***************************

        this.crosscoreV2Configuration = new CrosscoreV2Configuration(parameterStoreService);
    }

    public String getContraindicationMappings() {
        return contraindicationMappings;
    }

    public CrosscoreV2Configuration getCrosscoreV2Configuration() {
        return crosscoreV2Configuration;
    }

    public List<String> getZeroScoreUcodes() {
        return zeroScoreUcodes;
    }

    public Integer getNoFileFoundThreshold() {
        return noFileFoundThreshold;
    }
}
