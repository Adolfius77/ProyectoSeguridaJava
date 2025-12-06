package ServidorMain;

import DTO.UsuarioDTO;
import EnsambladorServer.EnsambladorServer;
import GeneradorColor.GeneradorColor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Map;
import org.itson.componentereceptor.IReceptor;
import org.itson.paquetedto.PaqueteDTO;

/**
 * Receptor del servidor que procesa solicitudes de login y registro. Convierte LinkedTreeMap a UsuarioDTO usando Gson para evitar problemas de casting.
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

        // Crear respuesta
        PaqueteDTO resp = new PaqueteDTO();
        resp.setTipoEvento(ok ? "LOGIN_OK" : "LOGIN_ERROR");

        if (ok) {
            // Recuperar usuario registrado
            UsuarioDTO usuario = servidor.obtenerUsuario(usuarioReq.getNombreUsuario());

            // Actualizar IP y puerto según datos recibidos
            usuario.setIp(usuarioReq.getIp() != null && !usuarioReq.getIp().isEmpty() ? usuarioReq.getIp() : paquete.getHost());
            usuario.setPuerto(usuarioReq.getPuerto() != 0 ? usuarioReq.getPuerto() : paquete.getPuertoOrigen());

            if (usuarioReq.getColor() != null) {
                usuario.setColor(usuarioReq.getColor());
            }
            if (usuarioReq.getPublicKey() != null) {
                usuario.setPublicKey(usuarioReq.getPublicKey());
                EnsambladorServer.guardarLlaveClienteBase64(usuario.getIp(), usuario.getPuerto(), usuarioReq.getPublicKey());
            }

            // Guardar cambios en repositorio
            servidor.getRepositorioUsuarios().actualizar(usuario);

            // Crear copia sin contraseña para enviar al cliente
            UsuarioDTO usuarioParaCliente = new UsuarioDTO(
                    usuario.getNombreUsuario(),
                    "", // contraseña vacía
                    usuario.getIp(),
                    usuario.getPuerto(),
                    usuario.getColor(),
                    usuario.getPublicKey()
            );

            resp.setContenido(usuarioParaCliente);
        } else {
            resp.setContenido("Credenciales incorrectas");
        }

        // Setear host y puerto destino
        resp.setHost(usuarioReq.getIp() != null && !usuarioReq.getIp().isEmpty() ? usuarioReq.getIp() : paquete.getHost());
        resp.setPuertoDestino(usuarioReq.getPuerto() != 0 ? usuarioReq.getPuerto() : paquete.getPuertoOrigen());

        // Enviar respuesta
        servidor.enviarRespuesta(resp);

        System.out.println("[ReceptorServidor] LOGIN procesado: " + usuarioReq.getNombreUsuario()
                + ", host: " + resp.getHost() + ", puerto: " + resp.getPuertoDestino());
    }

    // ---------------- REGISTRO ----------------
    private void procesarRegistro(PaqueteDTO paquete) {
        Map<String, Object> data = (Map<String, Object>) paquete.getContenido();
        UsuarioDTO usuario = mapToUsuarioDTO(data);

        // Generar color único
        GeneradorColor gen = GeneradorColor.getInstancia();
        usuario.setColor(gen.generarColor());

        // Setear IP y puerto si no vienen
        if (usuario.getIp() == null || usuario.getIp().isEmpty()) {
            usuario.setIp(paquete.getHost());
        }
        if (usuario.getPuerto() == 0) {
            usuario.setPuerto(paquete.getPuertoOrigen());
        }

        // Guardar llave pública del cliente
        if (usuario.getPublicKey() != null && !usuario.getPublicKey().isEmpty()) {
            EnsambladorServer.guardarLlaveClienteBase64(usuario.getIp(), usuario.getPuerto(), usuario.getPublicKey());
        }

        // Registrar usuario
        boolean ok = servidor.registrarUsuario(usuario);

        // Preparar respuesta
        PaqueteDTO resp = new PaqueteDTO();
        resp.setTipoEvento(ok ? "REGISTRO_OK" : "REGISTRO_ERROR");
        resp.setContenido(ok ? "Usuario registrado" : "El usuario ya existe");

        // Setear host y puerto destino
        resp.setHost(usuario.getIp());
        resp.setPuertoDestino(usuario.getPuerto());

        // Enviar respuesta
        servidor.enviarRespuesta(resp);

        System.out.println("[ReceptorServidor] Registro procesado: " + usuario.getNombreUsuario()
                + ", host: " + usuario.getIp() + ", puerto: " + usuario.getPuerto());
    }

    /**
     * Mapper para convertir LinkedTreeMap o Map a UsuarioDTO usando Gson
     */
    private UsuarioDTO mapToUsuarioDTO(Map<String, Object> data) {
        String json = gson.toJson(data);
        return gson.fromJson(json, UsuarioDTO.class);
    }
}
