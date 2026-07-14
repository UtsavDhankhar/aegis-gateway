package com.aegis.gateway.proxy.pathRewrite;

import com.aegis.gateway.context.GatewayRequest;
import com.aegis.gateway.routing.RouteDefinition;
import com.aegis.gateway.routing.RouteMetadataAccessor;
import com.aegis.gateway.routing.exceptions.InvalidRouteMetadataException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.aegis.gateway.routing.enums.RouteMetadataKeys.PREFIX_PATH;
import static com.aegis.gateway.routing.enums.RouteMetadataKeys.REWRITE_PATH_REGEX;
import static com.aegis.gateway.routing.enums.RouteMetadataKeys.REWRITE_PATH_REPLACEMENT;
import static com.aegis.gateway.routing.enums.RouteMetadataKeys.STRIP_PREFIX;

@Component
public class DefaultPathRewrite implements PathRewriteStrategy {

    private final RouteMetadataAccessor metadataAccessor;

    public DefaultPathRewrite(RouteMetadataAccessor metadataAccessor) {
        this.metadataAccessor = metadataAccessor;
    }

    @Override
    public String rewritePath(RouteDefinition route, GatewayRequest request) {

        String originalPath = request.uri().getRawPath();

        Optional<String> rewriteRegex = metadataAccessor.getString(route, REWRITE_PATH_REGEX);

        Optional<String> rewriteReplacement = metadataAccessor.getString(route, REWRITE_PATH_REPLACEMENT);

        if (rewriteRegex.isPresent() || rewriteReplacement.isPresent()) {
            return rewriteUsingRegex(route, originalPath, rewriteRegex, rewriteReplacement);
        }

        String rewrittenPath = originalPath;

        Optional<String> stripPrefix = metadataAccessor.getString(route, STRIP_PREFIX);
        if (stripPrefix.isPresent()) {
            rewrittenPath = stripPrefix(rewrittenPath, stripPrefix.get());
        }

        Optional<String> prefixPath = metadataAccessor.getString(route, PREFIX_PATH);
        if (prefixPath.isPresent()) {
            rewrittenPath = prefixPath(rewrittenPath, prefixPath.get());
        }

        return normalizePath(rewrittenPath);
    }

    private String rewriteUsingRegex(RouteDefinition route, String originalPath,
                                     Optional<String> rewriteRegex, Optional<String> rewriteReplacement) {

        if (rewriteRegex.isEmpty() || rewriteReplacement.isEmpty()) {
            throw new InvalidRouteMetadataException("Route '%s' must define both rewrite regex and replacement".formatted(route.getId()));
        }

        try {

            Pattern pattern = Pattern.compile(rewriteRegex.get());
            String rewrittenPath = pattern.matcher(originalPath).replaceFirst(rewriteReplacement.get());

            return normalizePath(rewrittenPath);

        } catch (PatternSyntaxException exception) {
            throw new InvalidRouteMetadataException("Route '%s' has invalid rewrite regex: %s".formatted(route.getId(), rewriteRegex.get()), exception);
        } catch (IllegalArgumentException exception) {
            throw new InvalidRouteMetadataException("Route '%s' has invalid rewrite replacement: %s".formatted(route.getId(), rewriteReplacement.get()), exception);
        }
    }

    private String stripPrefix(String path, String prefix) {

        String normalizedPrefix = normalizePrefix(prefix);

        if (normalizedPrefix.equals("/")) {
            return path;
        }

        if (path.equals(normalizedPrefix)) {
            return "/";
        }

        if (path.startsWith(normalizedPrefix + "/")) {
            return path.substring(normalizedPrefix.length());
        }

        return path;
    }

    private String prefixPath(String path, String prefix) {
        String normalizedPrefix = normalizePrefix(prefix);

        if (normalizedPrefix.equals("/")) {
            return path;
        }

        if (path.equals("/")) {
            return normalizedPrefix;
        }

        return normalizedPrefix + path;
    }

    private String normalizePrefix(String prefix) {
        String normalized = prefix.trim();

        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }

        if (!path.startsWith("/")) {
            return "/" + path;
        }

        return path;
    }
}

