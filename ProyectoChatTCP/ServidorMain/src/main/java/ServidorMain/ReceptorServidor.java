package ServidorMain;

import DTO.UsuarioDTO;
import GeneradorColor.GeneradorColor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Map;
import org.itson.componentereceptor.IReceptor;
import org.itson.paquetedto.PaqueteDTO;

/**
 * Receptor del servidor que procesa solicitudes de login y registro.
 * Convierte LinkedTreeMap a UsuarioDTO usando Gson para evitar problemas de casting.
 * 
 * @author Jack Murrieta
 */
public class ReceptorServidor implements IReceptor {

    private ServidorMain servidor;
    private Gson gson;

    public ReceptorServidor(ServidorMain servidor) {
        this.servidor = servidor;
        this.gson = new GsonBuilder().create();
    }

    @Override
    public void recibirCambio(PaqueteDTO paquete) {
        String tipo = paquete.getTipoEvento();
        System.out.println("[ServidorMain] Evento recibido: " + tipo);

        switch (tipo.toUpperCase()) {
            case "SOLICITAR_LOGIN":
                procesarLogin(paquete);
                break;

            case "REGISTRAR_USUARIO":
                procesarRegistro(paquete);
                break;

            default:
                System.out.println("[ServidorMain] Evento no reconocido: " + tipo);
                break;
        }
    }

    // ---------------- LOGIN ----------------
    private void procesarLogin(PaqueteDTO paquete) {
        Map<String, Object> data = (Map<String, Object>) paquete.getContenido();
        UsuarioDTO usuarioReq = mapToUsuarioDTO(data);

        boolean ok = servidor.validarUsuario(usuarioReq.getNombreUsuario(), usuarioReq.getContrasena());

        PaqueteDTO resp = new PaqueteDTO();
        resp.setTipoEvento(ok ? "LOGIN_OK" : "LOGIN_ERROR");

        if (ok) {
            // Recuperar datos completos del usuario
            UsuarioDTO usuario = servidor.obtenerUsuario(usuarioReq.getNombreUsuario());
            // Actualizamos IP, puerto, color y publicKey del login actual
            usuario.setIp(usuarioReq.getIp());
            usuario.setPuerto(usuarioReq.getPuerto());
            usuario.setColor(usuarioReq.getColor());
            usuario.setPublicKey(usuarioReq.getPublicKey());
            // Guardar cambios
            servidor.getRepositorioUsuarios().actualizar(usuario);

            resp.setContenido(usuario);
        } else {
            resp.setContenido("Credenciales incorrectas");
        }

        resp.setHost(paquete.getHost());
        resp.setPuertoDestino(paquete.getPuertoOrigen());
        servidor.enviarRespuesta(resp);
    }

    // ---------------- REGISTRO ----------------
    private void procesarRegistro(PaqueteDTO paquete) {
        Map<String, Object> data = (Map<String, Object>) paquete.getContenido();
        UsuarioDTO usuario = mapToUsuarioDTO(data);

        // Generar color único
        GeneradorColor gen = GeneradorColor.getInstancia();
        usuario.setColor(gen.generarColor());

        boolean ok = servidor.registrarUsuario(usuario);

        PaqueteDTO resp = new PaqueteDTO();
        resp.setTipoEvento(ok ? "REGISTRO_OK" : "REGISTRO_ERROR");
        resp.setContenido(ok ? "Usuario registrado" : "El usuario ya existe");

        resp.setHost(paquete.getHost());
        resp.setPuertoDestino(paquete.getPuertoOrigen());
        servidor.enviarRespuesta(resp);
    }

    /**
     * Mapper para convertir LinkedTreeMap o Map a UsuarioDTO usando Gson
     */
    private UsuarioDTO mapToUsuarioDTO(Map<String, Object> data) {
        String json = gson.toJson(data);
        return gson.fromJson(json, UsuarioDTO.class);
    }
}
