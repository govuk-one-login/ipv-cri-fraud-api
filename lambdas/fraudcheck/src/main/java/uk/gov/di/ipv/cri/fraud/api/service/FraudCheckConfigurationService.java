package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.ipv.cri.fraud.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.fraud.library.service.parameterstore.ParameterPrefix;
import uk.gov.di.ipv.cri.fraud.library.strategy.Strategy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FraudCheckConfigurationService {
    // Parameter Keys
    public static final String CONTRAINDICATION_MAPPINGS_PARAMETER_KEY = "contraindicationMappings";
    public static final String ZERO_SCORE_UCODES_PARAMETER_KEY = "zeroScoreUcodes";
    public static final String NO_FILE_FOUND_THRESHOLD_PARAMETER_KEY = "noFileFoundThreshold";

    private final String contraindicationMappings;

    private final List<String> zeroScoreUcodes;

    private final ParameterStoreService parameterStoreService;
    private final ObjectMapper objectMapper;

    private final CrosscoreV2Configuration crosscoreV2Configuration;

    public FraudCheckConfigurationService(
            ParameterStoreService parameterStoreService, ObjectMapper objectMapper)
            throws JsonProcessingException {

        this.parameterStoreService = parameterStoreService;
        this.objectMapper = objectMapper;

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

    public Integer getNoFileFoundThreshold(Strategy strategy) {

        if (strategy == Strategy.NO_CHANGE) {
            return Integer.parseInt(
                    parameterStoreService.getParameterValue(
                            ParameterPrefix.OVERRIDE, NO_FILE_FOUND_THRESHOLD_PARAMETER_KEY));
        } else {
            // Temporary until all CRI parameters are place under the same path
            String jsonString =
                    parameterStoreService.getParameterValue(
                            ParameterPrefix.OVERRIDE,
                            "testStrategy/" + NO_FILE_FOUND_THRESHOLD_PARAMETER_KEY);

            TypeReference<HashMap<String, String>> typeRef = new TypeReference<>() {};

            HashMap<String, String> map = null;
            try {
                map = objectMapper.readValue(jsonString, typeRef);
            } catch (JsonProcessingException e) {
                // Avoids all callers needing to handle json processing
                // As below this indicates the parameter is not there.
                return null;
            }

            // Get the associated value from the map
            return Integer.parseInt(map.get(strategy.name()));
        }
    }
}
