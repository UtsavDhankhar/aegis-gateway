package com.aegis.gateway.ratelimit;

public final class RateLimitMetadataKeys {

    private RateLimitMetadataKeys() {
    }

    public static final String ENABLED = "aegis.ratelimit.enabled";

    public static final String CAPACITY = "aegis.ratelimit.capacity";

    public static final String REFILL_TOKENS = "aegis.ratelimit.refill-tokens";

    public static final String REFILL_PERIOD_MS = "aegis.ratelimit.refill-period-ms";

    public static final String TOKENS_PER_REQUEST = "aegis.ratelimit.tokens-per-request";

    public static final String KEY_RESOLVER = "aegis.ratelimit.key-resolver";

    public static final String FAIL_OPEN = "aegis.ratelimit.fail-open";
}
