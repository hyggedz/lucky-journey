package org.xyz.luckyjourney.holder;

public class UserHolder {



    private static ThreadLocal<Long> userThreadLocal = new ThreadLocal<>();

    public static void set(Object id){
        userThreadLocal.set(Long.valueOf(id.toString()));
    }

    public static Long get(){
        return userThreadLocal.get();
    }

    public static void clear(){
        userThreadLocal.remove();
    }

}
