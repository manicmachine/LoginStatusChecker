package net.manicmachine.controller;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import net.manicmachine.model.Credential;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;

public class CipherManager {

    private File credFile;
    private String secretKey;
    private String salt;
    private byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private IvParameterSpec ivspec;
    private SecretKeyFactory factory;
    private KeySpec spec;
    private SecretKey tmpSecret;
    private SecretKeySpec secretKeySpec;
    private Cipher cipher;

    public CipherManager(File credFile, String secretKey) throws IOException {
        this.credFile = credFile;
        this.secretKey = secretKey;

        // Create the salt based off the file creation time
        BasicFileAttributes credFileAttr = Files.readAttributes(credFile.toPath(), BasicFileAttributes.class);
        salt = credFileAttr.creationTime().toString();

        ivspec = new IvParameterSpec(iv);

        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), 65536, 256);
            tmpSecret = factory.generateSecret(spec);
            secretKeySpec = new SecretKeySpec(tmpSecret.getEncoded(), "AES");

            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (Exception e) {
            System.out.println("Error initializing CipherManager: " + e.toString());
        }

    }

    public void encrypt(ArrayList<Credential> credentials) throws IOException {
        // Store credentials as a JSON object for simpler parsing on launch
        JsonArray json_credentials = new JsonArray();

        for (Credential credential: credentials) {
            JsonObject json_credential = new JsonObject();
            json_credential.add("credName", credential.getCredName())
                    .add("username", credential.getUsername())
                    .add("password", credential.getPassword())
                    .add("credText", credential.getCredText())
                    .add("credType", credential.getCredType().toString());
            json_credentials.add(json_credential);
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(credFile));

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivspec);
            String encryptedText = Base64.getEncoder().encodeToString(cipher.doFinal(json_credentials.toString().getBytes("UTF-8")));

            writer.write(encryptedText);
            writer.close();
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
    }

    public void decrypt() throws IOException {
        String decryptedText = "";
        String encryptedText = new String(Files.readAllBytes(Paths.get(credFile.toString())));

        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivspec);
            decryptedText = new String(cipher.doFinal(Base64.getDecoder().decode(encryptedText)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }

        System.out.println(decryptedText);
    }
}
