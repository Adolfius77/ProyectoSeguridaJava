package Emisor;

import Cifrado.GestorSeguridad;
import ObserverEmisor.ObservadorEnvios;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Cliente TCP con soporte de cifrado híbrido RSA + AES-GCM
 *
 * @author Jck Murrieta
 */
public class ClienteTCP implements ObservadorEnvios {

    private ColaEnvios cola;
    private int puerto;
    private String host;
    private GestorSeguridad gestorSeguridad;
    private boolean cifradoHabilitado;

    @Override
    public void actualizar() {
        try {
            String paqueteSerializado = cola.dequeue();

            if (paqueteSerializado == null) {
                return;
            }

            JsonObject obj = JsonParser.parseString(paqueteSerializado).getAsJsonObject();

            String hostDestino = obj.get("host").getAsString();
            int puertoDestino = obj.get("puertoDestino").getAsInt();
            System.out.println("[ClienteTCP] paquete: " + obj.toString() + " " + hostDestino + puertoDestino);
            enviarPaquete(obj.toString(), hostDestino, puertoDestino);

        } catch (Exception e) {
            System.err.println("Error en ClienteTCP.actualizar(): " + e.getMessage());
        }
    }

    /**
     * Constructor original (mantener compatibilidad hacia atrás)
     * Genera su propio GestorSeguridad
     */
    public ClienteTCP(ColaEnvios cola, int puerto, String host) {
        this.cola = cola;
        this.puerto = puerto;
        this.host = host;
        this.cifradoHabilitado = false;

        try {
            this.gestorSeguridad = new GestorSeguridad();
            this.cifradoHabilitado = true;
            System.out.println("[ClienteTCP] Cifrado habilitado con RSA + AES-GCM (nuevo gestor)");
        } catch (Exception e) {
            System.err.println("[ClienteTCP] No se pudo inicializar el cifrado: " + e.getMessage());
            this.cifradoHabilitado = false;
        }
    }

    /**
     * Constructor que acepta un GestorSeguridad compartido (RECOMENDADO)
     * Permite compartir las mismas llaves entre ClienteTCP y ServidorTCP
     */
    public ClienteTCP(ColaEnvios cola, int puerto, String host, GestorSeguridad gestorSeguridad) {
        this.cola = cola;
        this.puerto = puerto;
        this.host = host;
        this.gestorSeguridad = gestorSeguridad;
        this.cifradoHabilitado = (gestorSeguridad != null);

        if (this.cifradoHabilitado) {
            System.out.println("[ClienteTCP] Cifrado habilitado con GestorSeguridad compartido");
        } else {
            System.out.println("[ClienteTCP] Cifrado deshabilitado (gestorSeguridad es null)");
        }
    }

    /**
     * Habilita o deshabilita el cifrado de mensajes
     */
    public void setCifradoHabilitado(boolean habilitado) {
        this.cifradoHabilitado = habilitado && gestorSeguridad != null;
    }

    /**
     * Obtiene la llave pública de este cliente
     */
    public byte[] obtenerLlavePublica() {
        if (gestorSeguridad != null) {
            return gestorSeguridad.obtenerPublicaBytes();
        }
        return null;
    }

    /**
     * Obtiene el GestorSeguridad asociado a este cliente
     */
    public GestorSeguridad getGestorSeguridad() {
        return gestorSeguridad;
    }

    public void enviarPaquete(String paquete, String host, int puerto) {
        System.out.println("[ClienteTCP] Intentando conectar a " + host + ":" + puerto);

        if (puerto == 0) {
            System.err.println("[ClienteTCP] ✗ ERROR: Puerto destino es 0. No se puede conectar.");
            System.err.println("[ClienteTCP] Paquete que se intentaba enviar: " + paquete.substring(0, Math.min(200, paquete.length())));
            return;
        }

        try (Socket socket = new Socket(host, puerto);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            System.out.println("[ClienteTCP] ✓ Conexión establecida con " + host + ":" + puerto);

            if (cifradoHabilitado && gestorSeguridad != null) {
                // 1. Intercambio de llaves: recibir llave pública del servidor
                int tamanoLlave = in.readInt();
                byte[] llavePublicaServidor = new byte[tamanoLlave];
                in.readFully(llavePublicaServidor);

                PublicKey llaveServidor = gestorSeguridad.importarPublica(llavePublicaServidor);

                if (llaveServidor == null) {
                    System.err.println("[ClienteTCP] Error: No se pudo importar la llave pública del servidor");
                    // Enviar sin cifrar como fallback
                    enviarSinCifrar(out, paquete);
                    return;
                }

                // 2. Enviar nuestra llave pública
                byte[] nuestraLlave = gestorSeguridad.obtenerPublicaBytes();
                out.writeInt(nuestraLlave.length);
                out.write(nuestraLlave);
                out.flush();

                // 3. Cifrar y enviar el mensaje
                byte[] mensajeCifrado = gestorSeguridad.cifrar(paquete, llaveServidor);
                out.writeInt(mensajeCifrado.length);
                out.write(mensajeCifrado);
                out.flush();

                System.out.println("[ClienteTCP] Paquete enviado cifrado a " + host + ":" + puerto);
            } else {
                // Enviar sin cifrado
                enviarSinCifrar(out, paquete);
            }

        } catch (IOException e) {
            System.err.println("[ClienteTCP] Error enviando paquete por TCP: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ClienteTCP] Error cifrando paquete: " + e.getMessage());
        }
    }

    private void enviarSinCifrar(DataOutputStream out, String paquete) throws IOException {
        byte[] datos = paquete.getBytes();
        out.writeInt(0); // Indicador de que NO está cifrado
        out.writeInt(datos.length);
        out.write(datos);
        out.flush();
        System.out.println("[ClienteTCP] Paquete enviado SIN cifrar");
    }
}
