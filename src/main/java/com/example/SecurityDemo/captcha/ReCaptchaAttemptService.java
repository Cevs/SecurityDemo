package com.example.SecurityDemo.captcha;


import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service("reCaptchaAttemptService")
public class ReCaptchaAttemptService {
    private final int MAX_ATTEMPT = 4;
    private LoadingCache<String, Integer> attemptsCache;

    public ReCaptchaAttemptService(){
        super();
        attemptsCache = Caffeine.newBuilder()
                .expireAfterWrite(4, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) throws Exception {
                        return 0;
                    }
                });
    }

    public void reCaptchaSucceeded(String key){
        attemptsCache.invalidate(key);
    }

    public void reCaptchaFailed(String key){
        int attempts = attemptsCache.get(key);
        attempts++;
        attemptsCache.put(key,attempts);
    }

    public boolean isBlocked(String key){
        return attemptsCache.get(key) >= MAX_ATTEMPT;
    }
}
