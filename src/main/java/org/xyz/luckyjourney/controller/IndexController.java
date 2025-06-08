package org.xyz.luckyjourney.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xyz.luckyjourney.entity.video.Video;
import org.xyz.luckyjourney.entity.vo.BasePage;
import org.xyz.luckyjourney.service.video.VideoService;
import org.xyz.luckyjourney.util.JwtUtil;
import org.xyz.luckyjourney.util.R;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("luckyjouerney/index")
public class IndexController {

    @Autowired
    private VideoService videoService;

    @GetMapping("/pushVideos")
    public R pushVideos(HttpServletRequest request){
        Long userId = JwtUtil.getUserId(request);
        return R.ok().data(videoService.pushVideos(userId));
    }

    @GetMapping("video/user/{userId}")
    public R listVideoByUserId(@PathVariable Long userId,
                               BasePage basePage, HttpServletRequest request){
        userId = userId == null ? JwtUtil.getUserId(request) : userId;
        return R.ok().data(videoService.listByUserIdOpenVideo(userId,basePage));
    }

    @GetMapping("video/similiar")
    public R pushSimiliarVideos(Video video){
        return R.ok().data(videoService.listSimiliarVideo(video));
    }

    @GetMapping("/video/type/{typeId}")
    public R getVideoByTypeId(@PathVariable Long typeId){
        return R.ok().data(videoService.getVideoByTypeId(typeId));
    }
}
