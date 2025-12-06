package Emisor;

import Cifrado.GestorSeguridad;
import ObserverReceptor.ObservadorEnvios;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.security.PublicKey;

public class ClienteTCP implements ObservadorEnvios {

    private ColaEnvios cola;
    // Valores por defecto si no vienen en el paquete
    private int puertoDefault; 
    private String hostDefault;
    
    private GestorSeguridad gestorSeguridad;
    private boolean cifradoHabilitado = false;

    public ClienteTCP(ColaEnvios cola, int puertoDefault, String hostDefault) {
        this.cola = cola;
        this.puertoDefault = puertoDefault;
        this.hostDefault = hostDefault;
        try {
            this.gestorSeguridad = new GestorSeguridad();
            this.cifradoHabilitado = true;
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void setCifradoHabilitado(boolean b) { this.cifradoHabilitado = b; }

    @Override
    public void actualizar() {
        // Desencolar mensaje (JSON String)
        String json = cola.dequeue(); 
        if (json == null) return;

        try {
            // Analizar JSON para ver si trae destino específico
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            
            // Usar destino del paquete, o el default si no trae
            String host = obj.has("host") ? obj.get("host").getAsString() : hostDefault;
            int puerto = obj.has("puertoDestino") && obj.get("puertoDestino").getAsInt() > 0 
                         ? obj.get("puertoDestino").getAsInt() : puertoDefault;

            enviar(json, host, puerto);
        } catch (Exception e) {
            System.err.println("[ClienteTCP] Error procesando envío: " + e.getMessage());
        }
    }

    private void enviar(String mensaje, String host, int puerto) {
        try (Socket socket = new Socket(host, puerto);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            if (cifradoHabilitado) {
                // PASO 1: Recibir llave pública del Servidor
                int lenServerKey = in.readInt();
                byte[] serverKeyBytes = new byte[lenServerKey];
                in.readFully(serverKeyBytes);
                PublicKey llaveServidor = gestorSeguridad.importarPublica(serverKeyBytes);

                // PASO 2: Enviar mi llave pública
                byte[] miKey = gestorSeguridad.obtenerPublicaBytes();
                out.writeInt(miKey.length);
                out.write(miKey);
                out.flush();

                // PASO 3: Cifrar mensaje con llave del servidor
                byte[] cifrado = gestorSeguridad.cifrar(mensaje, llaveServidor);
                out.writeInt(cifrado.length);
                out.write(cifrado);
                out.flush();
                
                System.out.println("[ClienteTCP] Enviado cifrado a " + host + ":" + puerto);
            } else {
                byte[] b = mensaje.getBytes();
                out.writeInt(0);
                out.writeInt(b.length);
                out.write(b);
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("[ClienteTCP] Error enviando: " + e.getMessage());
        }
    }
}