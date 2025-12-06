/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Receptor;

import ObserverReceptor.ObservableRecibos;
import ObserverReceptor.ObservadorRecibos;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.itson.paquetedto.PaqueteDTO;

/**
 *
 * @author Jck Murrieta
 */
public class ColaRecibos implements ObservableRecibos {

    private Queue<String> cola = new LinkedList<>();
    private Gson serializador = new Gson();
    private ObservableRecibos observadorRecibos;
    private List<ObservadorRecibos> observadores = new ArrayList<>();

    @Override
    public void agregarObservador(ObservadorRecibos ob) {
        observadores.add(ob);
    }

    @Override
    public void notificar() {
        for (ObservadorRecibos ob : observadores) {
            System.out.println("[ColaRecibos] aver ya basta: " + ob.toString());
            ob.actualizar();
        }
    }

    public void queue(String paquete) {
        System.out.println("[ColaRecibos] Recibiendo paquete JSON para procesar...");
        cola.add(paquete);
        System.out.println("[ColaRecibos] Paquete agregado a cola, notificando observadores...");
        notificar();
        System.out.println("[ColaRecibos] Paquete completo: " + paquete);
    }

    public PaqueteDTO<?> dequeue() {
        String paquete = cola.poll();
        return deserializar(paquete);
    }

    public PaqueteDTO<?> deserializar(String paquete) {
        return serializador.fromJson(paquete, PaqueteDTO.class);
    }
}
