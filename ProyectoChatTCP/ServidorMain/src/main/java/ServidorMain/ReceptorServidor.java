/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ServidorMain;

import DTO.UsuarioDTO;
import GeneradorColor.GeneradorColor;
import java.util.Map;
import org.itson.componentereceptor.IReceptor;
import org.itson.paquetedto.PaqueteDTO;

/**
 *
 * @author Jack Murrieta
 */
public class ReceptorServidor implements IReceptor {

    private ServidorMain servidor;

    public ReceptorServidor(ServidorMain servidor) {
        this.servidor = servidor;
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

        String nombreUsuario = (String) data.get("nombreUsuario");
        String contrasena = (String) data.get("contrasena");
        String ip = (String) data.get("ip");
        int puerto = ((Double) data.get("puerto")).intValue();
        String color = (String) data.get("color");
        String publicKey = (String) data.get("publicKey");

        boolean ok = servidor.validarUsuario(nombreUsuario, contrasena);

        PaqueteDTO resp = new PaqueteDTO();
        resp.setTipoEvento(ok ? "LOGIN_OK" : "LOGIN_ERROR");

        if (ok) {
            // Recuperar datos completos del usuario
            UsuarioDTO usuario = servidor.obtenerUsuario(nombreUsuario);
            // Actualizamos IP, puerto, color y publicKey del login actual
            usuario.setIp(ip);
            usuario.setPuerto(puerto);
            usuario.setColor(color);
            usuario.setPublicKey(publicKey);
            // Guardar cambios
            servidor.getRepositorioUsuarios().actualizar(usuario);

            resp.setContenido(usuario);
        } else {
            resp.setContenido("Credenciales incorrectas");
        }

        // Dirección inversa (regresar al cliente)
        resp.setHost(paquete.getHost());
        resp.setPuertoDestino(paquete.getPuertoOrigen());
        resp.setPuertoOrigen(9999); // Puerto del servidor main

        servidor.enviarRespuesta(resp);
    }

    // ---------------- REGISTRO ----------------
    private void procesarRegistro(PaqueteDTO paquete) {

        Map<String, Object> data = (Map<String, Object>) paquete.getContenido();

        UsuarioDTO usuario = new UsuarioDTO(
                (String) data.get("nombreUsuario"),
                (String) data.get("contrasena"),
                (String) data.get("ip"),
                ((Double) data.get("puerto")).intValue(),
                (String) data.get("color"),
                (String) data.get("publicKey")
        );

        GeneradorColor gen = GeneradorColor.getInstancia();
        String color = gen.generarColor();
        usuario.setColor(color);

        boolean ok = servidor.registrarUsuario(usuario);

        PaqueteDTO resp = new PaqueteDTO();
        resp.setTipoEvento(ok ? "REGISTRO_OK" : "REGISTRO_ERROR");
        resp.setContenido(ok ? "Usuario registrado" : "El usuario ya existe");

        resp.setHost(paquete.getHost());
        resp.setPuertoDestino(paquete.getPuertoOrigen());
        resp.setPuertoOrigen(9999);

        servidor.enviarRespuesta(resp);
    }
}
