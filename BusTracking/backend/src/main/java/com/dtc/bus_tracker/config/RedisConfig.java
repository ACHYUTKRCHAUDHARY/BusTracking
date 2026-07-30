package com.dtc.bus_tracker.config;

import com.dtc.bus_tracker.dto.BusLocationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @SuppressWarnings("deprecation")
    @Bean
    public RedisTemplate<String, BusLocationEvent> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, BusLocationEvent> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use Jackson2JsonRedisSerializer instead of deprecated GenericJackson2JsonRedisSerializer
        org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<BusLocationEvent> serializer = 
            new org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<>(objectMapper(), BusLocationEvent.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }
}