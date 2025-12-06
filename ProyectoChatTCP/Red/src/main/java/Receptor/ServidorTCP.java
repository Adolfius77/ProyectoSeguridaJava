/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Receptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Jck Murrieta
 */
public class ServidorTCP {

    private ColaRecibos cola;
    private int puerto;
    private ServerSocket socket;

    public ServidorTCP(ColaRecibos cola, int puerto) {
        this.cola = cola;
        this.puerto = puerto;
    }

    public void iniciar() {
        try {
            socket = new ServerSocket(puerto);
            System.out.println("Servidor TCP iniciado en puerto: " + puerto);

            Thread hiloServidor = new Thread(() -> {
                while (!socket.isClosed()) {
                    try {
                        Socket cliente = socket.accept();
                        recibirPaquete(cliente);
                    } catch (IOException e) {
                        System.err.println("Error aceptando cliente: " + e.getMessage());
                    }
                }
            });

            hiloServidor.start();

        } catch (IOException e) {
            System.err.println("No se pudo iniciar el servidor TCP: " + e.getMessage());
        }
    }

    public void recibirPaquete(Socket cliente) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(cliente.getInputStream()))) {

            // Recibe el paquete serializado 
            String recibido = br.readLine();

            if (recibido != null) {
                cola.queue(recibido);
            }
            System.out.println("[ServidorTCP] recibido paquete: " + recibido);
        } catch (IOException e) {
            System.err.println("Error recibiendo paquete: " + e.getMessage());
        } finally {
            try {
                cliente.close();
            } catch (IOException e) {
                System.err.println("No se pudo cerrar el socket cliente");
            }
        }
    }
}
