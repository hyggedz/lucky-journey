package org.xyz.luckyjourney.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xyz.luckyjourney.entity.user.Favorites;
import org.xyz.luckyjourney.exception.BaseException;
import org.xyz.luckyjourney.mapper.user.FavoritesMapper;
import org.xyz.luckyjourney.service.user.FavoritesService;
import org.xyz.luckyjourney.service.user.FavoritesVideoService;

@Service
public class FavoritesServiceImpl extends ServiceImpl<FavoritesMapper, Favorites> implements FavoritesService {

    @Autowired
    private FavoritesVideoService favoritesVideoService;

    @Override
    public void exist(Long userId, Long defaultFavoritesId) {
        final long count = count(new LambdaQueryWrapper<Favorites>().eq(Favorites::getUserId,userId).eq(Favorites::getId,defaultFavoritesId));
        if(count == 0){
            throw new BaseException("收藏夹选择错误");
        }
    }
}
