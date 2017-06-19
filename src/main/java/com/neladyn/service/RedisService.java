package com.neladyn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class RedisService {

    private JedisPool jedisPool;

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisService.class);

    public RedisService(@Value("${redisURL}") String redisURL) throws URISyntaxException {
        LOGGER.info("Initializing redis url={}", redisURL);
        URI redisURI = new URI(redisURL);
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        this.jedisPool = new JedisPool(poolConfig, redisURI);

    }

    public String getValue(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    public void setValue(String key, String value, int exptime) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, exptime, value);
        }
    }

    public void deleteKey(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        }
    }

}

