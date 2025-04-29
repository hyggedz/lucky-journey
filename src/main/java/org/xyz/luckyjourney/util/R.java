package org.xyz.luckyjourney.util;

import lombok.Data;

import java.text.MessageFormat;

@Data
public class R<T> {
    private static final Long serialVersionUID = 22L;

    private T type;

    //响应码，状态，消息，数据，计数(分页)
    private int code;

    private Boolean state;

    private String message;

    private Object data;

    private long count;

    public R(){}

    public static R ok(){
        R r = new R();
        r.setCode(0);
        r.setState(true);
        r.setMessage("成功");
        return r;
    }

    public static R error(){
        R r = new R();
        r.setCode(201);
        r.setState(false);
        r.setMessage("失败");
        return r;
    }

    public R count(long c){
        this.count = c;
        return this;
    }

    public R code(int c){
        this.code = c;
        return this;
    }

    public R data(Object res){
        this.data = res;
        return this;
    }

    public R state(Boolean s){
        this.state = s;
        return this;
    }

    public R message(String mess){
        this.message = mess;
        return this;
    }

    public R message(String mess,Object... objects){
        this.message = MessageFormat.format(mess,objects);
        return this;
    }
}
