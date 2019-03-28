package com.zzhou.entrance.guard.util;



import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by zhouzhen on 6/29/16.
 */
public class CipherUtils {

    // 向量
    private final static String iv = "01234567";
    // 加解密统一使用的编码方式
    private final static String encoding = "utf-8";
    /**
     * MD5加密
     * <br>http://stackoverflow.com/questions/1057041/difference-between-java-and-php5-md5-hash
     * <br>http://code.google.com/p/roboguice/issues/detail?id=89
     *
     * @param
     * @return 加密后的字符串
     */
    public static String md5(String string) {
        byte[] hash = null;
        try {
            hash = MessageDigest.getInstance("MD5").digest(
                    string.getBytes(encoding));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }
 public static String test(String data, String value){
//     Log.e("CipherUtils",data);
     return data +value;
 }
    /**
     * 根据指定的密钥及算法，将字符串进行加密。
     * @param data 要进行加密的字符串。
     * @return  加密后的结果将由byte[]数组转换为64进制表示的数组。如果加密过程失败，将返回null。
     * @throws Exception
     */
    public static String encrypt(String data){
        String crkey = "mmamamamamamaamamammamam";
        try {
            Key key = getDESKey(crkey.getBytes());
            Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");
            IvParameterSpec ips = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, key, ips);
            byte[] encryptData = cipher.doFinal(data.getBytes(encoding));
            return Base64.encode(encryptData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 根据指定的密钥及算法，将字符串进行解密。
     * @param data 要进行解密的数据，它是由原来的byte[]数组转化为字符串的结果。
     * @return 解密后的结果。它由解密后的byte[]重新创建为String对象。如果解密失败，将返回null。
     * @throws Exception
     */
    public static String decrypt(String data){
        String crkey = "mmamamamamamaamamammamam";
        try {
            Key key = getDESKey(crkey.getBytes());
            Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");
            IvParameterSpec ips = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, key, ips);

            byte[] decryptData = cipher.doFinal(Base64.decode(data));
            return new String(decryptData, encoding);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 返回可逆算法DES的密钥
     *
     * @param key 前8字节将被用来生成密钥。
     * @return 生成的密钥
     * @throws Exception
     */
    public static Key getDESKey(byte[] key) throws Exception {
        DESedeKeySpec des = new DESedeKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("desede");
        return keyFactory.generateSecret(des);
    }
}
