package org.xyz.luckyjourney.entity.vo;

import lombok.Data;
import org.xyz.luckyjourney.holder.UserHolder;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserModel {
    private List<Model> models;
    private Long userId;


    public static UserModel buildUserModel(List<String> labels,Long videoId,Double score){
        final UserModel userModel = new UserModel();
        userModel.setUserId(UserHolder.get());
        ArrayList<Model> models = new ArrayList<>();
        for(String label : labels){
            Model model = new Model();
            model.setLabel(label);
            model.setVideoId(videoId);
            model.setScore(score);
            models.add(model);
        }
        userModel.setModels(models);
        return userModel;
    }
}
