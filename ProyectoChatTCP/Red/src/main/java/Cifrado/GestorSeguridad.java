package Cifrado;

/**
 * Gestor de seguridad para cifrado híbrido RSA + AES-GCM
 *
 * @author Jck Murrieta
 */

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
        // Genera claves RSA 2048 al iniciar
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();

        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    // ==============================
    //   GUARDAR LLAVES EN ARCHIVOS
    // ==============================
    public void guardarLlavePrivada(String archivo) throws Exception {
        byte[] pem = privateKey.getEncoded();
        Files.write(Path.of(archivo), pem);
    }

    public void guardarLlavePublica(String archivo) throws Exception {
        byte[] pem = publicKey.getEncoded();
        Files.write(Path.of(archivo), pem);
    }

    // ==============================
    //   CARGAR LLAVES
    // ==============================
    public boolean cargarPrivadaDesdeArchivo(String archivo) {
        try {
            byte[] keyBytes = Files.readAllBytes(Path.of(archivo));

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            this.privateKey = kf.generatePrivate(spec);
            this.publicKey = kf.generatePublic(new X509EncodedKeySpec(this.publicKey.getEncoded()));
            return true;

        } catch (Exception e) {
            System.out.println("Error cargando llave privada: " + e.getMessage());
            return false;
        }
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
            System.out.println("Error importando llave pública: " + e.getMessage());
            return null;
        }
    }

    // =======================================
    //       CIFRADO HÍBRIDO (RSA + AES)
    // =======================================
    public byte[] cifrar(String mensaje, PublicKey llaveDestino) throws Exception {

        // 1. Generar llave AES-256
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey aesKey = keyGen.generateKey();

        // 2. Cifrar el mensaje con AES-GCM
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[12];
        SecureRandom rand = new SecureRandom();
        rand.nextBytes(iv);

        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] mensajeCifrado = aesCipher.doFinal(mensaje.getBytes());

        // 3. Cifrar llave AES con RSA
        Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsa.init(Cipher.ENCRYPT_MODE, llaveDestino);
        byte[] aesKeyCifrada = rsa.doFinal(aesKey.getEncoded());

        // 4. Paquete final = llaveAES + :: + IV + :: + mensajeCifrado
        byte[] separador = ":::".getBytes();

        return concat(
                aesKeyCifrada, separador,
                iv, separador,
                mensajeCifrado
        );
    }

    // =======================================
    //       DESCIFRADO HÍBRIDO
    // =======================================
    public String descifrar(byte[] paquete) {
        try {
            byte[][] partes = split(paquete, ":::".getBytes(), 3);

            byte[] aesKeyCifrada = partes[0];
            byte[] iv = partes[1];
            byte[] datosCifrados = partes[2];

            // 1. Descifrar llave AES con RSA privada
            Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            rsa.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] aesKeyBytes = rsa.doFinal(aesKeyCifrada);

            SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

            // 2. Descifrar con AES-GCM
            Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));

            byte[] textoPlano = aesCipher.doFinal(datosCifrados);

            return new String(textoPlano);

        } catch (Exception e) {
            System.out.println("Error al descifrar: " + e.getMessage());
            return null;
        }
    }

    // ======================
    //   HASH PASSWORD
    // ======================
    public static String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes());
        return bytesAHex(hash);
    }

    // ======================
    //   UTILIDADES
    // ======================
    private static byte[] concat(byte[]... arrays) {
        int size = 0;
        for (byte[] a : arrays) {
            size += a.length;
        }

        byte[] result = new byte[size];
        int pos = 0;
        for (byte[] a : arrays) {
            System.arraycopy(a, 0, result, pos, a.length);
            pos += a.length;
        }
        return result;
    }

    private static byte[][] split(byte[] data, byte[] separator, int expectedParts)
            throws IOException {

        byte[][] result = new byte[expectedParts][];
        int partIndex = 0;

        int start = 0;
        int i = 0;

        while (i < data.length && partIndex < expectedParts - 1) {
            if (matchesAt(data, separator, i)) {
                result[partIndex++] = Arrays.copyOfRange(data, start, i);
                i += separator.length;
                start = i;
            } else {
                i++;
            }
        }

        result[partIndex] = Arrays.copyOfRange(data, start, data.length);
        return result;
    }

    private static boolean matchesAt(byte[] data, byte[] sep, int index) {
        if (index + sep.length > data.length) {
            return false;
        }
        for (int i = 0; i < sep.length; i++) {
            if (data[index + i] != sep[i]) {
                return false;
            }
        }
        return true;
    }

    private static String bytesAHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
