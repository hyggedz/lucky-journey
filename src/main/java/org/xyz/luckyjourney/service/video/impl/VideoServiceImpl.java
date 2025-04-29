package org.xyz.luckyjourney.service.video.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.xyz.luckyjourney.constant.AuditStatus;
import org.xyz.luckyjourney.entity.File;
import org.xyz.luckyjourney.entity.user.User;
import org.xyz.luckyjourney.entity.video.Video;
import org.xyz.luckyjourney.entity.vo.BasePage;
import org.xyz.luckyjourney.entity.vo.UserVO;
import org.xyz.luckyjourney.mapper.video.VideoMapper;
import org.xyz.luckyjourney.service.FileService;
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
}
