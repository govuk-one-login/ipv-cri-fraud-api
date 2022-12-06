package gov.di_ipv_fraud.utilities;

import java.io.FileInputStream;
import java.util.Properties;

/** reads the properties file configuration.properties */
public class ConfigurationReader {

    private static Properties properties;

    static {
        try {
            String path = "configuration.properties";
            FileInputStream input = new FileInputStream(path);
            properties = new Properties();
            properties.load(input);

            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String keyName) {
        return properties.getProperty(keyName);
    }

    public static String getBrowser() {
        return System.getenv("BROWSER") != null ? System.getenv("BROWSER") : "chrome";
    }

    public static boolean noChromeSandbox() {
        return "true".equalsIgnoreCase(System.getenv("NO_CHROME_SANDBOX"));
    }

    public static String getOrchestratorUrl() {
        return getEnvironmentVariableOrError("ORCHESTRATOR_STUB_URL");
    }

    private static String getEnvironmentVariableOrError(String variable) {
        String value = System.getenv(variable);
        if (value == null) {
            throw new IllegalArgumentException(
                    String.format("Environment variable %s is not set", variable));
        }
        return value;
    }
}
