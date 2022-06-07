package uk.gov.di.ipv.cri.fraud.api.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Base64;
import java.util.Objects;

class SSLContextFactory {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String DEFAULT_SSL_CONTEXT_PROTOCOL = "TLSv1.2";
    private static final String DEFAULT_KEYSTORE_TYPE = "pkcs12";

    private final SSLContext sslContext;

    SSLContextFactory(String encodedKeyStore, String keyStorePassword) throws IOException {
        Objects.requireNonNull(encodedKeyStore, "encodedKeyStore must not be null");
        Objects.requireNonNull(keyStorePassword, "keyStorePassword must not be null");

        if (encodedKeyStore.isBlank() || encodedKeyStore.isEmpty()) {
            throw new IllegalArgumentException("encodedKeyStore must not be blank or empty");
        }
        if (keyStorePassword.isBlank() || keyStorePassword.isEmpty()) {
            throw new IllegalArgumentException("keyStorePassword must not be blank or empty");
        }

        Path keyStorePath = null;
        try {
            keyStorePath = persistKeystoreToFile(encodedKeyStore);
            this.sslContext = createSSLContext(keyStorePath.toString(), keyStorePassword);
        } finally {
            Files.deleteIfExists(keyStorePath);
        }
    }

    SSLContext getSSLContext() {
        return this.sslContext;
    }

    private SSLContext createSSLContext(String keyStorePath, String keyStorePassword) {
        try {
            KeyStore keyStore =
                    KeyStore.Builder.newInstance(
                                    DEFAULT_KEYSTORE_TYPE,
                                    null,
                                    new File(keyStorePath),
                                    new KeyStore.PasswordProtection(keyStorePassword.toCharArray()))
                            .getKeyStore();

            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

            SSLContext defaultSSLContext = SSLContext.getInstance(DEFAULT_SSL_CONTEXT_PROTOCOL);
            defaultSSLContext.init(keyManagerFactory.getKeyManagers(), null, null);
            return defaultSSLContext;
        } catch (Exception e) {
            LOGGER.error(
                    "An error occurred when initialising an SSLContext with the given keystore", e);
            return null;
        }
    }

    private Path persistKeystoreToFile(String encodedKeyStore) throws IOException {
        Path tempFile = Files.createTempFile(null, null);
        Files.write(tempFile, Base64.getDecoder().decode(encodedKeyStore));
        return tempFile;
    }
}
