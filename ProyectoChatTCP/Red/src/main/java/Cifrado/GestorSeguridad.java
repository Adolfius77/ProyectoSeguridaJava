package Cifrado;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.*;

public class GestorSeguridad {

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public GestorSeguridad() throws Exception {
        // Genera par de claves RSA 2048
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public byte[] obtenerPublicaBytes() {
        return publicKey.getEncoded();
    }

    public PublicKey importarPublica(byte[] bytes) {
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (Exception e) {
            System.err.println("[Seguridad] Error importando llave: " + e.getMessage());
            return null;
        }
    }

 
    public byte[] cifrar(String mensaje, PublicKey llaveDestino) throws Exception {
        // 1. Generar llave AES temporal
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey aesKey = keyGen.generateKey();

        // 2. Cifrar mensaje con AES-GCM
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[12]; // Vector de inicialización
        new SecureRandom().nextBytes(iv);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] mensajeCifrado = aesCipher.doFinal(mensaje.getBytes());

        // 3. Cifrar llave AES con RSA (usando llave pública destino)
        Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsa.init(Cipher.ENCRYPT_MODE, llaveDestino);
        byte[] aesKeyCifrada = rsa.doFinal(aesKey.getEncoded());

        ByteBuffer buffer = ByteBuffer.allocate(4 + aesKeyCifrada.length + 4 + iv.length + mensajeCifrado.length);
        
        buffer.putInt(aesKeyCifrada.length);
        buffer.put(aesKeyCifrada);
        
        buffer.putInt(iv.length);
        buffer.put(iv);
        
        buffer.put(mensajeCifrado);
        
        return buffer.array();
    }

    // --- DESCIFRADO HÍBRIDO ---
    public String descifrar(byte[] paquete) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(paquete);

            int keyLength = buffer.getInt();
            if (keyLength < 0 || keyLength > 1024) throw new IllegalArgumentException("Longitud de llave invalida");
            byte[] aesKeyCifrada = new byte[keyLength];
            buffer.get(aesKeyCifrada);

            int ivLength = buffer.getInt();
            if (ivLength != 12) throw new IllegalArgumentException("IV invalido");
            byte[] iv = new byte[ivLength];
            buffer.get(iv);

            byte[] datosCifrados = new byte[buffer.remaining()];
            buffer.get(datosCifrados);

            Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            rsa.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] aesKeyBytes = rsa.doFinal(aesKeyCifrada);
            SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

            Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
            
            return new String(aesCipher.doFinal(datosCifrados));

        } catch (Exception e) {
            System.err.println("[Seguridad] Fallo al descifrar: " + e.getMessage());
            e.printStackTrace(); 
            return null;
        }
    }

    public static String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}