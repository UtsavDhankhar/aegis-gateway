local key = KEYS[1]

local capacity = tonumber(ARGV[1])
local refill_tokens = tonumber(ARGV[2])
local refill_period_ms = tonumber(ARGV[3])
local requested_tokens = tonumber(ARGV[4])

local redis_time = redis.call("TIME")

local now_ms = redis_time[1] * 1000 + math.floor(redis_time[2] / 1000)

local state = redis.call("HMGET", key, "tokens", "last_refill_ms")

local tokens = tonumber(state[1])
local last_refill_ms = tonumber(state[2])

if tokens == nil then
    tokens = capacity
end

if last_refill_ms == nil then
    last_refill_ms = now_ms
end

local elapsed_ms = math.max(0, now_ms - last_refill_ms)

local refill_rate = refill_tokens / refill_period_ms

local refilled_tokens = elapsed_ms * refill_rate

tokens = math.min(capacity, tokens + refilled_tokens)

local allowed = 0
local retry_after_ms = 0

if tokens >= requested_tokens then

    tokens = tokens - requested_tokens

    allowed = 1

else

    local missing_tokens = requested_tokens - tokens

    retry_after_ms = math.ceil(
            missing_tokens / refill_rate
    )

end

redis.call("HSET", key, "tokens", tokens, "last_refill_ms", now_ms)

local full_refill_time_ms = math.ceil(capacity / refill_rate)

redis.call("PEXPIRE", key, full_refill_time_ms * 2)

return tostring(allowed)
        .. "|"
        .. tostring(math.floor(tokens))
        .. "|"
        .. tostring(retry_after_ms)