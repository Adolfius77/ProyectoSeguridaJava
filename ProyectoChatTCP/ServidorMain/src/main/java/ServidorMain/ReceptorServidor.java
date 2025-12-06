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

        PaqueteDTO resp = new PaqueteDTO();
        resp.setTipoEvento(ok ? "LOGIN_OK" : "LOGIN_ERROR");

        if (ok) {
            // Recuperar datos completos del usuario registrado
            UsuarioDTO usuario = servidor.obtenerUsuario(usuarioReq.getNombreUsuario());

            // --- Actualizar información del usuario con datos del paquete ---
            if (usuarioReq.getIp() != null && !usuarioReq.getIp().isEmpty()) {
                usuario.setIp(usuarioReq.getIp());
            } else {
                usuario.setIp(paquete.getHost());
            }

            if (usuarioReq.getPuerto() != 0) {
                usuario.setPuerto(usuarioReq.getPuerto());
            } else {
                usuario.setPuerto(paquete.getPuertoOrigen());
            }

            // Actualizar color y publicKey si vienen en el paquete
            if (usuarioReq.getColor() != null) {
                usuario.setColor(usuarioReq.getColor());
            }
            if (usuarioReq.getPublicKey() != null) {
                usuario.setPublicKey(usuarioReq.getPublicKey());

                // Guardar la llave pública del cliente en el caché para poder cifrar respuestas
                EnsambladorServer.guardarLlaveClienteBase64(
                    usuario.getIp(),
                    usuario.getPuerto(),
                    usuarioReq.getPublicKey()
                );
            }

            // Guardar cambios
            servidor.getRepositorioUsuarios().actualizar(usuario);

            // Contenido de la respuesta: usuario completo
            resp.setContenido(usuario);
        } else {
            resp.setContenido("Credenciales incorrectas");
        }

        // Setear host y puerto destino para que la respuesta llegue al cliente correcto
        // IMPORTANTE: Usar usuarioReq.getPuerto() que tiene el puerto correcto del DTO
        String hostRespuesta = usuarioReq.getIp() != null && !usuarioReq.getIp().isEmpty()
            ? usuarioReq.getIp()
            : paquete.getHost();
        int puertoRespuesta = usuarioReq.getPuerto() != 0
            ? usuarioReq.getPuerto()
            : paquete.getPuertoOrigen();

        resp.setHost(hostRespuesta);
        resp.setPuertoDestino(puertoRespuesta);

        System.out.println("[ReceptorServidor] Respuesta LOGIN:");
        System.out.println("  - Destino host: " + hostRespuesta);
        System.out.println("  - Destino puerto: " + puertoRespuesta);

        servidor.enviarRespuesta(resp);

        System.out.println("[ServidorMain] Login procesado: " + usuarioReq.getNombreUsuario()
                + ", IP: " + hostRespuesta + ", Puerto: " + puertoRespuesta);
    }

    // ---------------- REGISTRO ----------------
    private void procesarRegistro(PaqueteDTO paquete) {
        Map<String, Object> data = (Map<String, Object>) paquete.getContenido();
        UsuarioDTO usuario = mapToUsuarioDTO(data);

        // DEBUG: Ver qué viene en el paquete
        System.out.println("[ReceptorServidor] Paquete recibido:");
        System.out.println("  - host: " + paquete.getHost());
        System.out.println("  - puertoOrigen: " + paquete.getPuertoOrigen());
        System.out.println("  - puertoDestino: " + paquete.getPuertoDestino());
        System.out.println("  - usuario.ip: " + usuario.getIp());
        System.out.println("  - usuario.puerto: " + usuario.getPuerto());

        // --- Setear información del servidor y del cliente ---
        // Generar color único
        GeneradorColor gen = GeneradorColor.getInstancia();
        usuario.setColor(gen.generarColor());

        // Si quieres setear la IP del cliente y puerto del paquete
        // asumimos que el cliente envía host y puerto
        if (usuario.getIp() == null || usuario.getIp().isEmpty()) {
            usuario.setIp(paquete.getHost()); // host desde el paquete
        }
        if (usuario.getPuerto() == 0) {
            usuario.setPuerto(paquete.getPuertoOrigen()); // puerto origen del paquete
        }

        // Guardar la llave pública del cliente en el caché para cifrar respuestas futuras
        if (usuario.getPublicKey() != null && !usuario.getPublicKey().isEmpty()) {
            EnsambladorServer.guardarLlaveClienteBase64(
                usuario.getIp(),
                usuario.getPuerto(),
                usuario.getPublicKey()
            );
        }

        // --- Registrar usuario en el servidor ---
        boolean ok = servidor.registrarUsuario(usuario);

        // --- Preparar respuesta ---
        PaqueteDTO resp = new PaqueteDTO();
        resp.setTipoEvento(ok ? "REGISTRO_OK" : "REGISTRO_ERROR");
        resp.setContenido(ok ? "Usuario registrado" : "El usuario ya existe");

        // Setear host y puerto destino para que la respuesta llegue correctamente al cliente
        // IMPORTANTE: Usar usuario.getPuerto() que tiene el puerto correcto del DTO,
        // no paquete.getPuertoOrigen() que puede ser 0
        String hostRespuesta = usuario.getIp() != null && !usuario.getIp().isEmpty()
            ? usuario.getIp()
            : paquete.getHost();
        int puertoRespuesta = usuario.getPuerto() != 0
            ? usuario.getPuerto()
            : paquete.getPuertoOrigen();

        resp.setHost(hostRespuesta);
        resp.setPuertoDestino(puertoRespuesta);

        System.out.println("[ReceptorServidor] Preparando respuesta:");
        System.out.println("  - Destino host: " + hostRespuesta);
        System.out.println("  - Destino puerto: " + puertoRespuesta);

        servidor.enviarRespuesta(resp);

        System.out.println("[ServidorMain] Usuario procesado: " + usuario.getNombreUsuario()
                + ", IP: " + usuario.getIp() + ", Puerto: " + usuario.getPuerto());
    }

    /**
     * Mapper para convertir LinkedTreeMap o Map a UsuarioDTO usando Gson
     */
    private UsuarioDTO mapToUsuarioDTO(Map<String, Object> data) {
        String json = gson.toJson(data);
        return gson.fromJson(json, UsuarioDTO.class);
    }
}
