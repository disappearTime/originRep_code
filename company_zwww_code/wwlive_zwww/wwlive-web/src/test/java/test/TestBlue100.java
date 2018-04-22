package test;

import java.io.File;

import org.junit.Test;

import com.chineseall.iwanvi.wwlive.common.tools.FileMD5Tools;
import com.chineseall.iwanvi.wwlive.common.tools.StrMD5;

public class TestBlue100 {

	@Test
	public void test1() {
		File file = new File("C:/Users/Thinkpad/Desktop/2.3.0.apk");
		String md5 = FileMD5Tools.getMd5ByFile(file);
		System.out.println(md5);
	}

	public void test2() {
		System.out.println(StrMD5.getInstance().getStringMD5("707J_LFP4CG768913cfab8442979a42d3f2907b4eff"));
	}
	
}
