package dev.jasser.heartBeatReceiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisHeartBeatStorage implements HeartBeatStorage {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void saveHeartBeat(String nodeId, String heartBeatStatus) {
        stringRedisTemplate.opsForValue().set(nodeId, heartBeatStatus);
    }
    public String getHeartBeatStatus(String nodeId) {
        return stringRedisTemplate.opsForValue().get(nodeId);
    }
}
