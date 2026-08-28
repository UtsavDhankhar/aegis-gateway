package com.aegis.gateway.ratelimit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RateLimitRedisConfig {

    @Bean
    RedisScript<String> tokenBucketScript() {
        return RedisScript.of(new ClassPathResource("scripts/lua/token_bucket.lua"), String.class);
    }
}
