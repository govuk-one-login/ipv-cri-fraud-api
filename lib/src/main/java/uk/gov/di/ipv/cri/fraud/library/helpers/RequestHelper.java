package uk.gov.di.ipv.cri.fraud.library.helpers;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class RequestHelper {

    private RequestHelper() {}

    public static String getHeaderByKey(Map<String, String> headers, String headerKey) {
        if (Objects.isNull(headers)) {
            return null;
        }
        var values =
                headers.entrySet().stream()
                        .filter(e -> headerKey.equalsIgnoreCase(e.getKey()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList());
        if (values.size() == 1) {
            var value = values.get(0);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
