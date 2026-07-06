package com.aegis.gateway.routing.predicate;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.routing.RoutePredicate;
import org.springframework.http.HttpHeaders;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class HostRoutePredicate implements RoutePredicate {

    private final List<String> hostPatterns;

    public HostRoutePredicate(String... hostPatterns) {
        this.hostPatterns = Arrays.stream(hostPatterns)
                .map(pattern -> pattern.toLowerCase(Locale.ROOT))
                .toList();

        if (this.hostPatterns.isEmpty()) {
            throw new IllegalArgumentException("host predicate requires at least one host pattern");
        }
    }

    @Override
    public String name() {
        return "Host";
    }

    @Override
    public boolean matches(GatewayContext context) {
        HttpHeaders headers = context.request().headers();
        InetSocketAddress host = headers.getHost();

        if (host == null) {
            return false;
        }

        String actualHost = host.getHostString().toLowerCase(Locale.ROOT);

        return hostPatterns.stream()
                .anyMatch(pattern -> matchesPattern(pattern, actualHost));
    }

    private boolean matchesPattern(String pattern, String actualHost) {
        if (pattern.startsWith("*.")) {
            String suffix = pattern.substring(1);
            return actualHost.endsWith(suffix);
        }

        return pattern.equals(actualHost);
    }
}