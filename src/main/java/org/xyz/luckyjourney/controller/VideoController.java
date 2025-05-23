package org.xyz.luckyjourney.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xyz.luckyjourney.service.user.FavoritesService;
import org.xyz.luckyjourney.service.video.VideoService;
import org.xyz.luckyjourney.util.R;

@RestController
@RequestMapping("/luckyjourney/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @PostMapping("/favorites/{fid}/{vid}")
    public R favoritesVideo(@PathVariable Long fid, @PathVariable Long vid){
        String msg = videoService.favoritesVideo(fid,vid) ? "已收藏" : "已删除";
        return R.ok().message(msg);
    }
}
