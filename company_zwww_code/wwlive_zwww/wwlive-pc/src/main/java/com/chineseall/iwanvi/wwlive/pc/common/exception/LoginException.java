package com.chineseall.iwanvi.wwlive.pc.common.exception;

/**
 * Created by kai
 */
public class LoginException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 5229643620329859048L;

	public LoginException() {
        super();
    }

    public LoginException(String s) {
        super(s);
    }

    public LoginException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public LoginException(Throwable throwable) {
        super(throwable);
    }
}
