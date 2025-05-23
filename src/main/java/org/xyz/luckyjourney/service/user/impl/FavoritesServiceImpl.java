package org.xyz.luckyjourney.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xyz.luckyjourney.entity.user.Favorites;
import org.xyz.luckyjourney.entity.user.FavoritesVideo;
import org.xyz.luckyjourney.entity.user.User;
import org.xyz.luckyjourney.exception.BaseException;
import org.xyz.luckyjourney.holder.UserHolder;
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

    @Override
    @Transactional
    public void remove(Long id, Long userId) {
        final Favorites favorites = getOne(new LambdaQueryWrapper<Favorites>().eq(Favorites::getId, id).eq(Favorites::getUserId, userId));
        if(favorites.getName().equals("默认收藏夹")){
            throw new BaseException("无法删除默认收藏夹");
        }

        final boolean remove = remove(new LambdaQueryWrapper<Favorites>().eq(Favorites::getUserId, userId).eq(Favorites::getId, id));
        if(remove){
            favoritesVideoService.remove(new LambdaQueryWrapper<FavoritesVideo>().eq(FavoritesVideo::getFavoritesId,id));
        }else {
            throw new BaseException("不能删除别人的收藏夹");
        }
    }

    @Override
    public boolean favoritesVideo(Long fid, Long vid) {
        Long userId = UserHolder.get();
        try{
            FavoritesVideo favoritesVideo = new FavoritesVideo();
            favoritesVideo.setFavoritesId(fid);
            favoritesVideo.setUserId(userId);
            favoritesVideo.setVideoId(vid);
            favoritesVideoService.save(favoritesVideo);
        }catch (Exception e){
            favoritesVideoService.remove(new LambdaQueryWrapper<FavoritesVideo>()
                    .eq(FavoritesVideo::getFavoritesId,fid)
                    .eq(FavoritesVideo::getUserId,userId)
                    .eq(FavoritesVideo::getVideoId,vid));
            return false;
        }
        return true;
    }
}
