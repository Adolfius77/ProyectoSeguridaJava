package PublicadorEventos;

import EventBus.EventBus;
import Logs.Log;
import org.itson.componentereceptor.IReceptor;
import org.itson.paquetedto.PaqueteDTO;

/**
 *
 * @author Jck Murrieta
 */
public class PublicadorEventos implements IReceptor {

    private int puerto;
    private String host;
    private EventBus eventBus;

    public PublicadorEventos(int puerto, String host, EventBus eventBus) {
        this.puerto = puerto;
        this.host = host;
        this.eventBus = eventBus;
    }

    @Override
    public void recibirCambio(PaqueteDTO paquete) {
        try {
            if (paquete.getTipoEvento().equalsIgnoreCase("OBTENER_HOST")) {
                eventBus.enviarHost(paquete);
                return;
            }

            System.out.println("[PublicadorEventos] Evento recibido:" + paquete.getTipoEvento());
            eventBus.publicarEvento(paquete);

        } catch (Exception e) {
            Log.registrar("ERROR", "Error al procesar evento '" + paquete.getTipoEvento() + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getPuerto() {
        return puerto;
    }

    public String getHost() {
        return host;
    }

}
