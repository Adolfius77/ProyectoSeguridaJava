package Emisor;

import ObserverEmisor.ObservableEnvios;
import ObserverEmisor.ObservadorEnvios;
import com.google.gson.Gson;
import java.util.LinkedList;
import java.util.Queue;
import org.itson.paquetedto.PaqueteDTO;

public class ColaEnvios implements ObservableEnvios {
    private Queue<PaqueteDTO> cola = new LinkedList<>();
    private Gson gson = new Gson();
    private ObservadorEnvios observador; // Solo un cliente TCP observando

    @Override
    public void agregarObservador(ObservadorEnvios ob) {
        this.observador = ob;
    }

    @Override
    public void notificar() {
        if(observador != null) observador.actualizar();
    }

    public synchronized void queue(PaqueteDTO paquete) {
        cola.offer(paquete);
        notificar();
    }

    public synchronized String dequeue() {
        PaqueteDTO p = cola.poll();
        return p != null ? gson.toJson(p) : null;
    }
}