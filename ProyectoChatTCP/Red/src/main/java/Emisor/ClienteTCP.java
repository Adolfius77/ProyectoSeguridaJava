package Emisor;

import ObserverEmisor.ObservadorEnvios;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Jck Murrieta
 */
public class ClienteTCP implements ObservadorEnvios {

    private ColaEnvios cola;
    private int puerto;
    private String host;

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

    public ClienteTCP(ColaEnvios cola, int puerto, String host) {
        this.cola = cola;
        this.puerto = puerto;
        this.host = host;

    }

    public void enviarPaquete(String paquete, String host, int puerto) {
        try (Socket socket = new Socket(host, puerto); PrintWriter out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream()), true)) {

            out.println(paquete);
            if (out.checkError()) {
                System.err.println("Error al enviar datos (PrintWriter)");
            }

        } catch (IOException e) {
            System.err.println("Error enviando paquete por TCP: " + e.getMessage());
        }
    }
}
