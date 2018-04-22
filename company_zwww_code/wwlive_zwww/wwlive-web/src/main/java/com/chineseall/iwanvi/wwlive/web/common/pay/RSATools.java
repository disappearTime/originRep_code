package com.chineseall.iwanvi.wwlive.web.common.pay;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;




import org.apache.log4j.Logger;

import com.chineseall.iwanvi.wwlive.common.tools.Base64Tools;

public class RSATools {
	
	private static Logger LOGGER = Logger.getLogger(RSATools.class);
	
	/**
	 * RSA签名，签名方式为SHA1WithRSA
	 * @param plainText 待签名数据
	 * @param privateKey 私钥
	 * @return 签名值
	 */
	public static String sign(String plainText, String privateKey) {
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
					Base64Tools.decode(privateKey));
			KeyFactory keyf = KeyFactory.getInstance("RSA");
			PrivateKey prikey = keyf.generatePrivate(priPKCS8);

			// 用私钥对信息生成数字签名
			java.security.Signature signet = java.security.Signature
					.getInstance("SHA1WithRSA");
			signet.initSign(prikey);
			signet.update(plainText.getBytes());
			String encrypt = Base64Tools.encode(signet.sign());
			return encrypt;
		} catch (java.lang.Exception e) {
			LOGGER.error("生成签名错误", e);
		}
		return null;
	}

	public static String sign(String plainText, String privateKey, String charset) {
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
					Base64Tools.decode(privateKey));
			KeyFactory keyf = KeyFactory.getInstance("RSA");
			PrivateKey prikey = keyf.generatePrivate(priPKCS8);

			// 用私钥对信息生成数字签名
			java.security.Signature signet = java.security.Signature
					.getInstance("SHA1WithRSA");
			signet.initSign(prikey);
			signet.update(plainText.getBytes(charset));
			String encrypt = Base64Tools.encode(signet.sign());
			return encrypt;
		} catch (java.lang.Exception e) {
			LOGGER.error("生成签名错误", e);
		}
		return null;
	}
	
	public static boolean doCheck(String content, String sign,
			String publicKey) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			byte[] encodedKey = Base64Tools.decode(publicKey);
			PublicKey pubKey = keyFactory
					.generatePublic(new X509EncodedKeySpec(encodedKey));

			java.security.Signature signature = java.security.Signature
					.getInstance("SHA1WithRSA");

			signature.initVerify(pubKey);
			signature.update(content.getBytes());

			return signature.verify(Base64Tools.decode(sign));

		} catch (Exception e) {
			LOGGER.error("签名校验错误", e);
		}

		return false;
	}

	public static boolean doCheck(String content, String sign,
			String publicKey, String charset) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			byte[] encodedKey = Base64Tools.decode(publicKey);
			PublicKey pubKey = keyFactory
					.generatePublic(new X509EncodedKeySpec(encodedKey));

			java.security.Signature signature = java.security.Signature
					.getInstance("SHA1WithRSA");

			signature.initVerify(pubKey);
			signature.update(content.getBytes(charset));

			return signature.verify(Base64Tools.decode(sign));

		} catch (Exception e) {
			LOGGER.error("签名校验错误", e);
		}

		return false;
	}
	/*public static void main(String[] args) throws Exception {
		// System.out.println(new
		// String(Arrays.toString((encrypt("{\"a\":\"123\"}")))));
		Test1 t = new Test1();
		String json = JSONObject.toJSONString(t).toString();
		String sign = new String(sign(json));
		System.out.println(doCheck(json, sign));
		// doCheck
		// System.out.println(new String(decrypt(encrypt("{\"a\":\"123\"}"))));
		 * 
		class Test1 {
		private String a = "123";
	
		public String getA() {
			return a;
		}
	
		public void setA(String a) {
			this.a = a;
		}
	}
	}*/

}


