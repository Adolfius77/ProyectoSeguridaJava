package PublicadorEventos;

import EventBus.EventBus;
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
        if (paquete.getTipoEvento().equalsIgnoreCase("OBTENER_HOST")) {
            eventBus.enviarHost(paquete);
            return;
        }

        System.out.println("[PublicadorEventos] Evento recibido:" + paquete.getTipoEvento());
        eventBus.publicarEvento(paquete);
    }

    public int getPuerto() {
        return puerto;
    }

    public String getHost() {
        return host;
    }

}
