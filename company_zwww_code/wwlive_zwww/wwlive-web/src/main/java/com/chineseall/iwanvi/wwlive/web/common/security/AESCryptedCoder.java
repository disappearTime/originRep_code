package com.chineseall.iwanvi.wwlive.web.common.security;

import java.io.UnsupportedEncodingException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.common.tools.Base64Tools;

public class AESCryptedCoder {
	private static final Logger LOGGER = Logger
			.getLogger(AESCryptedCoder.class);

	private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

	/**
	 * 密钥算法
	 */
	private static final String KEY_ALGORITHM = "AES";

	private static final byte[] key = new byte[] { -68, -57, 19, 9, 92, -78,
			-60, -26, -128, 89, 48, -68, 125, 75, -26, 78 };

	/**
	 * 加密
	 * 
	 * @throws UnsupportedEncodingException
	 * 
	 */
	public static String encrypt(String data) {
		return encrypt(data, key);
	}

	public static void main(String[] args) {
//		String s = encrypt("wx123321123168");
//		System.out.println(s.length());
//		System.out.println(s);
//		System.out.println(decrypt(s));
		System.out.println(decrypt("119"));
	}

	/**
	 * 解密
	 * 
	 */
	public static String decrypt(String data) {
		Assert.notNull(data, "待解密数据不可为空!");
		return decrypt(data, key);
	}

	/**
	 * 加密
	 * 
	 * @param data
	 * @param bytekey
	 * @return
	 */
	public static String encrypt(String data, byte[] bytekey) {
		Assert.notNull(data, "待解密数据不可为空!");
		if (bytekey.length > 16) {
			LOGGER.error("aes加密失败，key 不合法. ");
			throw new IWanviException("aes加密失败，key 不合法. ");
		}
		// 还原密钥
		Key key = toKey(bytekey);

		byte[] encryptedDatas = null;
		try {
			byte[] datas = data.getBytes("UTF8");
			// 实例化
			Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
			// 使用密钥初始化，设置为加密模式
			cipher.init(Cipher.ENCRYPT_MODE, key);
			// 执行操作
			encryptedDatas = cipher.doFinal(datas);
		} catch (Exception e) {
			LOGGER.error("aes加密失败" + e.toString());
			return "";
		}
		byte[] base64Datas = Base64Tools.encode(encryptedDatas).getBytes();
		String encryptedString = new String(base64Datas);
		return encryptedString;
	}

	public static String decrypt(String data, byte[] bytekey) {
		Assert.notNull(data, "待加密数据不可为空!");

		// 还原密钥
		Key key = toKey(bytekey);

		byte[] base64Datas = Base64Tools.decode(data);
		byte[] decryptedDatas = null;
		try {
			// 实例化
			Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
			// 使用密钥初始化，设置为加密模式
			cipher.init(Cipher.DECRYPT_MODE, key);
			// 执行操作
			decryptedDatas = cipher.doFinal(base64Datas);
		} catch (Exception e) {
			LOGGER.error("aes加密失败" + e.toString());
			return "";
		}

		String decryptedString = null;
		try {
			decryptedString = new String(decryptedDatas, "UTF8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("aes加密失败" + e.toString());
			return "";
		}
		return decryptedString;
	}

	/**
	 * 转换密钥
	 * 
	 * @param key
	 *            二进制密钥
	 * @return 密钥
	 */
	private static Key toKey(byte[] key) {
		// 生成密钥
		return new SecretKeySpec(key, KEY_ALGORITHM);
	}
}
