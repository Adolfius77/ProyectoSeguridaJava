package EnsambladorConfig;

import Cifrado.GestorSeguridad;
import Emisor.ClienteTCP;
import Emisor.ColaEnvios;
import Emisor.Emisor;
import ModeloChatTCP.LogicaCliente;
import ModeloChatTCP.ReceptorPaquete;
import Receptor.ColaRecibos;
import Receptor.Receptor;
import Receptor.ServidorTCP;
import org.itson.componenteemisor.IEmisor;
import org.itson.componentereceptor.IReceptor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;

/**
 * EnsambladorConfig singleton que carga propiedades, inicializa red, GestorSeguridad y crea LogicaCliente para el cliente de chat.
 *
 * Gestiona el intercambio automático de llaves públicas entre cliente y servidor mediante: 1. Generación automática de par de llaves RSA (pública/privada) en GestorSeguridad 2. Intercambio automático de llaves públicas en cada conexión TCP 3. Cifrado/descifrado híbrido RSA + AES-GCM de todos los mensajes
 */
public class EnsambladorConfig {

    private static EnsambladorConfig instancia;

    private Properties propsServidor;
    private Properties propsUsuario;

    private IEmisor emisorCliente;
    private GestorSeguridad gestorSeguridad;

    private ColaEnvios colaEnvios;
    private ColaRecibos colaRecibos;
    private ServidorTCP servidorTCP;
    private ClienteTCP clienteTCP;

    private LogicaCliente logicaCliente;

    private EnsambladorConfig() throws IOException {
        propsServidor = cargarPropiedades("config_ServidorMain.properties");
        propsUsuario = cargarPropiedades("config_Usuario.properties");

        // Crear GestorSeguridad compartido - genera automáticamente par de llaves RSA
        try {
            gestorSeguridad = new GestorSeguridad();
            byte[] llavePublica = gestorSeguridad.obtenerPublicaBytes();
            System.out.println("[EnsambladorConfig] GestorSeguridad inicializado con éxito");
            System.out.println("[EnsambladorConfig] Llave pública RSA generada (Base64): "
                    + Base64.getEncoder().encodeToString(llavePublica).substring(0, 50) + "...");
        } catch (Exception e) {
            System.err.println("[EnsambladorConfig] Error al crear GestorSeguridad: " + e.getMessage());
            throw new IOException("No se pudo inicializar el sistema de cifrado", e);
        }

        // Inicializar emisor, receptor y servidor TCP con cifrado habilitado
        inicializarRed();

        // Inicializar lógica cliente
        inicializarLogicaCliente();

        System.out.println("[EnsambladorConfig] ✓ Cliente configurado y listo para comunicación cifrada");
    }

    public static EnsambladorConfig getInstancia() throws IOException {
        if (instancia == null) {
            instancia = new EnsambladorConfig();
        }
        return instancia;
    }

    private Properties cargarPropiedades(String archivo) throws IOException {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(archivo)) {
            if (input == null) {
                throw new IOException("No se encontró el archivo de configuración: " + archivo);
            }
            props.load(input);
        }
        return props;
    }

    private void inicializarRed() {
        // --- Emisor (envío de mensajes cifrados) ---
        colaEnvios = new ColaEnvios();
        String hostUsuario = propsUsuario.getProperty("host");
        int puertoEnvioUsuario = Integer.parseInt(propsUsuario.getProperty("puerto.user"));

        // ClienteTCP comparte el mismo GestorSeguridad para usar las mismas llaves
        clienteTCP = new ClienteTCP(colaEnvios, puertoEnvioUsuario, hostUsuario, gestorSeguridad);
        colaEnvios.agregarObservador(clienteTCP);
        emisorCliente = new Emisor(colaEnvios);

        System.out.println("[EnsambladorConfig] Emisor inicializado - enviará mensajes CIFRADOS automáticamente");

        // --- Receptor (recepción de mensajes cifrados) ---
        colaRecibos = new ColaRecibos();
        int puertoEntradaUsuario = Integer.parseInt(propsUsuario.getProperty("puerto.entrada"));

        // ServidorTCP comparte el mismo GestorSeguridad para descifrar con la misma llave privada
        servidorTCP = new ServidorTCP(colaRecibos, puertoEntradaUsuario, gestorSeguridad);

        System.out.println("[EnsambladorConfig] Receptor TCP escuchando en puerto " + puertoEntradaUsuario);
        System.out.println("[EnsambladorConfig] El servidor TCP recibirá y descifrará mensajes automáticamente");

        // Iniciar servidor TCP en hilo separado
        new Thread(() -> servidorTCP.iniciar(), "ServidorTCP-Cliente").start();
    }

    private void inicializarLogicaCliente() {
        // Crear LogicaCliente
        logicaCliente = new LogicaCliente(emisorCliente, null, "config_ServidorMain.properties");

        // Crear ReceptorPaquete y asociarlo a LogicaCliente
        IReceptor receptorCliente = new ReceptorPaquete(logicaCliente);
        logicaCliente.setReceptor(receptorCliente);

        // Asociar receptor a la cola de recibos
        Receptor receptor = new Receptor();
        receptor.setCola(colaRecibos);
        receptor.setReceptor(receptorCliente);

        System.out.println("[EnsambladorConfig] LogicaCliente y ReceptorPaquete inicializados y listos para uso");
    }

    // ------------------- Getters -------------------
    public IEmisor getEmisorCliente() {
        return emisorCliente;
    }

    public LogicaCliente getLogicaCliente() {
        return logicaCliente;
    }

    public GestorSeguridad getGestorSeguridad() {
        return gestorSeguridad;
    }

    public String getHostServidor() {
        return propsServidor.getProperty("host");
    }

    public int getPuertoServidor() {
        return Integer.parseInt(propsServidor.getProperty("puerto.servidor"));
    }

    public int getPuertoEntradaServidor() {
        return Integer.parseInt(propsServidor.getProperty("puerto.entrada"));
    }

    public String getHostUsuario() {
        return propsUsuario.getProperty("host");
    }

    public int getPuertoEnvioUsuario() {
        return Integer.parseInt(propsUsuario.getProperty("puerto.user"));
    }

    public int getPuertoEntradaUsuario() {
        return Integer.parseInt(propsUsuario.getProperty("puerto.entrada"));
    }

    public ClienteTCP getClienteTCP() {
        return clienteTCP;
    }

    public ServidorTCP getServidorTCP() {
        return servidorTCP;
    }

    // ------------------- Main para prueba -------------------
    public static void main(String[] args) {
        try {
            System.out.println("=== Inicializando Cliente de Chat con Cifrado Automático ===\n");

            EnsambladorConfig ensamblador = EnsambladorConfig.getInstancia();
            LogicaCliente logica = ensamblador.getLogicaCliente();

            // Obtener llave pública en Base64 (solo para información)
            byte[] publicKeyBytes = ensamblador.getGestorSeguridad().obtenerPublicaBytes();
            String publicKeyString = Base64.getEncoder().encodeToString(publicKeyBytes);

            System.out.println("\n[Main] Llave pública del cliente (primeros 80 caracteres):");
            System.out.println(publicKeyString.substring(0, Math.min(80, publicKeyString.length())) + "...\n");

            // Ejemplo de función en LogicaCliente
            // El intercambio de llaves públicas ocurre automáticamente en ClienteTCP
            logica.registrar(
                    "EsteSiExiteste",
                    "murrieta09",
                    ensamblador.getHostServidor(), // enviar al servidor
                    ensamblador.getPuertoServidor(), // puerto del servidor
                    publicKeyString
            );

            System.out.println("\n[Main] ✓ Mensaje de registro enviado con cifrado automático RSA + AES-GCM");
            System.out.println("[Main] ✓ El intercambio de llaves públicas se realizó automáticamente");
            System.out.println("[Main] ✓ Cliente listo para enviar/recibir mensajes cifrados\n");

        } catch (IOException e) {
            System.err.println("\n[ERROR] No se pudo inicializar el cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
