package com.gw.ssh;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import org.apache.commons.codec.binary.Base64;

public class RSAEncryptTool {

  public static Map<String, KeyPair> token2KeyPair = new HashMap();

  public static String getPublicKey(String sessionid) throws NoSuchAlgorithmException {

    StringBuffer key = new StringBuffer();

    KeyPair kp = RSAEncryptTool.buildKeyPair();

    token2KeyPair.put(sessionid, kp);

    key.append("{ \"rsa_public\": \"")
        .append(RSAEncryptTool.byte2Base64(kp.getPublic().getEncoded()))
        .append("\" } ");

    return key.toString();
  }

  public static String[] getPasswords(String[] encryptedlist, String sessionid) throws Exception {

    String[] passwords = new String[encryptedlist.length];

    try {

      for (int i = 0; i < encryptedlist.length; i++) {

        byte[] pswdbytes = base642Byte(encryptedlist[i]);

        PrivateKey pk = token2KeyPair.get(sessionid).getPrivate();

        passwords[i] = new String(decrypt(pk, pswdbytes));
      }

    } finally {

      voidKey(sessionid);
    }

    return passwords;
  }

  public static String getPassword(String encrypted, String sessionid) throws Exception {

    String password = null;

    try {

      byte[] pswdbytes = base642Byte(encrypted);
      KeyPair kp = token2KeyPair.get(sessionid);

      if (kp != null) {

        PrivateKey pk = kp.getPrivate();

        password = new String(decrypt(pk, pswdbytes));
      }

    } finally {

      voidKey(sessionid);
    }

    return password;
  }

  public static void voidKey(String sessionid) {

    token2KeyPair.remove(sessionid);
  }

  public static KeyPair buildKeyPair() throws NoSuchAlgorithmException {
    final int keySize = 2048;
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(keySize);
    return keyPairGenerator.genKeyPair();
  }

  public static byte[] decrypt(PrivateKey privateKey, byte[] encrypted) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.DECRYPT_MODE, privateKey);

    return cipher.doFinal(encrypted);
  }

  public static String byte2Base64(byte[] bytes) {

    Base64 codec = new Base64();

    byte[] encoded = codec.encodeBase64(bytes);

    return new String(encoded);
  }

  public static byte[] base642Byte(String base64) {

    Base64 codec = new Base64();

    byte[] decoded = codec.decodeBase64(base64.getBytes());

    return decoded;
  }

  public static byte[] encrypt(PublicKey publicKey, String message) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);

    return cipher.doFinal(message.getBytes());
  }
}
