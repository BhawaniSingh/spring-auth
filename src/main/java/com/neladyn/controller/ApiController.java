package com.neladyn.controller;

import com.neladyn.repository.RedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private RedisRepository redisRepository;

    @GetMapping(value = "/api")
    public List<String> getKeys(){
        LOGGER.info("Get keys endpoint");
        return redisRepository.getAllKeys();
    }

    @GetMapping(value = "/api/{key}")
    public ResponseEntity getKey(@PathVariable("key") String key){
        return ResponseEntity.ok(redisRepository.getKey(key));
    }

    @DeleteMapping(value = "/api/{key}")
    public ResponseEntity deleteKey(@PathVariable("key") String key){
        redisRepository.deleteKey(key);
        return ResponseEntity.ok().build();
    }
}
