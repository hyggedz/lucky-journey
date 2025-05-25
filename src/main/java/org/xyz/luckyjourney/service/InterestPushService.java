package org.xyz.luckyjourney.service;

import org.springframework.stereotype.Service;

import java.util.List;


public interface InterestPushService {
    void initUserModel(Long userId, List<String> labels);
}
