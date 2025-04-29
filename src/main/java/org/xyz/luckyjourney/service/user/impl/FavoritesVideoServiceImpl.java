package org.xyz.luckyjourney.service.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xyz.luckyjourney.entity.user.FavoritesVideo;
import org.xyz.luckyjourney.mapper.user.FavoritesVideoMapper;
import org.xyz.luckyjourney.service.user.FavoritesVideoService;

@Service
public class FavoritesVideoServiceImpl extends ServiceImpl<FavoritesVideoMapper, FavoritesVideo> implements FavoritesVideoService {

}
