package EventBus;

import Servicio.Servicio;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.itson.componenteemisor.IEmisor;

import org.itson.paquetedto.PaqueteDTO;

/**
 * Bus que recibe y canaliza mensajes recibidos por servicios.
 *
 * @author Jck Murrieta
 */
public class EventBus {

    private Map<String, List<Servicio>> servicios;
    private IEmisor emisor;

    public EventBus() {
        this.servicios = new ConcurrentHashMap<>();

    }

    public void setEmisor(IEmisor emisor) {
        this.emisor = emisor;
    }

    public void publicarEvento(PaqueteDTO paquete) {
        Servicio origen = new Servicio(paquete.getPuertoOrigen(), paquete.getHost());
        normalizarPaquete(paquete, origen);

        if (paquete.getTipoEvento().equalsIgnoreCase("INICIAR_CONEXION")) {
            Servicio nuevoServicio = new Servicio(paquete.getPuertoOrigen(), paquete.getHost());

            List<String> eventos = (List<String>) paquete.getContenido();

            if (eventos != null) {
                for (String evento : eventos) {
                    registrarServicio(evento, nuevoServicio);
                }
            }

            System.out.println("[EventBus] Servicio registrado: " + nuevoServicio);
            return;
        }

        notificarServicios(paquete);
    }

    public void registrarServicio(String tipoEvento, Servicio servicio) {
        List<Servicio> lista = servicios.computeIfAbsent(tipoEvento, serviciosSuscritos -> new ArrayList<>());
        for (Servicio s : lista) {
            if (s.getHost().equals(servicio.getHost()) && s.getPuerto() == servicio.getPuerto()) {

                return;
            }
        }
        lista.add(servicio);
    }

    public void eliminarServicio(String tipoEvento, Servicio servicio) {
        List<Servicio> lista = servicios.get(tipoEvento);
        if (lista != null) {
            lista.remove(servicio);
        }
    }

    //notificar servicios por red
    public void notificarServicios(PaqueteDTO paquete) {
        //  Obtener suscriptores del tipo de evento del paquete
        List<Servicio> lista = servicios.get(paquete.getTipoEvento());

        if (lista != null) {

            for (Servicio servicio : lista) {
                if (Objects.equals(servicio.getHost(), paquete.getHost())
                        && servicio.getPuerto() == paquete.getPuertoOrigen()) {
                    continue;
                }

                paquete.setHost(servicio.getHost());
                paquete.setPuertoDestino(servicio.getPuerto());
                emisor.enviarCambio(paquete);
            }
        }
    }

    public void agregarEvento(String nombreEvento, Servicio servicio) {
        List<Servicio> lista = servicios.get(nombreEvento);
        if (lista == null) {
            lista = new ArrayList<>();
            servicios.put(nombreEvento, lista);
        }
        for (Servicio s : lista) {
            boolean mismoHost = s.getHost().equals(servicio.getHost());
            boolean mismoPuerto = s.getPuerto() == servicio.getPuerto();

            if (mismoHost && mismoPuerto) {
                System.out.println("[EventBus] Servicio ya registrado para este evento.");
                return;
            }
        }
        lista.add(servicio);
    }

    private void normalizarPaquete(PaqueteDTO paquete, Servicio origen) {

        // Si el host viene vacío o null, se lo ponemos
        if (paquete.getHost() == null || paquete.getHost().isEmpty()) {
            paquete.setHost(origen.getHost());
        }

        // Si el puertoOrigen viene en 0, se lo ponemos
        if (paquete.getPuertoOrigen() == 0) {
            paquete.setPuertoOrigen(origen.getPuerto());
        }
    }

    //obtener el host de la partida
    public Servicio obtenerHostPartida() {
        //el host es el unico suscriptor a este evento
        List<Servicio> lista = servicios.get("SOLICITAR_UNIRSE");
        if (lista == null || lista.isEmpty()) {
            return null;
        }
        return lista.get(0);
    }

    public void enviarHost(PaqueteDTO paquete) {
        Servicio host = obtenerHostPartida();

        PaqueteDTO respuesta = new PaqueteDTO();
        respuesta.setTipoEvento("RESPUESTA_HOST");

        respuesta.setHost(paquete.getHost()); // ip del solicitante

        respuesta.setPuertoDestino(paquete.getPuertoOrigen()); // el solicitante va a recibir la respuesta

        respuesta.setPuertoOrigen(host != null ? host.getPuerto() : 0);

        respuesta.setContenido(host);

        emisor.enviarCambio(respuesta);

    }

}
