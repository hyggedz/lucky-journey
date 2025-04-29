package org.xyz.luckyjourney.constant;

import java.util.HashMap;
import java.util.Map;

public class AuditStatus {
    public static Integer SUCCESS = 0; //成功
    public static Integer PROCESS = 1; //审核中
    public static Integer PASS = 2;    //失败
    public static Integer MANUAL = 3;  //需要人工审核

    private static Map<Integer,String> map = new HashMap<>();

    static {
        map.put(SUCCESS,"成功");
        map.put(PROCESS,"审核中");
        map.put(PASS,"失败");
        map.put(MANUAL,"需要人工审核");
    }

    public static String getName(Integer audit){
        return map.get(audit);
    }
}
