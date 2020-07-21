package com.ch.filesdemo.exception;

/**
 * @description: FileException
 * @author: chang
 * @create: 2020-07-21 16:18
 **/
public class FileException extends RuntimeException{

    public FileException(String message) {
        super(message);
    }

    public FileException(String message, Throwable cause) {
        super(message, cause);
    }

}
