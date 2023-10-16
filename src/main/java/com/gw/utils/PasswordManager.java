package com.gw.utils;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;

public class PasswordManager {

    private static final String KEYSTORE_PATH = "~/gw-workspace/keystore.ks";
    private static final String KEYSTORE_PASSWORD = "";
    private static final String SECRET_KEY_ALIAS = "GeoweaverLocalhostPassword";
    private static final String KEY_ALGORITHM = "PBE";
    private KeyStore keyStore;

    public PasswordManager() throws Exception {
        File keystoreFile = new File(KEYSTORE_PATH);
        if (keystoreFile.exists()) {
            try (FileInputStream fis = new FileInputStream(keystoreFile)) {
               keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
               keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());
            }
        } else {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, KEYSTORE_PASSWORD.toCharArray());
            try (FileOutputStream fos = new FileOutputStream(KEYSTORE_PATH)) {
                keyStore.store(fos, KEYSTORE_PASSWORD.toCharArray());
            }
        }
    }

    public String getLocalhostPassword() throws Exception {
        if (keyStore.containsAlias(SECRET_KEY_ALIAS)) {
            KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection(KEYSTORE_PASSWORD.toCharArray());
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(SECRET_KEY_ALIAS, keyPassword);
            SecretKey secretKey = entry.getSecretKey();
            return new String(secretKey.getEncoded());
        }
        return null;
    }

    public void setLocalhostPassword(String originalPassword, boolean force) throws Exception{
        if (keyStore.containsAlias(SECRET_KEY_ALIAS) && !force) {
            return;
        }
        KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection(KEYSTORE_PASSWORD.toCharArray());
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        SecretKey generatedSecret = factory.generateSecret(new PBEKeySpec(originalPassword.toCharArray()));
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(generatedSecret);
        keyStore.setEntry(SECRET_KEY_ALIAS, secretKeyEntry, keyPassword);

        try (FileOutputStream fos = new FileOutputStream(KEYSTORE_PATH)) {
            keyStore.store(fos, KEYSTORE_PASSWORD.toCharArray());
        }
    }
}
