package com.aegis.gateway.routing.enums;

public enum RouteMetadataKeys {

    STRIP_PREFIX("aegis.proxy.strip-prefix"),
    PREFIX_PATH("aegis.proxy.prefix-path"),
    REWRITE_PATH_REGEX("aegis.proxy.rewrite-path.regex"),
    REWRITE_PATH_REPLACEMENT("aegis.proxy.rewrite-path.replacement"),
    PRESERVE_QUERY("aegis.proxy.preserve-query");

    final String val;

    RouteMetadataKeys(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }
}
