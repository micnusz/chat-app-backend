package com.micnusz.chat;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

import com.micnusz.chat.util.AesGcmUtil;

public class GenerateAESKeyTest {

    @Test
    void printKey() throws Exception {
        SecretKey key = AesGcmUtil.generateKey();
        System.out.println("AES_KEY=" + AesGcmUtil.keyToBase64(key));
    }
}
