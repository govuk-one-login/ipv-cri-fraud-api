package uk.gov.di.ipv.cri.fraud.api.service;

import org.apache.http.HttpException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class ThirdPartyCloseableHttpClientFactory {

    public ThirdPartyCloseableHttpClientFactory() {
        /* Intended */
    }

    // SSL context with TLS
    public CloseableHttpClient generateTLSHttpClient() throws HttpException {
        try {
            SSLContext sslContext = SSLContexts.custom().setProtocol("TLSv1.2").build();

            return HttpClients.custom().setSSLContext(sslContext).build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new HttpException(e.getMessage());
        }
    }
}
