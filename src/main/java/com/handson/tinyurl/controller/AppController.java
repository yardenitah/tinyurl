package com.handson.tinyurl.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handson.tinyurl.model.NewTinyRequest;
import com.handson.tinyurl.service.Redis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Random;

@RestController
public class AppController {
    Random random = new Random();

    private static final int MAX_RETRIES = 6;

    private static final int TINY_LENGTH = 4;
    @Autowired
    Redis redis;
    @Autowired
    ObjectMapper om;
    @Value("${base.url}")
    String baseUrl;

    @RequestMapping(value = "/tiny", method = RequestMethod.POST)
    public String generate(@RequestBody NewTinyRequest request) throws JsonProcessingException {
        String tinyCode = generateTinyCode();
        int i = 0;
        while (!redis.set(tinyCode, om.writeValueAsString(request)) && i < MAX_RETRIES) {
            tinyCode = generateTinyCode();
            i++;
        }
        if (i == MAX_RETRIES) throw new RuntimeException("SPACE IS FULL");
        return baseUrl + tinyCode + "/";
    }

    @RequestMapping(value = "/{tiny}/", method = RequestMethod.GET)
    public ModelAndView getTiny(@PathVariable String tiny) throws JsonProcessingException {
        System.out.println("getRequest for tiny: " + tiny);
        Object tinyRequestStr = redis.get(tiny);
        NewTinyRequest tinyRequest = om.readValue(tinyRequestStr.toString(),NewTinyRequest.class);
        if (tinyRequest.getLongUrl() != null) {
            return new ModelAndView("redirect:" + tinyRequest.getLongUrl());
        } else {
            throw new RuntimeException(tiny + " not found");
        }
    }
    private String generateTinyCode() {
        String charPool = "ABCDEFHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < TINY_LENGTH; i++) {
            res.append(charPool.charAt(random.nextInt(charPool.length())));
        }
        return res.toString();
    }

//    private String generateTinyCode() {
//        StringBuilder res = new StringBuilder();
//        for (int i = 0; i < TINY_LENGTH; i++) {
//            int rand = random.nextInt(62);  // 26 (A-Z) + 26 (a-z) + 10 (0-9) = 62 possible characters
//            if (rand < 26) {
//                res.append((char) ('A' + rand));  // A-Z (65-90)
//            } else if (rand < 52) {
//                res.append((char) ('a' + rand - 26));  // a-z (97-122)
//            } else {
//                res.append((char) ('0' + rand - 52));  // 0-9 (48-57)
//            }
//        }
//        return res.toString();
//    }




}

