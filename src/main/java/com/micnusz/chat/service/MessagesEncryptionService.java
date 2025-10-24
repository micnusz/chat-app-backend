package com.micnusz.chat.service;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.micnusz.chat.util.AesGcmUtil;

@Service
public class MessagesEncryptionService {

    private final SecretKey secretKey;

    public MessagesEncryptionService() {
        try {
            this.secretKey = AesGcmUtil.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate AES key", e);
        }
    }

    public String encrypt(String message) {
        try {
            // Poprawka: GCMParameterSpec w AesGcmUtil używa TAG_SIZE zamiast IV_SIZE
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
