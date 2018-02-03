package com.example.SecurityDemo;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {
    private final int MAX_ATTEMPT = 10;
    private LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService(){
        super();
        attemptsCache =  Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) throws Exception {
                        return 0;
                    }
                });
    }

    public void loginSucceeded(String key){
        attemptsCache.invalidate(key);
    }

    public void loginFailed(String key){
        int attempts = 0;
        try{
            attempts = attemptsCache.get(key);
        }catch (Exception e){
            attempts = 0;
        }
        attempts++;
        attemptsCache.put(key,attempts);
    }

    public boolean isBlocked(String key){
        try{
            return attemptsCache.get(key)>= MAX_ATTEMPT;
        }catch (Exception e){
            return false;
        }
    }


}
