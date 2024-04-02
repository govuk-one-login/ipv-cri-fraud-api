package uk.gov.di.ipv.cri.fraud.library.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.service.AuditEventFactory;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.library.config.ParameterStoreParameters;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;
import uk.gov.di.ipv.cri.fraud.library.service.parameterstore.ParameterPrefix;

import java.time.Clock;

public class ServiceFactory {

    private final ObjectMapper objectMapper;
    private final EventProbe eventProbe;

    private final ClientFactoryService clientFactoryService;
    private final ParameterStoreService parameterStoreService;

    private final AuditService auditService;
    private final SessionService sessionService;

    private final PersonIdentityService personIdentityService;

    private final ResultItemStorageService<FraudResultItem> resultItemStorageService;

    // Common-Lib
    private final ConfigurationService commonLibConfigurationService;

    public ServiceFactory() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.eventProbe = new EventProbe();

        this.clientFactoryService = new ClientFactoryService();
        this.parameterStoreService = new ParameterStoreService(clientFactoryService);

        this.commonLibConfigurationService =
                new uk.gov.di.ipv.cri.common.library.service.ConfigurationService();

        this.sessionService = new SessionService();
        this.auditService =
                new AuditService(
                        clientFactoryService.getSqsClient(),
                        commonLibConfigurationService,
                        objectMapper,
                        new AuditEventFactory(commonLibConfigurationService, Clock.systemUTC()));
        this.personIdentityService = new PersonIdentityService();

        final String resultItemTableName =
                parameterStoreService.getParameterValue(
                        ParameterPrefix.STACK,
                        ParameterStoreParameters.FRAUD_RESULT_ITEM_TABLE_NAME);
        this.resultItemStorageService =
                new ResultItemStorageService<>(
                        resultItemTableName, FraudResultItem.class, clientFactoryService);
    }

    @ExcludeFromGeneratedCoverageReport
    ServiceFactory(
            EventProbe eventProbe,
            ClientFactoryService clientFactoryService,
            ParameterStoreService parameterStoreService,
            SessionService sessionService,
            AuditService auditService,
            ResultItemStorageService<FraudResultItem> resultItemStorageService,
            PersonIdentityService personIdentityService,
            ConfigurationService commonLibConfigurationService) {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.eventProbe = eventProbe;
        this.clientFactoryService = clientFactoryService;
        this.parameterStoreService = parameterStoreService;
        this.sessionService = sessionService;
        this.auditService = auditService;
        this.resultItemStorageService = resultItemStorageService;
        this.personIdentityService = personIdentityService;
        this.commonLibConfigurationService = commonLibConfigurationService;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public EventProbe getEventProbe() {
        return eventProbe;
    }

    public ClientFactoryService getClientFactoryService() {
        return clientFactoryService;
    }

    public ParameterStoreService getParameterStoreService() {
        return parameterStoreService;
    }

    public AuditService getAuditService() {
        return auditService;
    }

    public SessionService getSessionService() {
        return sessionService;
    }

    public PersonIdentityService getPersonIdentityService() {
        return personIdentityService;
    }

    public ConfigurationService getCommonLibConfigurationService() {
        return commonLibConfigurationService;
    }

    public ResultItemStorageService<FraudResultItem> getResultItemStorageService() {
        return resultItemStorageService;
    }
}
