package Receptor;

import ObserverReceptor.ObservadorRecibos;
import org.itson.componentereceptor.IReceptor;
import org.itson.paquetedto.PaqueteDTO;

public class Receptor implements ObservadorRecibos {
    private ColaRecibos cola;
    private IReceptor componenteLogica; // Quien procesa la info (LogicaCliente / PublicadorEventos)

    public void setCola(ColaRecibos cola) { this.cola = cola; }
    public void setReceptor(IReceptor r) { this.componenteLogica = r; }

    @Override
    public void actualizar() {
        PaqueteDTO paquete = cola.dequeue();
        if (paquete != null && componenteLogica != null) {
            componenteLogica.recibirCambio(paquete);
        }
    }
}