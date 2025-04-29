package org.xyz.luckyjourney.exception;

import lombok.Data;

@Data
public class BaseException extends RuntimeException{

    String msg;

    public BaseException(String message){
        this.msg = message;
    }
}
