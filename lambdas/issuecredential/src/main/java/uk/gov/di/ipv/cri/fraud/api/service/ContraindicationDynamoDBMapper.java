package uk.gov.di.ipv.cri.fraud.api.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.fraud.api.persistence.item.ContraindicationMappingItem;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

class ContraindicationDynamoDBMapper implements ContraindicationMapper {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<String, String> contraindicationMappings;
    private final DataStore<ContraindicationMappingItem> dataStore;

    ContraindicationDynamoDBMapper(
            DataStore<ContraindicationMappingItem> dataStore, String thirdPartyId) {
        this.dataStore = dataStore;
        this.contraindicationMappings = getContraindicationMappings(thirdPartyId);
    }

    public String[] mapThirdPartyFraudCodes(String[] thirdPartyFraudCodes) {
        Objects.requireNonNull(thirdPartyFraudCodes, "thirdPartyFraudCodes must not be null");

        if (thirdPartyFraudCodes.length == 0) {
            return new String[] {};
        }

        List<String> contraindicators =
                Arrays.stream(thirdPartyFraudCodes)
                        .filter(this.contraindicationMappings::containsKey)
                        .map(this.contraindicationMappings::get)
                        .collect(Collectors.toList());

        if (contraindicators.size() != thirdPartyFraudCodes.length) {
            String[] unmappedFraudCodes =
                    Arrays.stream(thirdPartyFraudCodes)
                            .filter(fraudCode -> !contraindicators.contains(fraudCode))
                            .toArray(String[]::new);

            String unmappedFraudCodesAsString = String.join(", ", unmappedFraudCodes);

            LOGGER.warn("Unmapped fraud codes encountered: {}", unmappedFraudCodesAsString);
        }

        return contraindicators.toArray(String[]::new);
    }

    private Map<String, String> getContraindicationMappings(String thirdPartyId) {
        List<ContraindicationMappingItem> contraindicationMappingItems =
                dataStore.getItemsByAttribute("thirdPartyId", thirdPartyId);

        return contraindicationMappingItems.stream()
                .collect(
                        Collectors.toMap(
                                ContraindicationMappingItem::getThirdPartyFraudCode,
                                ContraindicationMappingItem::getContraindicationCode));
    }
}
