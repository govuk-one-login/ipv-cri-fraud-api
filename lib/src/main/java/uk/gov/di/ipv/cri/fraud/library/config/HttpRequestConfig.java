package uk.gov.di.ipv.cri.fraud.library.config;

import lombok.experimental.UtilityClass;
import org.apache.http.client.config.RequestConfig;

@UtilityClass
public class HttpRequestConfig {
    public static RequestConfig getCustomRequestConfig(
            int poolRequestTimeout, int initialConnectionTimeout, int socketReadTimeout) {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(poolRequestTimeout)
                .setConnectTimeout(initialConnectionTimeout)
                .setSocketTimeout(socketReadTimeout)
                .build();
    }
}
