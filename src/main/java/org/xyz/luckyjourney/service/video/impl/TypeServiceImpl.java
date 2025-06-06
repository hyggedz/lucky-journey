package org.xyz.luckyjourney.service.video.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xyz.luckyjourney.entity.video.Type;
import org.xyz.luckyjourney.mapper.video.TypeMapper;
import org.xyz.luckyjourney.service.video.TypeService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TypeServiceImpl extends ServiceImpl<TypeMapper, Type> implements TypeService {

    @Override
    public List<String> random10Labels() {
        List<String> labels = new ArrayList<>();
        List<Type> list = list(null);
        Collections.shuffle(list);
        for(Type type : list){
            for(String label : type.buildLabel()){
                if(labels.size() == 10){
                    return labels;
                }
                labels.add(label);
            }
        }
        return labels;
    }
}
