package gov.di_ipv_fraud.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class TestUtils {

    private static final Logger LOGGER = LogManager.getLogger();

    public static String getProperty(String propertyName) {
        String property = System.getProperty(propertyName);
        return Objects.requireNonNullElse(property, "");
    }
}
