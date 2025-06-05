package org.xyz.luckyjourney.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xyz.luckyjourney.service.user.FavoritesService;
import org.xyz.luckyjourney.service.video.VideoService;
import org.xyz.luckyjourney.util.R;

@RestController
@RequestMapping("/luckyjourney/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    /**
     * 收藏视频
     *
     * @param fid
     * @param vid
     * @return
     */
    @PostMapping("/favorites/{fid}/{vid}")
    public R favoritesVideo(@PathVariable Long fid, @PathVariable Long vid){
        String msg = videoService.favoritesVideo(fid,vid) ? "已收藏" : "已删除";
        return R.ok().message(msg);
    }

    @GetMapping("/favorites/{fid}")
    public R listByFavoritesId(@PathVariable Long fid){
        return R.ok().data(videoService.listByFavoritesId(fid));
    }

    @PostMapping("/star/{id}")
    public R starVideo(@PathVariable Long id){
        String msg = "点赞成功";
        if(!videoService.starVideo(id)){
            msg = "已取消";
        }
        return R.ok().message(msg);
    }
}
