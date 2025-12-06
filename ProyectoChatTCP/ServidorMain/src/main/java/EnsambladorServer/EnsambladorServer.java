/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EnsambladorServer;

import Cifrado.GestorSeguridad;
import Emisor.ClienteTCP;
import Emisor.ColaEnvios;
import Emisor.Emisor;
import Receptor.ColaRecibos;
import Receptor.Receptor;
import Receptor.ServidorTCP;
import ServidorMain.ReceptorServidor;
import ServidorMain.ServidorMain;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;
import org.itson.componenteemisor.IEmisor;
import org.itson.componentereceptor.IReceptor;

/**
 * EnsambladorServer configura el servidor con cifrado automático RSA + AES-GCM
 *
 * Funcionalidades:
 * 1. Genera automáticamente un par de llaves RSA (pública/privada) única para el servidor
 * 2. Intercambia automáticamente llaves públicas con cada cliente que se conecta
 * 3. Cifra todos los mensajes salientes con la llave pública del destinatario
 * 4. Descifra todos los mensajes entrantes con su llave privada
 *
 * @author Jack Murieta
 */
public class EnsambladorServer {

    private String host;
    private int puertoEntrada;
    private int puertoSalida;

    private ServidorMain servidorMain;
    private IEmisor emisorServidor;
    private GestorSeguridad gestorSeguridad;
    private ClienteTCP clienteTCP;
    private ServidorTCP servidorTCP;

    public EnsambladorServer(String config_servidorMain) throws IOException {
        Properties props = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream(config_servidorMain);
        if (input == null) {
            throw new IOException("No se encontró el archivo de configuración: " + config_servidorMain);
        }
        props.load(input);

        this.host = props.getProperty("host");

        // Puerto de entrada
        this.puertoEntrada = Integer.parseInt(props.getProperty("puerto.entrada"));

        // Puerto de salida (intenta primero 'puerto.salida', si no existe usa 'puerto.servidor')
        String salida = props.getProperty("puerto.salida");
        if (salida != null) {
            this.puertoSalida = Integer.parseInt(salida);
        } else {
            this.puertoSalida = Integer.parseInt(props.getProperty("puerto.servidor"));
        }
    }

    public void iniciar() {
        System.out.println("\n=== Inicializando Servidor con Cifrado Automático ===\n");

        // Inicializar ServidorMain
        servidorMain = new ServidorMain(null); // emisor se asignará después

        // Crear UN SOLO GestorSeguridad compartido para el ServidorMain
        // Esto genera automáticamente el par de llaves RSA del servidor
        try {
            gestorSeguridad = new GestorSeguridad();
            byte[] llavePublica = gestorSeguridad.obtenerPublicaBytes();
            System.out.println("[ServidorMain] ✓ GestorSeguridad creado con éxito");
            System.out.println("[ServidorMain] ✓ Par de llaves RSA generado automáticamente");
            System.out.println("[ServidorMain] Llave pública del servidor (Base64, primeros 50 caracteres):");
            System.out.println("  " + Base64.getEncoder().encodeToString(llavePublica).substring(0, 50) + "...\n");
        } catch (Exception e) {
            System.err.println("[ServidorMain] ✗ Error al crear GestorSeguridad: " + e.getMessage());
            System.err.println("[ServidorMain] El servidor NO podrá cifrar/descifrar mensajes");
            return;
        }

        // --- Emisor (envía mensajes cifrados con el gestor compartido) ---
        ColaEnvios colaEnvios = new ColaEnvios();
        clienteTCP = new ClienteTCP(colaEnvios, puertoSalida, host, gestorSeguridad);
        colaEnvios.agregarObservador(clienteTCP);
        emisorServidor = new Emisor(colaEnvios);
        servidorMain.setEmisor(emisorServidor);

        System.out.println("[ServidorMain] ✓ Emisor configurado - enviará mensajes CIFRADOS automáticamente");

        // --- Receptor (recibe y descifra mensajes con el mismo gestor compartido) ---
        ColaRecibos colaRecibos = new ColaRecibos();
        servidorTCP = new ServidorTCP(colaRecibos, puertoEntrada, gestorSeguridad);

        IReceptor receptorServidor = new ReceptorServidor(servidorMain);
        Receptor receptor = new Receptor();
        receptor.setCola(colaRecibos);
        receptor.setReceptor(receptorServidor);

        colaRecibos.agregarObservador(receptor);

        System.out.println("[ServidorMain] ✓ Receptor configurado en puerto " + puertoEntrada);
        System.out.println("[ServidorMain] ✓ El servidor recibirá y descifrará mensajes automáticamente");

        // --- Iniciar servidor TCP en hilo separado ---
        new Thread(() -> servidorTCP.iniciar(), "ServidorTCP-Main").start();

        System.out.println("\n[ServidorMain] ╔══════════════════════════════════════════════════════╗");
        System.out.println("[ServidorMain] ║  Servidor iniciado correctamente                     ║");
        System.out.println("[ServidorMain] ║  Puerto entrada: " + String.format("%-34d", puertoEntrada) + "║");
        System.out.println("[ServidorMain] ║  Puerto salida:  " + String.format("%-34d", puertoSalida) + "║");
        System.out.println("[ServidorMain] ║  Cifrado: RSA 2048 + AES-GCM 256                     ║");
        System.out.println("[ServidorMain] ║  Intercambio de llaves: AUTOMÁTICO                   ║");
        System.out.println("[ServidorMain] ╚══════════════════════════════════════════════════════╝\n");
    }

    // ------------------- Getters -------------------

    public GestorSeguridad getGestorSeguridad() {
        return gestorSeguridad;
    }

    public ClienteTCP getClienteTCP() {
        return clienteTCP;
    }

    public ServidorTCP getServidorTCP() {
        return servidorTCP;
    }

    public ServidorMain getServidorMain() {
        return servidorMain;
    }

    public IEmisor getEmisorServidor() {
        return emisorServidor;
    }

    // ------------------- Main -------------------

    public static void main(String[] args) {
        try {
            System.out.println("╔═══════════════════════════════════════════════════════════╗");
            System.out.println("║    SERVIDOR CHAT TCP CON CIFRADO AUTOMÁTICO RSA + AES    ║");
            System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

            EnsambladorServer ensamblador = new EnsambladorServer("config_servidorMain.properties");
            ensamblador.iniciar();

            System.out.println("[Main] Servidor en ejecución. Presiona Ctrl+C para detener.\n");

            // Mantener el programa ejecutándose
            Thread.currentThread().join();

        } catch (IOException e) {
            System.err.println("\n[ERROR] Error al iniciar ServidorMain: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("\n[Main] Servidor detenido por el usuario.");
        }
    }
}
