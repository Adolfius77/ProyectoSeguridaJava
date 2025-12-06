/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Receptor;

import Cifrado.GestorSeguridad;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;

/**
 * Servidor TCP con soporte de cifrado híbrido RSA + AES-GCM
 *
 * @author Jck Murrieta
 */
public class ServidorTCP {

    private ColaRecibos cola;
    private int puerto;
    private ServerSocket socket;
    private GestorSeguridad gestorSeguridad;
    private boolean cifradoHabilitado;

    public ServidorTCP(ColaRecibos cola, int puerto) {
        this.cola = cola;
        this.puerto = puerto;
        this.cifradoHabilitado = false;

        try {
            this.gestorSeguridad = new GestorSeguridad();
            this.cifradoHabilitado = true;
            System.out.println("[ServidorTCP] Cifrado habilitado con RSA + AES-GCM");
        } catch (Exception e) {
            System.err.println("[ServidorTCP] No se pudo inicializar el cifrado: " + e.getMessage());
            this.cifradoHabilitado = false;
        }
    }

    /**
     * Habilita o deshabilita el cifrado de mensajes
     */
    public void setCifradoHabilitado(boolean habilitado) {
        this.cifradoHabilitado = habilitado && gestorSeguridad != null;
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
        try (DataInputStream in = new DataInputStream(cliente.getInputStream());
             DataOutputStream out = new DataOutputStream(cliente.getOutputStream())) {

            if (cifradoHabilitado && gestorSeguridad != null) {
                // 1. Enviar nuestra llave pública al cliente
                byte[] nuestraLlave = gestorSeguridad.obtenerPublicaBytes();
                out.writeInt(nuestraLlave.length);
                out.write(nuestraLlave);
                out.flush();

                // 2. Recibir llave pública del cliente
                int tamanoLlave = in.readInt();
                byte[] llavePublicaCliente = new byte[tamanoLlave];
                in.readFully(llavePublicaCliente);

                PublicKey llaveCliente = gestorSeguridad.importarPublica(llavePublicaCliente);

                // 3. Recibir mensaje cifrado
                int tamanoMensaje = in.readInt();
                byte[] mensajeCifrado = new byte[tamanoMensaje];
                in.readFully(mensajeCifrado);

                // 4. Descifrar mensaje
                String mensajeDescifrado = gestorSeguridad.descifrar(mensajeCifrado);

                if (mensajeDescifrado != null) {
                    cola.queue(mensajeDescifrado);
                    System.out.println("[ServidorTCP] Paquete CIFRADO recibido y descifrado correctamente");
                } else {
                    System.err.println("[ServidorTCP] Error: No se pudo descifrar el mensaje");
                }
            } else {
                // Recibir sin cifrado
                int indicadorCifrado = in.readInt();
                int tamano = in.readInt();
                byte[] datos = new byte[tamano];
                in.readFully(datos);

                String recibido = new String(datos);
                cola.queue(recibido);
                System.out.println("[ServidorTCP] Paquete SIN CIFRAR recibido: " + recibido);
            }

        } catch (IOException e) {
            System.err.println("[ServidorTCP] Error recibiendo paquete: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ServidorTCP] Error descifrando paquete: " + e.getMessage());
        } finally {
            try {
                cliente.close();
            } catch (IOException e) {
                System.err.println("[ServidorTCP] No se pudo cerrar el socket cliente");
            }
        }
    }
}
