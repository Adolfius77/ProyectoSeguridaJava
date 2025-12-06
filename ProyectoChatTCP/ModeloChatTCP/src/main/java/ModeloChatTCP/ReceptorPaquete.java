package ModeloChatTCP;

import org.itson.componentereceptor.IReceptor;
import org.itson.paquetedto.PaqueteDTO;

/**
 * Receptor auxiliar.
 * Nota: Actualmente LogicaCliente maneja la recepción directamente.
 */
public class ReceptorPaquete implements IReceptor {
    
    public ReceptorPaquete() {
    }

    @Override
    public void recibirCambio(PaqueteDTO paquete) {
       
        System.out.println("[ReceptorPaquete] Paquete recibido en componente auxiliar: " + paquete.getTipoEvento());
    }
}