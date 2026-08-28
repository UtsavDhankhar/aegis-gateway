package com.aegis.gateway.proxy;

import com.aegis.gateway.context.GatewayRequest;
import com.aegis.gateway.proxy.pathRewrite.PathRewriteStrategy;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import com.aegis.gateway.routing.RouteMetadataAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static com.aegis.gateway.routing.enums.RouteMetadataKeys.PRESERVE_QUERY;

@Component
public class BackendUriBuilder {

    private final PathRewriteStrategy pathRewriteStrategy;
    private final RouteMetadataAccessor routeMetadataAccessor;

    public BackendUriBuilder(PathRewriteStrategy pathRewriteStrategy, RouteMetadataAccessor routeMetadataAccessor) {
        this.pathRewriteStrategy = pathRewriteStrategy;
        this.routeMetadataAccessor = routeMetadataAccessor;
    }


    public URI buildBackendUri(RouteDefinition routeDefinition, GatewayRequest gatewayRequest, URI upstreamUri) {

        String rewrittenPath = pathRewriteStrategy.rewritePath(routeDefinition, gatewayRequest);

        boolean preserveQuery = routeMetadataAccessor.getBoolean(routeDefinition, PRESERVE_QUERY.getVal(), true);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUri(upstreamUri)
                .replacePath(rewrittenPath);

        if (preserveQuery) {
            builder.replaceQuery(gatewayRequest.uri().getRawQuery());
        } else {
            builder.replaceQuery(null);
        }

        return builder.build(true).toUri();
    }
}
