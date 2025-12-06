package ensamblador;

import Cifrado.GestorSeguridad;
import Emisor.ClienteTCP;
import Emisor.ColaEnvios;
import Emisor.Emisor;
import Receptor.ColaRecibos;
import Receptor.Receptor;
import Receptor.ServidorTCP;
import org.itson.componenteemisor.IEmisor;
import org.itson.componentereceptor.IReceptor;

/**
 * Singleton encargado de ensamblar la infraestructura de red del cliente.
 */
public class EnsambladorRed {

    private static EnsambladorRed instancia;

    private ServidorTCP servidor;
    private ClienteTCP clienteTCP;
    private Emisor emisor;
    private GestorSeguridad seguridad;

    // Configuración de puertos
    private int puertoEscucha = 0; // 0 = Asignación automática
    private String hostDestino = "localhost";
    private int puertoDestino = 5556; // Puerto del BUS de eventos

    private EnsambladorRed() {
        try {
            this.seguridad = new GestorSeguridad();
        } catch (Exception e) {
            System.err.println("[EnsambladorRed] Error iniciando seguridad: " + e.getMessage());
        }
    }

    public static synchronized EnsambladorRed getInstancia() {
        if (instancia == null) {
            instancia = new EnsambladorRed();
        }
        return instancia;
    }

    /**
     * Ensambla los componentes de red (Emisor, Receptor, Sockets).
     * @param receptorLogica La clase que procesará los mensajes recibidos.
     * @return La interfaz IEmisor para enviar mensajes.
     */
    public IEmisor ensamblar(IReceptor receptorLogica) {
        // --- 1. CONFIGURACIÓN DE RECEPCIÓN (SERVIDOR) ---
        ColaRecibos colaRecibos = new ColaRecibos();

        // Servidor que escuchará respuestas del Bus
        this.servidor = new ServidorTCP(colaRecibos, puertoEscucha);
        this.servidor.setCifradoHabilitado(true);

        // Conectar la cola con la lógica del cliente
        Receptor receptorObj = new Receptor();
        receptorObj.setCola(colaRecibos);
        receptorObj.setReceptor(receptorLogica);
        colaRecibos.agregarObservador(receptorObj);

        // Iniciar el servidor en un hilo secundario
        new Thread(() -> {
            servidor.iniciar();
            // Aquí podríamos capturar el puerto real si fue 0
        }).start();

        // --- 2. CONFIGURACIÓN DE EMISIÓN (CLIENTE) ---
        ColaEnvios colaEnvios = new ColaEnvios();

        // Cliente TCP que enviará al Bus
        this.clienteTCP = new ClienteTCP(colaEnvios, puertoDestino, hostDestino);
        this.clienteTCP.setCifradoHabilitado(true);
        colaEnvios.agregarObservador(this.clienteTCP);

        this.emisor = new Emisor(colaEnvios);

        return this.emisor;
    }

    public void detener() {
        // Implementar lógica de cierre si es necesario
    }

    public byte[] getPublicKey() {
        return seguridad != null ? seguridad.obtenerPublicaBytes() : null;
    }

    public int getPuertoEscucha() {
        // Retorna el puerto donde el cliente espera respuestas.
        // Si usas puerto 0, deberías obtener el puerto real del socket servidor.
        // Por ahora hardcodeamos 5555 o el puerto fijo que decidas usar para el cliente.
        return 5555;
    }
}