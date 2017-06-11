package com.neladyn.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service
public class RedisRepository {

    private static Logger LOGGER = LoggerFactory.getLogger(RedisRepository.class);

    private Jedis jedis;
    private List<String> allKeys;

    public RedisRepository(){
        LOGGER.info("Initializing redis repository");
        jedis = new Jedis("localhost");
    }


    public String getKey(String key) {
        LOGGER.info("Getting key: {}",key);
        return jedis.get(key);
    }

    public void deleteKey(String key) {
        Boolean exists = jedis.exists(key);
        if( !exists ){
            LOGGER.info("Key {} does not exist");
            return;
        }
        jedis.del(key);
    }

    public List<String> getAllKeys() {
        return new LinkedList<String>(jedis.keys("*"));
    }
}
