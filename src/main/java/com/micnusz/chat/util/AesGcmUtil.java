package com.micnusz.chat.util;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesGcmUtil {
    

    private static final String ALGO = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256; // 256-bit key
    private static final int IV_SIZE = 12;   // 12 bytes recommended for GCM
    private static final int TAG_SIZE = 128; // bits

    private static final SecureRandom secureRandom = new SecureRandom();

    // Generate random AES key
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGO);
        keyGenerator.init(KEY_SIZE);
        return keyGenerator.generateKey();
    }

    // Encrypt plain text
    public static String encrypt(String plainText, SecretKey secretKey) throws Exception {
    byte[] iv = new byte[IV_SIZE];
    secureRandom.nextBytes(iv);

    Cipher cipher = Cipher.getInstance(TRANSFORMATION);
    GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_SIZE, iv); 
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

    byte[] cipherText = cipher.doFinal(plainText.getBytes());
    byte[] cipherWithIv = new byte[iv.length + cipherText.length];

    System.arraycopy(iv, 0, cipherWithIv, 0, iv.length);
    System.arraycopy(cipherText, 0, cipherWithIv, iv.length, cipherText.length);

    return Base64.getEncoder().encodeToString(cipherWithIv);
}

    // Decrypt ciphertext
    public static String decrypt(String cipherTextBase64, SecretKey key) throws Exception {
        byte[] cipherWithIv = Base64.getDecoder().decode(cipherTextBase64);

        byte[] iv = new byte[IV_SIZE];
        byte[] cipherText = new byte[cipherWithIv.length - IV_SIZE];

        System.arraycopy(cipherWithIv, 0, iv, 0, IV_SIZE);
        System.arraycopy(cipherWithIv, IV_SIZE, cipherText, 0, cipherText.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] plainBytes = cipher.doFinal(cipherText);
        return new String(plainBytes);
    }

    // Convert Base64 string to SecretKey
    public static SecretKey keyFromBase64(String base64Key) {
        byte[] decoded = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(decoded, ALGO);
    }
    // Convert SecretKey to Base64 string
    public static String keyToBase64(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }


}
