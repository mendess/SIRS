package com.sirs.guardianapp.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class EncryptionService {

    private static final int keySize = 128;

    public SecretKey generateSecretKey() throws NoSuchAlgorithmException {
            SecureRandom secureRandom = new SecureRandom();
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(keySize, secureRandom);
            return keyGenerator.generateKey();
    }


}
