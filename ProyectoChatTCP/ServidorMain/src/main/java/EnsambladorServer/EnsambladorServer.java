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
import java.util.Properties;
import org.itson.componenteemisor.IEmisor;
import org.itson.componentereceptor.IReceptor;

/**
 *
 * @author Jack Murieta
 */
public class EnsambladorServer {

    private String host;
    private int puertoEntrada;
    private int puertoSalida;

    private ServidorMain servidorMain;
    private IEmisor emisorServidor;

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
        // Inicializar ServidorMain
        servidorMain = new ServidorMain(null); // emisor se asignará después

        // Crear UN SOLO GestorSeguridad compartido para el ServidorMain
        GestorSeguridad gestorSeguridad = null;
        try {
            gestorSeguridad = new GestorSeguridad();
            System.out.println("[ServidorMain] GestorSeguridad creado y compartido entre ClienteTCP y ServidorTCP");
        } catch (Exception e) {
            System.err.println("[ServidorMain] Error al crear GestorSeguridad: " + e.getMessage());
        }

        // --- Emisor (usa el gestor compartido) ---
        ColaEnvios colaEnvios = new ColaEnvios();
        ClienteTCP clienteTCP = new ClienteTCP(colaEnvios, puertoSalida, host, gestorSeguridad);
        colaEnvios.agregarObservador(clienteTCP);
        emisorServidor = new Emisor(colaEnvios);
        servidorMain.setEmisor(emisorServidor);

        // --- Receptor (usa el mismo gestor compartido) ---
        ColaRecibos colaRecibos = new ColaRecibos();
        ServidorTCP servidorTCP = new ServidorTCP(colaRecibos, puertoEntrada, gestorSeguridad);

        IReceptor receptorServidor = new ReceptorServidor(servidorMain);
        Receptor receptor = new Receptor();
        receptor.setCola(colaRecibos);
        receptor.setReceptor(receptorServidor);

        colaRecibos.agregarObservador(receptor);

        // --- Iniciar servidor TCP en hilo separado ---
        new Thread(() -> servidorTCP.iniciar()).start();

        System.out.println("[ServidorMain] Servicio iniciado en puerto " + puertoEntrada);
    }

    public static void main(String[] args) {
        try {
            EnsambladorServer ensamblador = new EnsambladorServer("config_servidorMain.properties");
            ensamblador.iniciar();
        } catch (IOException e) {
            System.err.println("Error al iniciar ServidorMain: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
