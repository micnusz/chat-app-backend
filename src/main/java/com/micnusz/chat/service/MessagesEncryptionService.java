package com.micnusz.chat.service;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.micnusz.chat.util.AesGcmUtil;

@Service
public class MessagesEncryptionService {

    private final SecretKey secretKey;

    public MessagesEncryptionService() {
        SecretKey key;
        try {
            key = AesGcmUtil.generateKey();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot generate AES key", e);
        }
        this.secretKey = key;
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

    public SecretKey getSecretKey() {
        return secretKey;
    }
}
