package com.handson.tinyurl.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.handson.tinyurl.service.Redis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AppController {

    @Autowired
    Redis redis;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public Boolean hello(@RequestParam String key) {
        System.out.println(redis.get(key).toString());
        return redis.set(key,key);
    }

}
