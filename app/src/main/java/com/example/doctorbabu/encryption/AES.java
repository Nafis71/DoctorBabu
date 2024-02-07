package com.example.doctorbabu.encryption;

import android.annotation.SuppressLint;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    Cipher cipher, decipher;
    SecretKeySpec secretKeySpec;

    public static final String ALGORITHM = "AES";


    public AES() {
    }
    public byte[] init(int keySize) throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM);
        generator.init(keySize); // The AES key size in number of bits
        SecretKey secKey = generator.generateKey();
        return secKey.getEncoded();
    }
    public String encryption(String message, byte[] secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException {
        cipher = Cipher.getInstance(ALGORITHM);
        byte[] stringByte = message.getBytes();
        byte[] encryptedByte; //The number of consecutive (non-overlapping) bytes in a byte string.
        try{
            secretKeySpec = new SecretKeySpec(secretKey,ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
            encryptedByte = cipher.doFinal(stringByte); //DoFinal() Finishes a multiple-part encryption or decryption operation, depending on how this cipher was initialized. DoFinal(Byte[]) Encrypts or decrypts data in a single-part operation, or finishes a multiple-part operation
        }catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw  new RuntimeException(e);
        }
        return new String(encryptedByte, StandardCharsets.ISO_8859_1);
    }
    @SuppressLint("GetInstance")
    public String decryption(String message, byte[] secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException {
        decipher = Cipher.getInstance(ALGORITHM);
        byte[] encryptedByte = message.getBytes(StandardCharsets.ISO_8859_1);
        String decryptedString = null;
        byte[] decryption;
        try {
            secretKeySpec = new SecretKeySpec(secretKey,ALGORITHM);
            decipher.init(Cipher.DECRYPT_MODE,secretKeySpec);
            decryption = decipher.doFinal(encryptedByte);
            decryptedString = new String(decryption);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                throw new RuntimeException(e);
        }
        return decryptedString;

    }
    @SuppressLint("GetInstance")
    public String decryptionSender(String message, byte[] secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException {
        decipher = Cipher.getInstance(ALGORITHM);
        byte[] encryptedByte = message.getBytes(StandardCharsets.ISO_8859_1);
        String decryptedString;
        byte[] decryption;
        try {
            secretKeySpec = new SecretKeySpec(secretKey,ALGORITHM);
            decipher.init(Cipher.DECRYPT_MODE,secretKeySpec);
            decryption = decipher.doFinal(encryptedByte);
            decryptedString = new String(decryption);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        return decryptedString;
    }
}
