
package EventBus;

import Datos.RepositorioUsuarios;
import Servicio.Servicio;
import com.google.gson.internal.LinkedTreeMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.itson.componenteemisor.IEmisor;
import org.itson.paquetedto.PaqueteDTO;


public class EventBus {

    private Map<String, List<Servicio>> servicios;
    private List<String> usuariosConectados;
    private IEmisor emisor;

    public EventBus() {
        this.servicios = new ConcurrentHashMap<>();
        this.usuariosConectados = new ArrayList<>();
    }

    public void setEmisor(IEmisor emisor) {
        this.emisor = emisor;
    }

    public void publicarEvento(PaqueteDTO paquete) {
        
        Servicio origen = new Servicio(paquete.getPuertoOrigen(), paquete.getHost());
        if (paquete.getHost() == null) paquete.setHost(origen.getHost());
        if (paquete.getPuertoOrigen() == 0) paquete.setPuertoOrigen(origen.getPuerto());

        String tipo = paquete.getTipoEvento().toUpperCase();

      
        if (tipo.equals("REGISTRO")) {
            procesarRegistro(paquete);
            return;
        } else if (tipo.equals("LOGIN")) {
            procesarLogin(paquete);
            return;
        }else if(tipo.equals("INICIAR_CONEXION")){
           Servicio nuevoServicio = new Servicio(paquete.getPuertoOrigen(), paquete.getHost());
             registrarServicio("MENSAJE", nuevoServicio); // Suscripción por defecto
             System.out.println("[EventBus] Cliente conectado: " + nuevoServicio);
             return; 
        }else if(tipo.equals("SOLICITAR_USUARIOS")){
           enviarListaUsuarios();
           return;
             
        }

        notificarServicios(paquete);
    }
    private void enviarListaUsuarios(PaqueteDTO paquete){
        LinkedTreeMap data = (LinkedTreeMap) paquete.getContenido();
        String user = (String) data.get("nombreUsuario");
        String pass = (String) data.get("contrasena");

        if (RepositorioUsuarios.validar(user, pass)) {
            Servicio s = new Servicio(paquete.getPuertoOrigen(), paquete.getHost());
            registrarServicio("MENSAJE", s);
            registrarServicio("LISTA_USUARIOS", s);
            
            
            if (!usuariosConectados.contains(user)) {
                usuariosConectados.add(user);
            }
            
            enviarRespuesta(paquete, "LOGIN_OK", user);
            
            
            enviarListaUsuarios();
        } else {
            enviarRespuesta(paquete, "ERROR", "Credenciales Incorrectas");
        }
    }
    private void enviarListaUsuarios() {
     
        PaqueteDTO paqueteLista = new PaqueteDTO();
        paqueteLista.setTipoEvento("LISTA_USUARIOS");
        paqueteLista.setContenido(new ArrayList<>(usuariosConectados));
        paqueteLista.setHost("localhost");
        
        
        List<Servicio> suscriptores = servicios.get("LISTA_USUARIOS");
        if (suscriptores != null) {
            for (Servicio s : suscriptores) {
                paqueteLista.setPuertoDestino(s.getPuerto());
                emisor.enviarCambio(paqueteLista);
            }
        }
    }

    private void procesarRegistro(PaqueteDTO paquete) {
        LinkedTreeMap data = (LinkedTreeMap) paquete.getContenido();
        String user = (String) data.get("nombreUsuario");
        String pass = (String) data.get("contrasena");

        boolean exito = RepositorioUsuarios.registrar(user, pass);
        enviarRespuesta(paquete, exito ? "REGISTRO_OK" : "ERROR", 
                        exito ? "Registro exitoso" : "Usuario ya existe");
    }

    private void procesarLogin(PaqueteDTO paquete) {
        LinkedTreeMap data = (LinkedTreeMap) paquete.getContenido();
        String user = (String) data.get("nombreUsuario");
        String pass = (String) data.get("contrasena");

        if (RepositorioUsuarios.validar(user, pass)) {
            // Suscribir al usuario a los eventos de chat
            Servicio s = new Servicio(paquete.getPuertoOrigen(), paquete.getHost());
            registrarServicio("MENSAJE", s);
            
            enviarRespuesta(paquete, "LOGIN_OK", user);
        } else {
            enviarRespuesta(paquete, "ERROR", "Credenciales Incorrectas");
        }
    }

    private void enviarRespuesta(PaqueteDTO origen, String tipo, Object contenido) {
        PaqueteDTO resp = new PaqueteDTO();
        resp.setTipoEvento(tipo);
        resp.setContenido(contenido);
        // Invertir destino
        resp.setHost(origen.getHost());
        resp.setPuertoDestino(origen.getPuertoOrigen());
        resp.setPuertoOrigen(5556);
        
        emisor.enviarCambio(resp);
    }

    public void registrarServicio(String tipoEvento, Servicio servicio) {
        List<Servicio> lista = servicios.computeIfAbsent(tipoEvento, k -> new ArrayList<>());
        for(Servicio s : lista) {
            if(s.getHost().equals(servicio.getHost()) && s.getPuerto() == servicio.getPuerto()) return;
        }
        lista.add(servicio);
    }
    
    public void notificarServicios(PaqueteDTO paquete) {
        List<Servicio> lista = servicios.get(paquete.getTipoEvento());
        if (lista != null) {
            for (Servicio servicio : lista) {
                // No enviar al mismo origen
                if (Objects.equals(servicio.getHost(), paquete.getHost()) && 
                    servicio.getPuerto() == paquete.getPuertoOrigen()) continue;

                paquete.setHost(servicio.getHost());
                paquete.setPuertoDestino(servicio.getPuerto());
                emisor.enviarCambio(paquete);
            }
        }
    }
    
    // Método necesario para compilación si lo usas en PublicadorEventos
    public void enviarHost(PaqueteDTO p) {} 
}
