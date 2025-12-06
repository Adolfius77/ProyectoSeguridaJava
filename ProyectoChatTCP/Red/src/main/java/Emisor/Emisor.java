package Emisor;

import org.itson.componenteemisor.IEmisor;
import org.itson.paquetedto.PaqueteDTO;

public class Emisor implements IEmisor {
    private ColaEnvios cola;

    public Emisor(ColaEnvios cola) {
        this.cola = cola;
    }

    @Override
    public void enviarCambio(PaqueteDTO paquete) {
        if(paquete != null) {
            cola.queue(paquete);
        }
    }
}