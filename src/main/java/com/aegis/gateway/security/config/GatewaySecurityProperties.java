package com.aegis.gateway.security.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aegis.gateway.security")
public record GatewaySecurityProperties(
        String subjectClaim,
        String rolesClaim,
        String scopesClaim,
        String rolePrefix
) {

    public GatewaySecurityProperties {
        subjectClaim = defaultIfBlank(subjectClaim, "sub");
        rolesClaim = defaultIfBlank(rolesClaim, "roles");
        scopesClaim = defaultIfBlank(scopesClaim, "scope");
        rolePrefix = defaultIfBlank(rolePrefix, "ROLE_");
    }

    private static String defaultIfBlank(
            String value,
            String defaultValue
    ) {
        return value == null || value.isBlank()
                ? defaultValue
                : value.trim();
    }
}
