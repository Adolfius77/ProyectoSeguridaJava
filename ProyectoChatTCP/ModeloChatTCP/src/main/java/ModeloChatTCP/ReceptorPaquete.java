package ModeloChatTCP;

import DTO.UsuarioDTO;
import ModeloChatTCP.LogicaCliente;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.itson.componentereceptor.IReceptor;
import org.itson.paquetedto.PaqueteDTO;

/**
 * Receptor de paquetes que vienen desde el ServidorMain. Procesa respuestas de login y registro enviadas al cliente.
 */
public class ReceptorPaquete implements IReceptor {

    private LogicaCliente logicaCliente;
    private Gson gson;

    public ReceptorPaquete(LogicaCliente logicaCliente) {
        this.logicaCliente = logicaCliente;
        this.gson = new GsonBuilder().create();
    }

    @Override
    public void recibirCambio(PaqueteDTO paquete) {
        String tipo = paquete.getTipoEvento();
        System.out.println("[ReceptorPaquete] Evento recibido: " + tipo);

        switch (tipo.toUpperCase()) {
            case "LOGIN_OK":
                procesarLoginOk(paquete);
                break;
            case "LOGIN_ERROR":
                procesarLoginError(paquete);
                break;
            case "REGISTRO_OK":
                procesarRegistroOk(paquete);
                break;
            case "REGISTRO_ERROR":
                procesarRegistroError(paquete);
                break;
            default:
                System.out.println("[ReceptorPaquete] Evento no reconocido: " + tipo);
                break;
        }
    }

    private void procesarLoginOk(PaqueteDTO paquete) {
        UsuarioDTO usuario = mapContenido(paquete.getContenido(), UsuarioDTO.class);
        logicaCliente.setUsuarioEnSesion(usuario);
        //Settear el usuario al ModeloCHAT TCP el modelo ChatTCP o notificarselo
        //pasarle la lista de usuariosDTO tambien 
        System.out.println("[ReceptorPaquete] Login exitoso: " + usuario.getNombreUsuario());
    }

    private void procesarLoginError(PaqueteDTO paquete) {
        String mensaje = (String) paquete.getContenido();
        System.out.println("[ReceptorPaquete] Login fallido: " + mensaje);
    }

    private void procesarRegistroOk(PaqueteDTO paquete) {
        String mensaje = (String) paquete.getContenido();
        //lo envial al frame de iniciarSesion
        System.out.println("[ReceptorPaquete] Registro exitoso: " + mensaje);
    }

    private void procesarRegistroError(PaqueteDTO paquete) {
        String mensaje = (String) paquete.getContenido();
        //le notifica a presentacion que hubo un error en el registro
        System.out.println("[ReceptorPaquete] Registro fallido: " + mensaje);
    }

    private <T> T mapContenido(Object contenido, Class<T> clase) {
        String json = gson.toJson(contenido);
        return gson.fromJson(json, clase);
    }
}
