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
import org.xyz.luckyjourney.entity.video.Type;
import org.xyz.luckyjourney.entity.video.Video;
import org.xyz.luckyjourney.entity.video.VideoStar;
import org.xyz.luckyjourney.entity.vo.BasePage;
import org.xyz.luckyjourney.entity.vo.UserModel;
import org.xyz.luckyjourney.entity.vo.UserVO;
import org.xyz.luckyjourney.exception.BaseException;
import org.xyz.luckyjourney.holder.UserHolder;
import org.xyz.luckyjourney.mapper.video.VideoMapper;
import org.xyz.luckyjourney.service.FileService;
import org.xyz.luckyjourney.service.InterestPushService;
import org.xyz.luckyjourney.service.user.FavoritesService;
import org.xyz.luckyjourney.service.user.UserService;
import org.xyz.luckyjourney.service.video.TypeService;
import org.xyz.luckyjourney.service.video.VideoService;
import org.xyz.luckyjourney.service.video.VideoStarService;

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

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoStarService videoStarService;

    @Autowired
    private InterestPushService interestPushService;
    @Autowired
    private TypeService typeService;

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
        List<Video> videos = this.listByIds(videoIds);

        //填充信息
        setUserVOAndUrl(videos);
        return videos;
    }

    @Override
    public boolean starVideo(Long id) {
        Video video = getById(id);
        if(video == null){
            throw  new BaseException("视频不存在");
        }

        VideoStar videoStar = new VideoStar();
        videoStar.setVideoId(id);
        videoStar.setUserId(UserHolder.get());

        boolean result = videoStarService.starVideo(videoStar);
        updateStar(video,result ? 1L : -1L);

        List<String> labels = video.buildLabel();
        UserModel userModel = UserModel.buildUserModel(labels,video.getId(),1.0);
        interestPushService.updateUserModel(userModel);

        return result;
    }

    @Override
    public List<Video> pushVideos(Long userId) {
        User user = null;
        if(userId != null){
            user = userService.getById(userId);
        }

        Collection<Long> videoIds = interestPushService.listVideoIdByUserModel(user);
        List<Video> videos = new ArrayList<>();

        if(ObjectUtils.isEmpty(videoIds)){
            LambdaQueryWrapper<Video> videoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            videoLambdaQueryWrapper.select(Video::getId);
            videoLambdaQueryWrapper.orderByDesc(Video::getGmtCreated);
            videoLambdaQueryWrapper.last("LIMIT 10");
            List<Object> list = listObjs(videoLambdaQueryWrapper);

            videoIds =  list.stream()
                            .map(obj -> Long.parseLong(String.valueOf(obj)))
                            .collect(Collectors.toList());
        }

        videos = videoService.listByIds(videoIds);
        setUserVOAndUrl(videos);
        return videos;
    }

    @Override
    public Collection<Video> getVideoByTypeId(Long typeId) {
        if(typeId == null) return Collections.EMPTY_LIST;
        Type type = typeService.getById(typeId);
        if(type == null) return Collections.EMPTY_LIST;

        Collection<Long> videoIds = interestPushService.listVideoIdByTypeId(typeId);
        if(ObjectUtils.isEmpty(videoIds)) return Collections.EMPTY_LIST;

        List<Video> videos = videoService.listByIds(videoIds);
        setUserVOAndUrl(videos);

        return videos;
    }

    @Override
    public Collection<Video> listSimiliarVideo(Video video) {
        if(ObjectUtils.isEmpty(video) || ObjectUtils.isEmpty(video.getLabelNames())){
            return Collections.EMPTY_LIST;
        }

        List<String> labels = video.buildLabel();
        List<String> labelNames = new ArrayList<>();
        labelNames.addAll(labels);
        labelNames.addAll(labels);

        Set<Long> videoIds = (Set<Long>) interestPushService.listVideoIdByLabes(labelNames);

        Collection<Video> videos = new ArrayList<>();

        videoIds.remove(video.getId());
        if(!ObjectUtils.isEmpty(videoIds)){
            videos = listByIds(videoIds);
            setUserVOAndUrl(videos);
        }

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

    void updateStar(Video video,Long value){
        UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("star_count = star_count + " + value);
        updateWrapper.lambda().eq(Video::getId,video.getId()).eq(Video::getStartCount,video.getShareCount());
        update(video,updateWrapper);
    }
}
