package com.xqy.gulimall.product.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * @author xqy
 */

/**
 * 在缓存中加入JSON的序列化器，使得缓存中的数据不是乱码而是JSON格式的数据
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class MyCacheConfig {

    @Autowired
    CacheProperties cacheProperties;

    @Bean
    RedisCacheConfiguration redisCacheConfiguration(){

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        CacheProperties.Redis cachePropertiesRedis = cacheProperties.getRedis();
        if (cachePropertiesRedis.getTimeToLive() != null){
            config = config.entryTtl(Duration.ofSeconds(cachePropertiesRedis.getTimeToLive().getSeconds()));
        }
        if (cachePropertiesRedis.getKeyPrefix() != null){
            config = config.prefixKeysWith(cachePropertiesRedis.getKeyPrefix());
        }
        if (!cachePropertiesRedis.isCacheNullValues()){
            config = config.disableCachingNullValues();
        }
        if (!cachePropertiesRedis.isUseKeyPrefix()){
            config = config.disableKeyPrefix();
        }
        return config;
    }
}
