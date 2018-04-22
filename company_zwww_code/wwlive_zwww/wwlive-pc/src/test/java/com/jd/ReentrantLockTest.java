package com.jd;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.ArrayUtils;

public class ReentrantLockTest {


	Object obj = new Object();
	public void test10(long millis) {
		String wholeStr = "app=live&name=LIVE0000081&swfurl=nil&flashver=nil&tcurl=rtmp://iwanvi1.uplive.ks-cdn.com/live&call=user_publish&vdoid=";
		String[] params = wholeStr.split("&");

		Lock lock = new ReentrantLock();
		lock.lock();
		try {
			System.out.println("22222222" + " " + Thread.currentThread().getName());
			Thread.sleep(millis);
			if (ArrayUtils.isNotEmpty(params) && params.length > 2) {
				System.out.println(Thread.currentThread().getName() + " " + params[1].split("=")[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		lock.unlock();
	}

	public static void main(String[] args) throws InterruptedException {

		ReentrantLockTest test = new ReentrantLockTest();
		ThreadTest tt = new ThreadTest();
		tt.setMillis(5000);
		tt.setTest(test);
		ThreadTest tt1 = new ThreadTest();
		tt1.setMillis(1000);
		tt1.setTest(test);
		
		tt.start();
		Thread.sleep(1000);
		tt1.start();
	}
	
}

class ThreadTest extends Thread {

	private long millis = 5000;

	ReentrantLockTest test;
	
	public long getMillis() {
		return millis;
	}

	public void setMillis(long millis) {
		this.millis = millis;
	}

	public ReentrantLockTest getTest() {
		return test;
	}

	public void setTest(ReentrantLockTest test) {
		this.test = test;
	}

	@Override
	public void run() {
		System.out.println("1111111111111" + " " + Thread.currentThread().getName());
		test.test10(millis);
	}
	
}


