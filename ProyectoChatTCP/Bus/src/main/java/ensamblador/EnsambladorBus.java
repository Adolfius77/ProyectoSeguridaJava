package ensamblador;

import Cifrado.GestorSeguridad;
import Emisor.ClienteTCP;
import Emisor.ColaEnvios;
import Emisor.Emisor;
import EventBus.EventBus;
import PublicadorEventos.PublicadorEventos;
import Receptor.ColaRecibos;
import Receptor.Receptor;
import Receptor.ServidorTCP;
import org.itson.componenteemisor.IEmisor;
import org.itson.componentereceptor.IReceptor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Ensamblador para iniciar el EventBus como servicio independiente.
 *
 * @author Jck Murrieta
 */
public class EnsambladorBus {

    private String host;
    private int puertoEntrada;
    private int puertoBus;

    private EventBus eventBus;
    private IEmisor emisorBus;

    public EnsambladorBus(String configFile) throws IOException {
        Properties props = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream(configFile);
        if (input == null) {
            throw new IOException("No se encontro el archivo de configuracion: " + configFile);
        }
        props.load(input);

        this.host = props.getProperty("host");
        this.puertoEntrada = Integer.parseInt(props.getProperty("puerto.entrada"));
        this.puertoBus = Integer.parseInt(props.getProperty("puerto.bus"));
    }

    public void iniciar() {
        eventBus = new EventBus();

        // Crear UN SOLO GestorSeguridad compartido para el EventBus
        GestorSeguridad gestorSeguridad = null;
        try {
            gestorSeguridad = new GestorSeguridad();
            System.out.println("[EventBus] GestorSeguridad creado y compartido entre ClienteTCP y ServidorTCP");
        } catch (Exception e) {
            System.err.println("[EventBus] Error al crear GestorSeguridad: " + e.getMessage());
        }

        // emisor del bus (usa el gestor compartido)
        ColaEnvios colaEnviosBus = new ColaEnvios();
        ClienteTCP clienteTCPBus = new ClienteTCP(colaEnviosBus, puertoBus, host, gestorSeguridad);
        colaEnviosBus.agregarObservador(clienteTCPBus);
        emisorBus = new Emisor(colaEnviosBus);
        eventBus.setEmisor(emisorBus);

        // receptor (usa el mismo gestor compartido)
        ColaRecibos colaRecibosBus = new ColaRecibos();
        ServidorTCP servidorTCPBus = new ServidorTCP(colaRecibosBus, puertoEntrada, gestorSeguridad);
        IReceptor publicador = new PublicadorEventos(puertoBus, host, eventBus);
        Receptor receptorBus = new Receptor();
        receptorBus.setCola(colaRecibosBus);
        receptorBus.setReceptor(publicador);
        colaRecibosBus.agregarObservador(receptorBus);

        new Thread(() -> servidorTCPBus.iniciar()).start();
        System.out.println("[EventBus] Servicio iniciado en puerto " + puertoEntrada);
    }

    public static void main(String[] args) {
        try {
            EnsambladorBus ensamblador = new EnsambladorBus("config_bus.properties");
            ensamblador.iniciar();
        } catch (IOException e) {
            System.err.println("Error al iniciar el EventBus: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
