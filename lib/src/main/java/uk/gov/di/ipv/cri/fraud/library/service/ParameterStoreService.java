package uk.gov.di.ipv.cri.fraud.library.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.parameters.SSMProvider;
import uk.gov.di.ipv.cri.fraud.library.service.parameterstore.ParameterPrefix;

import java.util.Map;

public class ParameterStoreService {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String LOG_MESSAGE_FORMAT = "Method {}, Prefix {}, Path {}, FullPath {}";

    private static final String PARAMETER_NAME_FORMAT = "/%s/%s";

    private final SSMProvider ssmProvider;

    public ParameterStoreService(ClientFactoryService clientFactoryService) {

        this.ssmProvider = clientFactoryService.getSSMProvider();
    }

    public String getParameterValue(ParameterPrefix prefix, String parameterName) {

        String parameterPath =
                String.format(PARAMETER_NAME_FORMAT, prefix.getPrefixValue(), parameterName);

        LOGGER.info(
                LOG_MESSAGE_FORMAT,
                "getParameterValue",
                prefix.getPrefixValue(),
                parameterName,
                parameterPath);

        return ssmProvider.get(parameterPath);
    }

    // Encrypted
    public String getEncryptedParameterValue(ParameterPrefix prefix, String parameterName) {

        String parameterPath =
                String.format(PARAMETER_NAME_FORMAT, prefix.getPrefixValue(), parameterName);

        LOGGER.info(
                LOG_MESSAGE_FORMAT,
                "getEncryptedParameterValue",
                prefix.getPrefixValue(),
                parameterName,
                parameterPath);

        return ssmProvider.withDecryption().get(parameterPath);
    }

    public Map<String, String> getAllParametersFromPath(ParameterPrefix prefix, String path) {

        String parametersPath = String.format(PARAMETER_NAME_FORMAT, prefix.getPrefixValue(), path);

        LOGGER.info(
                LOG_MESSAGE_FORMAT,
                "getAllParametersFromPath",
                prefix.getPrefixValue(),
                path,
                parametersPath);

        return ssmProvider.getMultiple(parametersPath);
    }

    // Encrypted
    public Map<String, String> getAllParametersFromPathWithDecryption(
            ParameterPrefix prefix, String path) {

        String parametersPath = String.format(PARAMETER_NAME_FORMAT, prefix.getPrefixValue(), path);

        LOGGER.info(
                LOG_MESSAGE_FORMAT,
                "getAllParametersFromPathWithDecryption",
                prefix.getPrefixValue(),
                path,
                parametersPath);

        return ssmProvider.withDecryption().getMultiple(parametersPath);
    }
}
