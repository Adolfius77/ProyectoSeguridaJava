package Receptor;

import ObserverReceptor.ObservableRecibos;
import ObserverReceptor.ObservadorRecibos;
import com.google.gson.Gson;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;
import org.itson.paquetedto.PaqueteDTO;

public class ColaRecibos implements ObservableRecibos {
    private Queue<String> cola = new LinkedList<>();
    private Gson gson = new Gson();
    private List<ObservadorRecibos> observadores = new ArrayList<>();

    @Override
    public void agregarObservador(ObservadorRecibos ob) {
        observadores.add(ob);
    }

    @Override
    public void notificar() {
        for (ObservadorRecibos ob : observadores) ob.actualizar();
    }

    public synchronized void queue(String json) {
        cola.add(json);
        notificar();
    }

    public synchronized PaqueteDTO dequeue() {
        String json = cola.poll();
        if (json == null) return null;
        try {
            return gson.fromJson(json, PaqueteDTO.class);
        } catch (Exception e) { return null; }
    }
}