package org.xyz.luckyjourney.service.video.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jdk.nashorn.internal.runtime.regexp.joni.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.xyz.luckyjourney.constant.AuditStatus;
import org.xyz.luckyjourney.entity.File;
import org.xyz.luckyjourney.entity.user.User;
import org.xyz.luckyjourney.entity.video.Video;
import org.xyz.luckyjourney.entity.vo.BasePage;
import org.xyz.luckyjourney.entity.vo.UserVO;
import org.xyz.luckyjourney.exception.BaseException;
import org.xyz.luckyjourney.holder.UserHolder;
import org.xyz.luckyjourney.mapper.video.VideoMapper;
import org.xyz.luckyjourney.service.FileService;
import org.xyz.luckyjourney.service.user.FavoritesService;
import org.xyz.luckyjourney.service.user.UserService;
import org.xyz.luckyjourney.service.video.VideoService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    @Autowired
    private FavoritesService favoritesService;
    private VideoService videoService;

    @Override
    public IPage<Video> listByUserIdOpenVideo(Long userId, BasePage basePage) {
        //校验 userId
        if(userId == null){
            return new Page<>();
        }

        //查询视频
        IPage<Video> page = page(basePage.page(), new LambdaQueryWrapper<Video>().eq(Video::getUserId, userId)
                .eq(Video::getAuditStatus, AuditStatus.SUCCESS)
                .orderByDesc(Video::getGmtCreated));

        List<Video> videos = page.getRecords();

        //enrich 视频信息
        setUserVOAndUrl(videos);
        return page;
    }

    @Override
    public boolean favoritesVideo(Long fid, Long vid) {
        Video video = getById(vid);
        if(video == null){
            throw  new BaseException("视频不存在");
        }

        boolean favorites =  favoritesService.favoritesVideo(fid,vid);
        updateFavorites(video,favorites ? 1L : -1L);

        //TODO 标签生成，更改推流

        return favorites;
    }

    @Override
    public Collection<Video> listByFavoritesId(Long favoritesId) {
        //获取视频id
        List<Long> videoIds = favoritesService.listByFavoritesId(favoritesId, UserHolder.get());
        if(ObjectUtils.isEmpty(videoIds)){
            return Collections.EMPTY_LIST;
        }

        //获取视频
        List<Video> videos = videoService.listByIds(videoIds);

        //填充信息
        setUserVOAndUrl(videos);
        return videos;
    }

    /**
     * 根据id （userId,urlId,coverId） 填充用户信息和文件信息
     *
     * @param videos
     */
    private void setUserVOAndUrl(Collection<Video> videos){
        if(!ObjectUtils.isEmpty(videos)){
            Set<Long> userIds = new HashSet<>();
            ArrayList<Long> fileIds = new ArrayList<>();

            for(Video video : videos){
                userIds.add(video.getUserId());
                fileIds.add(video.getUrl());
                fileIds.add(video.getCover());
            }

            Map<Long, File> fileMap = fileService.listByIds(fileIds).stream().collect(Collectors.toMap(File::getId, Function.identity()));
            Map<Long, User> userMap = userService.list(userIds).stream().collect(Collectors.toMap(User::getId,Function.identity()));

            for(Video video : videos){
                final UserVO  userVO = new UserVO();
                final User user = userMap.get(video.getUserId());

                userVO.setId(video.getUserId());
                userVO.setSex(user.getSex());
                userVO.setNickName(user.getNickName());
                userVO.setDescription(user.getDescription());
                video.setUser(userVO);

                final File file  = fileMap.get(video.getUrl());
                video.setVideoType(file.getFormat());
            }
        }
    }

    void updateFavorites(Video video,Long value){
        UpdateWrapper<Video> favoritesUpdateWrapper = new UpdateWrapper<>();
        favoritesUpdateWrapper.setSql("favorites_count = favorites_count + " + value);
        favoritesUpdateWrapper.lambda().eq(Video::getId,video.getId()).eq(Video::getFavoritesCount,video.getFavoritesCount());
        update(video,favoritesUpdateWrapper);
    }
}
