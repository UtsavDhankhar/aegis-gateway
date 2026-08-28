package com.aegis.gateway.context;

public class GatewayContextAttributes {

    private GatewayContextAttributes(){}

    public static final String REQUEST_ID = "aegis.request.id";
    public static final String SELECTED_ROUTE = "aegis.route.selected";
    public static final String AUTHENTICATED_PRINCIPAL = "aegis.security.authenticated-principal";
    public static final String RATE_LIMIT_DECISION = "aegis.ratelimit.decision";
    public static final String SELECTED_SERVICE_INSTANCE = "aegis.loadbalancer.selected-instance";
}
