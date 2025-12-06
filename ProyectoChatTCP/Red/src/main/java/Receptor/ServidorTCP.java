package Receptor;

import Cifrado.GestorSeguridad;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;

public class ServidorTCP {

    private ColaRecibos cola;
    private int puerto;
    private ServerSocket socket;
    private GestorSeguridad gestorSeguridad;
    private boolean cifradoHabilitado = false;

    public ServidorTCP(ColaRecibos cola, int puerto) {
        this.cola = cola;
        this.puerto = puerto;
        try {
            this.gestorSeguridad = new GestorSeguridad();
            this.cifradoHabilitado = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCifradoHabilitado(boolean habilitado) {
        this.cifradoHabilitado = habilitado;
    }

    public void iniciar() {
        try {
            socket = new ServerSocket(puerto);
            // Si el puerto era 0, actualizamos con el asignado real
            if (this.puerto == 0) this.puerto = socket.getLocalPort();
            
            System.out.println("[ServidorTCP] Escuchando en puerto: " + this.puerto);

            while (!socket.isClosed()) {
                Socket cliente = socket.accept();
                // Procesar cada cliente en un hilo nuevo para no bloquear
                new Thread(() -> manejarCliente(cliente)).start();
            }
        } catch (IOException e) {
            System.err.println("[ServidorTCP] Error: " + e.getMessage());
        }
    }

    private void manejarCliente(Socket cliente) {
        try (DataInputStream in = new DataInputStream(cliente.getInputStream());
             DataOutputStream out = new DataOutputStream(cliente.getOutputStream())) {

            if (cifradoHabilitado) {
                // PASO 1: Enviar mi llave pública
                byte[] miLlave = gestorSeguridad.obtenerPublicaBytes();
                out.writeInt(miLlave.length);
                out.write(miLlave);
                out.flush();

                // PASO 2: Recibir llave pública del cliente
                int lenLlave = in.readInt();
                byte[] llaveClienteBytes = new byte[lenLlave];
                in.readFully(llaveClienteBytes);
                // (Opcional: guardar llaveCliente si necesitamos responder en la misma sesión)

                // PASO 3: Recibir Mensaje Cifrado
                int lenMsj = in.readInt();
                byte[] msjCifrado = new byte[lenMsj];
                in.readFully(msjCifrado);

                // PASO 4: Descifrar
                String json = gestorSeguridad.descifrar(msjCifrado);
                if (json != null) {
                    System.out.println("[ServidorTCP] Mensaje recibido seguro.");
                    cola.queue(json);
                }
            } else {
                // Modo texto plano (Legacy/Debug)
                int check = in.readInt(); // Ignorar indicador
                int len = in.readInt();
                byte[] data = new byte[len];
                in.readFully(data);
                cola.queue(new String(data));
            }
        } catch (Exception e) {
            System.err.println("[ServidorTCP] Error conexión: " + e.getMessage());
        } finally {
            try { cliente.close(); } catch (Exception e) {}
        }
    }
}