package com.gojek.esb.sink.redis.dataentry;

import com.gojek.esb.sink.redis.ttl.DurationTTL;
import com.gojek.esb.sink.redis.ttl.ExactTimeTTL;
import com.gojek.esb.sink.redis.ttl.NoRedisTTL;
import com.gojek.esb.sink.redis.ttl.RedisTTL;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Pipeline;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RedisListEntryTest {

    @Mock
    private Pipeline pipeline;

    @Mock
    private JedisCluster jedisCluster;

    private RedisTTL redisTTL;
    private RedisListEntry redisListEntry;

    @Before
    public void setup() {
        redisTTL = new NoRedisTTL();
        redisListEntry = new RedisListEntry("test-key", "test-value");
    }

    @Test
    public void shouldIOnlyPushDataWithoutTTLByDefaultForPipeline() {
        redisListEntry.pushMessage(pipeline, redisTTL);

        verify(pipeline, times(1)).lpush("test-key", "test-value");
        verify(pipeline, times(0)).expireAt(any(String.class), any(Long.class));
        verify(pipeline, times(0)).expireAt(any(String.class), any(Long.class));
    }

    @Test
    public void shouldSetProperTTLForExactTimeForPipeline() {
        redisTTL = new ExactTimeTTL(1000L);
        redisListEntry.pushMessage(pipeline, redisTTL);

        verify(pipeline, times(1)).expireAt("test-key", 1000L);
    }

    @Test
    public void shouldSetProperTTLForDurationForPipeline() {
        redisTTL = new DurationTTL(1000);
        redisListEntry.pushMessage(pipeline, redisTTL);

        verify(pipeline, times(1)).expire("test-key", 1000);
    }

    @Test
    public void shouldIOnlyPushDataWithoutTTLByDefaultForCluster() {
        redisListEntry.pushMessage(jedisCluster, redisTTL);

        verify(jedisCluster, times(1)).lpush("test-key", "test-value");
        verify(jedisCluster, times(0)).expireAt(any(String.class), any(Long.class));
        verify(jedisCluster, times(0)).expireAt(any(String.class), any(Long.class));
    }

    @Test
    public void shouldSetProperTTLForExactTimeForCluster() {
        redisTTL = new ExactTimeTTL(1000L);
        redisListEntry.pushMessage(jedisCluster, redisTTL);

        verify(jedisCluster, times(1)).expireAt("test-key", 1000L);
    }

    @Test
    public void shouldSetProperTTLForDurationForCluster() {
        redisTTL = new DurationTTL(1000);
        redisListEntry.pushMessage(jedisCluster, redisTTL);

        verify(jedisCluster, times(1)).expire("test-key", 1000);
    }
}
