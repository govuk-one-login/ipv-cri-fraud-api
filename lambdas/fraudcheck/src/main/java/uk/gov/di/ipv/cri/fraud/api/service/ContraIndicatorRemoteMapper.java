package uk.gov.di.ipv.cri.fraud.api.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.parameters.ParamManager;

import java.util.*;
import java.util.stream.Collectors;

public class ContraIndicatorRemoteMapper implements ContraindicationMapper {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String CIMAP = "CIMap";

    private final Map<String, String> uCodeCIMap;
    private ConfigurationService configurationService;

    public ContraIndicatorRemoteMapper() {
        this.configurationService =
                new ConfigurationService(
                        ParamManager.getSecretsProvider(),
                        ParamManager.getSsmProvider(),
                        System.getenv("ENVIRONMENT"));

        final String contraindicatorMappingString =
                System.getenv().get(CIMAP) == null
                        ? configurationService.getContraindicationMappings()
                        : System.getenv().get(CIMAP);

        uCodeCIMap = parseUCodeCIMapString(contraindicatorMappingString);
    }

    /**
     * Secondary constructor to enhance testing. The mapping string can be sent directly to the
     * Mapper.
     *
     * @param mappingString
     */
    public ContraIndicatorRemoteMapper(String mappingString) {
        Objects.requireNonNull(mappingString);

        // Create the map
        uCodeCIMap = parseUCodeCIMapString(mappingString);
    }

    public String[] mapThirdPartyFraudCodes(String[] thirdPartyFraudCodes) {
        Objects.requireNonNull(thirdPartyFraudCodes, "thirdPartyFraudCodes must not be null");

        if (thirdPartyFraudCodes.length == 0) {
            return new String[] {};
        }

        List<String> contraindicators =
                Arrays.stream(thirdPartyFraudCodes)
                        .filter(this.uCodeCIMap::containsKey)
                        .map(this.uCodeCIMap::get)
                        .collect(Collectors.toList());

        if (contraindicators.size() != thirdPartyFraudCodes.length) {
            String[] unmappedFraudCodes =
                    Arrays.stream(thirdPartyFraudCodes)
                            .filter(fraudCode -> !uCodeCIMap.containsKey(fraudCode))
                            .toArray(String[]::new);

            String unmappedFraudCodesAsString = String.join(", ", unmappedFraudCodes);

            LOGGER.warn("Unmapped fraud codes encountered: {}", unmappedFraudCodesAsString);
        }

        // Flattens any duplicate CI Codes
        return Set.copyOf(contraindicators).toArray(String[]::new);
    }

    /**
     * Parses a string representing the uCodeCI Mappings. Example : CIMap=z101,z102:a1#x123:b1
     * Represents : z101 -> a1 z102 -> a1 x123 -> b1 Assumes we never map Ucodes to a second CICode.
     *
     * @param mappingString
     * @return A map of uCode to CI pairs
     */
    private Map<String, String> parseUCodeCIMapString(String mappingString) {

        LOGGER.info("Parsing UCode CI Mapping string...");

        Map<String, String> ucCIMap = new HashMap<>();

        String[] mapDesc = mappingString.split("\\|\\|");

        // Loop through each description and generate the individual mapping.
        for (String desc : mapDesc) {

            List<String> ucodeCIPairs = Arrays.asList(desc.split(":"));

            String[] uCodes = ucodeCIPairs.get(0).split(",");

            // This assumes we never also map to a second CICode
            String ciCode = ucodeCIPairs.get(1);

            for (String uCode : uCodes) {
                ucCIMap.put(uCode, ciCode);
            }
        }

        return ucCIMap;
    }
}
