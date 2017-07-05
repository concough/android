package com.concough.android.utils;

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;

/**
 * Created by abolfazl on 7/4/17.
 */

public class EnDeCryptorV18 {
    static final String TAG = "SimpleKeystoreApp";
    static final String CIPHER_TYPE = "RSA/ECB/PKCS1Padding";
    static final String CIPHER_PROVIDER = "AndroidOpenSSL";
    static final String KeyStore_PROVIDER = "AndroidKeyStore";

    static final String SIGNATURE_SHA256withRSA = "SHA256withRSA";
    static final String SIGNATURE_SHA512withRSA = "SHA512withRSA";

    KeyStore keyStore;
    String alias;
    Context context;

    public EnDeCryptorV18(Context context, String alias) throws Exception {
        Log.d(TAG, "EnDeCryptorV18: alias = " + alias);
        this.alias = alias;
        this.context = context;
        this.loadKeyStore();
    }

    private void loadKeyStore() throws Exception {
        keyStore = KeyStore.getInstance(KeyStore_PROVIDER);
        keyStore.load(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void createNewKeys() throws Exception {
        if (!keyStore.containsAlias(alias)) {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 25);

            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(alias)
                    .setSubject(new X500Principal("CN=" + alias + ", O=Android Authority"))
                    .setSerialNumber(BigInteger.ONE)
                    .setEndDate(end.getTime())
                    .setStartDate(start.getTime())
                    .build();
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", KeyStore_PROVIDER);
            generator.initialize(spec);

            KeyPair keyPair = generator.generateKeyPair();
        }
    }

    public void deleteKey(final String key) throws KeyStoreException {
        keyStore.deleteEntry(key);
    }

    public String encryptString(String value) throws Exception {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();

            if(value.toString() == "") {
                return null;
            }

            Cipher inCipher = Cipher.getInstance(CIPHER_TYPE, CIPHER_PROVIDER);
            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    outputStream, inCipher);
            cipherOutputStream.write(value.getBytes("utf-8"));
            cipherOutputStream.close();
            byte [] vals = outputStream.toByteArray();

            String result = Base64.encodeToString(vals, Base64.DEFAULT);
            return result;

    }

    public String decryptString(String value) throws Exception {
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
        RSAPrivateKey privateKey = (RSAPrivateKey) privateKeyEntry.getPrivateKey();

        Cipher output = Cipher.getInstance(CIPHER_TYPE, CIPHER_PROVIDER);
        output.init(Cipher.DECRYPT_MODE, privateKey);

        CipherInputStream cipherInputStream = new CipherInputStream(
                new ByteArrayInputStream(Base64.decode(value, Base64.DEFAULT)), output);
        ArrayList<Byte> values = new ArrayList<>();
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            values.add((byte) nextByte);
        }

        byte[] bytes = new byte[values.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = values.get(i).byteValue();
        }

        String finalText = new String(bytes, 0, bytes.length, "UTF-8");
        return finalText;
    }
}
