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

    public void addMultipleToCache(Map<String, DocumentDTO> map, long ttlInMinutes) {
        redisTemplate.opsForValue().multiSet(map);
        for (String key: map.keySet()) {
            redisTemplate.expire(key, Duration.ofMinutes(ttlInMinutes));
        }
    }

    public DocumentDTO getFromCache(String key) {
        return (DocumentDTO) redisTemplate.opsForValue().get(key);
    }
}
