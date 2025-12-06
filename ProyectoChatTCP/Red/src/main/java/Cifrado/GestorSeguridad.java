package Cifrado;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;

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

    // --- CIFRADO HÍBRIDO (Mensaje -> AES -> RSA) ---
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

        // 4. Empaquetar todo: [AES_Key_Enc | IV | Msg_Enc]
        byte[] separador = ":::".getBytes();
        return concat(aesKeyCifrada, separador, iv, separador, mensajeCifrado);
    }

    // --- DESCIFRADO HÍBRIDO ---
    public String descifrar(byte[] paquete) {
        try {
            byte[][] partes = split(paquete, ":::".getBytes(), 3);
            if (partes == null) return null;

            byte[] aesKeyCifrada = partes[0];
            byte[] iv = partes[1];
            byte[] datosCifrados = partes[2];

            // 1. Descifrar llave AES con mi llave Privada RSA
            Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            rsa.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] aesKeyBytes = rsa.doFinal(aesKeyCifrada);
            SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

            // 2. Descifrar mensaje con AES
            Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
            
            return new String(aesCipher.doFinal(datosCifrados));

        } catch (Exception e) {
            System.err.println("[Seguridad] Fallo al descifrar: " + e.getMessage());
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

    // --- Utilidades de Bytes ---
    private static byte[] concat(byte[]... arrays) {
        int len = 0;
        for (byte[] a : arrays) len += a.length;
        byte[] result = new byte[len];
        int pos = 0;
        for (byte[] a : arrays) {
            System.arraycopy(a, 0, result, pos, a.length);
            pos += a.length;
        }
        return result;
    }

    private static byte[][] split(byte[] data, byte[] separator, int parts) {
        // Implementación simplificada de split binario
        // NOTA: Para producción, usar una librería robusta o esta implementación manual con cuidado
        try {
            byte[][] result = new byte[parts][];
            int partIdx = 0;
            int start = 0;
            for (int i = 0; i < data.length - separator.length + 1 && partIdx < parts - 1; i++) {
                boolean match = true;
                for (int j = 0; j < separator.length; j++) {
                    if (data[i + j] != separator[j]) { match = false; break; }
                }
                if (match) {
                    result[partIdx++] = Arrays.copyOfRange(data, start, i);
                    i += separator.length - 1;
                    start = i + 1;
                }
            }
            result[partIdx] = Arrays.copyOfRange(data, start, data.length);
            return result;
        } catch (Exception e) { return null; }
    }
}