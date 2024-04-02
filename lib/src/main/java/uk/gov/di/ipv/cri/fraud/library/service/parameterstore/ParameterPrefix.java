package uk.gov.di.ipv.cri.fraud.library.service.parameterstore;

import java.util.Optional;

public enum ParameterPrefix {
    OVERRIDE(
            Optional.ofNullable(System.getenv("PARAMETER_PREFIX"))
                    .orElse(System.getenv("AWS_STACK_NAME"))),
    STACK(System.getenv("AWS_STACK_NAME")),
    COMMON_API(System.getenv("COMMON_PARAMETER_NAME_PREFIX")),
    ENV(System.getenv("ENVIRONMENT"));

    private final String prefixValue;

    ParameterPrefix(String prefixValue) {
        this.prefixValue = prefixValue;
    }

    public String getPrefixValue() {
        return prefixValue;
    }
}
