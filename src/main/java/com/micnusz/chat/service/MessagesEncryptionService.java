package com.micnusz.chat.service;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.micnusz.chat.util.AesGcmUtil;

@Service
public class MessagesEncryptionService {

    private final SecretKey secretKey;

    public MessagesEncryptionService() {
        try {
            String base64Key = System.getenv("AES_KEY");
            if (base64Key == null || base64Key.isEmpty()) {
                throw new RuntimeException("AES_KEY environment variable not set!");
            }
            this.secretKey = AesGcmUtil.keyFromBase64(base64Key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize AES key", e);
        }
    }

    public String encrypt(String message) {
        try {
            return AesGcmUtil.encrypt(message, secretKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt message", e);
        }
    }

    public String decrypt(String cipherText) {
        try {
            return AesGcmUtil.decrypt(cipherText, secretKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt message", e);
        }
    }

    public String getKeyBase64() {
        return AesGcmUtil.keyToBase64(secretKey);
    }
}
