package test;

import org.junit.Test;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class TestSpring01 {

	@Test
	public void test1() {
		FileSystemXmlApplicationContext ac = new FileSystemXmlApplicationContext("F:/git-iwanvi/wwlive/wwlive-web/target/classes/spring/application.xml");
		 System.out.println(ac.getBean("adInfoService"));
		 ac.close();
	}
	
}
