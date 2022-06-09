package uk.gov.di.ipv.cri.fraud.api.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.parameters.ParamManager;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.fraud.api.persistence.item.FraudResultItem;

import java.util.UUID;

public class FraudRetrievalService {
    private static final Logger LOGGER = LogManager.getLogger();

    private final DataStore<FraudResultItem> dataStore;
    private final ConfigurationService configurationService;

    FraudRetrievalService(
            DataStore<FraudResultItem> dataStore, ConfigurationService configurationService) {
        this.configurationService = configurationService;
        this.dataStore = dataStore;
    }

    public FraudRetrievalService() {
        this.configurationService =
                new ConfigurationService(
                        ParamManager.getSecretsProvider(),
                        ParamManager.getSsmProvider(),
                        System.getenv("ENVIRONMENT"));
        this.dataStore =
                new DataStore<FraudResultItem>(
                        configurationService.getFraudResultTableName(),
                        FraudResultItem.class,
                        DataStore.getClient());
    }

    public FraudResultItem getFraudResult(UUID sessionId) {
        return dataStore.getItem(sessionId.toString());
    }
}
