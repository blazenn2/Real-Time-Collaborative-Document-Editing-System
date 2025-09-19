package com.blazenn.realtime_document_editing.service;

import com.blazenn.realtime_document_editing.dto.DocumentDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addToCache(String key, Object value, long ttlInMinutes) {
        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(ttlInMinutes));
    }

    public void addMultipleToCache(Map<String, DocumentDTO> map, long ttlInMinutes) {
        redisTemplate.opsForValue().multiSet(map);
        for (String key: map.keySet()) {
            redisTemplate.expire(key, Duration.ofMinutes(ttlInMinutes));
        }
    }

    public Object getFromCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void removeFromCache(String key) {
        redisTemplate.delete(key);
    }
}
