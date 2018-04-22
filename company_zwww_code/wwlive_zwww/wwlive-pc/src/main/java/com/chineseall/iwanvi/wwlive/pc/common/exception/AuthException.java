package com.chineseall.iwanvi.wwlive.pc.common.exception;

/**
 * Created by kai on 16/5/17.
 */
public class AuthException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String message = "";

    public AuthException() {
        super();
    }

    public AuthException(String message) {
        super(message, null);
        this.message = message;
    }

    public AuthException(String msg, Throwable cause) {
        super(msg, cause);
        this.message = msg;
    }

    public AuthException(Throwable cause) {
        super(cause);
    }

    public AuthException(String message, String exceptionMsg) {
        super(exceptionMsg, null);
        this.message = message;
    }

    public AuthException(String message, String exceptionMsg, Throwable cause) {
        super(exceptionMsg, cause);
        this.message = message;
    }

    public String getMsg() {
        return message;
    }


}
