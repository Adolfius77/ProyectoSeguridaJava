package EventBus;

import Datos.RepositorioUsuarios;
import Logs.Log;
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

    private static final int MAX_USUARIOS = 5;

    public EventBus() {
        this.servicios = new ConcurrentHashMap<>();
        this.usuariosConectados = new ArrayList<>();
        Log.registrar("INFO", "EventBus iniciado. Limite de usuarios configurado: " + MAX_USUARIOS);
    }

    public void setEmisor(IEmisor emisor) {
        this.emisor = emisor;
    }

    public void publicarEvento(PaqueteDTO paquete) {

        Servicio origen = new Servicio(paquete.getPuertoOrigen(), paquete.getHost());
        if (paquete.getHost() == null) {
            paquete.setHost(origen.getHost());
        }
        if (paquete.getPuertoOrigen() == 0) {
            paquete.setPuertoOrigen(origen.getPuerto());
        }

        String tipo = paquete.getTipoEvento().toUpperCase();

        if (tipo.equals("REGISTRO")) {
            procesarRegistro(paquete);
            return;
        } else if (tipo.equals("LOGIN")) {
            procesarLogin(paquete);
            return;

        } else if (tipo.equals("INICIAR_CONEXION")) {
            Servicio nuevoServicio = new Servicio(paquete.getPuertoOrigen(), paquete.getHost());
            registrarServicio("MENSAJE", nuevoServicio); // Suscripción por defecto
            System.out.println("[EventBus] Cliente conectado: " + nuevoServicio);

            Log.registrar("RED", "Cliente conectado físicamente: " + nuevoServicio);
            return;

        } else if (tipo.equals("SOLICITAR_USUARIOS")) {
            Servicio solicitante = new Servicio(paquete.getPuertoOrigen(), paquete.getHost());
            registrarServicio("LISTA_USUARIOS", solicitante);

            enviarListaUsuarios();
            return;
            
        }else if(tipo.equals("LOGOUT")){
            procesarRegistro(paquete);
            return;
        }

        notificarServicios(paquete);
    }

    private void procesarLogout(PaqueteDTO paquete){
        String user = (String) paquete.getContenido();
        if(usuariosConectados.remove(user)){
            Log.registrar("INFO", "Usuario desconectado" + user);
            Log.registrar("INFO", "cupos disponibles" + (MAX_USUARIOS - usuariosConectados.size()));
            enviarListaUsuarios();
        }
    }
    public int getCantidadUsuariosConectados() {
        return usuariosConectados.size();
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
    private void numUsuariosConectados(){
        usuariosConectados.size();
    }
    private void procesarRegistro(PaqueteDTO paquete) {
        LinkedTreeMap data = (LinkedTreeMap) paquete.getContenido();
        String user = (String) data.get("nombreUsuario");
        String pass = (String) data.get("contrasena");

        Log.registrar("INFO", "Solicitud de registro recibida para usuario: " + user);

        boolean exito = RepositorioUsuarios.registrar(user, pass);

        if (exito) {
            Log.registrar("EXITO", "Usuario registrado correctamente: " + user);
            enviarRespuesta(paquete, "REGISTRO_OK", "Registro exitoso");
        } else {
            Log.registrar("WARNING", "Fallo registro (Usuario ya existe): " + user);
            enviarRespuesta(paquete, "ERROR", "Usuario ya existe");
        }
    }

    private void procesarLogin(PaqueteDTO paquete) {
        LinkedTreeMap data = (LinkedTreeMap) paquete.getContenido();
        String user = (String) data.get("nombreUsuario");
        String pass = (String) data.get("contrasena");

        Log.registrar("INFO", "Solicitud de login recibida para usuario: " + user);

        if (usuariosConectados.size() >= MAX_USUARIOS && !usuariosConectados.contains(user)) {
            Log.registrar("WARNING", "Login rechazado para " + user + ": Servidor lleno (" + usuariosConectados.size() + "/" + MAX_USUARIOS + ")");
            enviarRespuesta(paquete, "ERROR", "El servidor está lleno (Máx 5 usuarios).");
            return;
        }
        // ---------------------------------------

        if (RepositorioUsuarios.validar(user, pass)) {
            // Suscribir al usuario a los eventos de chat
            Servicio s = new Servicio(paquete.getPuertoOrigen(), paquete.getHost());

            registrarServicio("MENSAJE", s);
            registrarServicio("LISTA_USUARIOS", s);
            registrarServicio("CHAT_GRUPAL", s);

            if (!usuariosConectados.contains(user)) {
                usuariosConectados.add(user);
                Log.registrar("EXITO", "Login correcto. Usuarios en línea: " + usuariosConectados.size());
            } else {
                Log.registrar("INFO", "Usuario " + user + " se ha reconectado.");
            }

            enviarRespuesta(paquete, "LOGIN_OK", user);

        } else {
            Log.registrar("WARNING", "Login fallido (Credenciales incorrectas): " + user);
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
        for (Servicio s : lista) {
            if (s.getHost().equals(servicio.getHost()) && s.getPuerto() == servicio.getPuerto()) {
                return;
            }
        }
        lista.add(servicio);
    }

    public void notificarServicios(PaqueteDTO paquete) {
        List<Servicio> lista = servicios.get(paquete.getTipoEvento());
        if (lista != null) {
            for (Servicio servicio : lista) {
                // No enviar al mismo origen
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
    
    // Método necesario para compilación si lo usas en PublicadorEventos
    public void enviarHost(PaqueteDTO p) {
    }
}
