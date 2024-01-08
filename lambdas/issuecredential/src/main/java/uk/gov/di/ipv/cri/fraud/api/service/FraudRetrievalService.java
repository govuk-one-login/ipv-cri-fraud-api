package uk.gov.di.ipv.cri.fraud.api.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.parameters.ParamManager;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;

import java.util.UUID;

public class FraudRetrievalService {
    private static final Logger LOGGER = LogManager.getLogger();

    private final DataStore<FraudResultItem> dataStore;
    private final IssueCredentialConfigurationService issueCredentialConfigurationService;

    FraudRetrievalService(
            DataStore<FraudResultItem> dataStore,
            IssueCredentialConfigurationService issueCredentialConfigurationService) {
        this.issueCredentialConfigurationService = issueCredentialConfigurationService;
        this.dataStore = dataStore;
    }

    public FraudRetrievalService() {
        this.issueCredentialConfigurationService =
                new IssueCredentialConfigurationService(
                        ParamManager.getSecretsProvider(),
                        ParamManager.getSsmProvider(),
                        System.getenv("ENVIRONMENT"));
        this.dataStore =
                new DataStore<FraudResultItem>(
                        issueCredentialConfigurationService.getFraudResultTableName(),
                        FraudResultItem.class,
                        DataStore.getClient());
    }

    public FraudResultItem getFraudResult(UUID sessionId) {
        return dataStore.getItem(sessionId.toString());
    }
}
