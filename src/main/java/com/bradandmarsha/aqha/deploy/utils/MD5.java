package com.bradandmarsha.aqha.deploy.utils;

import java.security.MessageDigest;
import java.util.UUID;

/**
 *
 * @author sbwise01
 */
public class MD5 {
    public static final String HEX_DIGITS = "0123456789abcdef";
    public static MessageDigest algorithm;

    static {
        try {
           algorithm  = MessageDigest.getInstance("MD5");
        } catch(java.security.NoSuchAlgorithmException e) {

        }
    }

    public static String getHash() {
        try {
            MessageDigest md = (MessageDigest)algorithm.clone();
            md.update(UUID.randomUUID().toString().getBytes());

            byte messageDigestBytes[] = md.digest();

            return toHexString(messageDigestBytes);
        }
        catch(Exception e) {
            return "";
        }
    }

    public static String toHexString(byte[] v) {
        StringBuilder sb = new StringBuilder(v.length * 2);

        for (int i = 0; i < v.length; i++) {
             int b = v[i] & 0xFF;
             sb.append(HEX_DIGITS.charAt(b >>> 4))
               .append(HEX_DIGITS.charAt(b & 0xF));
        }

        return sb.toString();
    }
}
