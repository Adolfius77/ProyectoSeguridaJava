package Emisor;

import org.itson.componenteemisor.IEmisor;
import org.itson.paquetedto.PaqueteDTO;

/**
 *
 * @author Jck Murrieta
 */
public class Emisor implements IEmisor{
    private ColaEnvios cola;
    
    public Emisor(ColaEnvios cola) {
        this.cola = cola;
      
    }

    @Override
    public void enviarCambio(PaqueteDTO paquete) {
        if (paquete == null) {
            throw new IllegalArgumentException("El paquete no puede ser null");
        }
        System.out.println("[Emisor] cambio recibio:" + paquete.getContenido().toString());
        cola.queue(paquete); 
    }
}
