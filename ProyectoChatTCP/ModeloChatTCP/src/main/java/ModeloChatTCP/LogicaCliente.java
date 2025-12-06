package ModeloChatTCP;

import DTO.UsuarioDTO;
import org.itson.componenteemisor.IEmisor;
import org.itson.componentereceptor.IReceptor;
import org.itson.paquetedto.PaqueteDTO;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Lógica del cliente que se comunica directamente con el ServidorMain.
 * Login y registro se envían usando un IEmisor que ya fue configurado por el ensamblador.
 */
public class LogicaCliente {

    private UsuarioDTO usuarioEnSesion;
    private IEmisor emisor;
    private IReceptor receptor; // Receptor opcional
    private Properties configServidor;

    public LogicaCliente(IEmisor emisor, IReceptor receptor, String archivoPropertiesServidor) {
        this.emisor = emisor;
        this.receptor = receptor;
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(archivoPropertiesServidor)) {
            if (input == null) {
                throw new IOException("No se encontró el archivo de configuración: " + archivoPropertiesServidor);
            }
            configServidor = new Properties();
            configServidor.load(input);
        } catch (IOException e) {
            System.err.println("[LogicaCliente] Error cargando configuración del servidor: " + e.getMessage());
        }
    }

    // ------------------- Getters y Setters -------------------
    public UsuarioDTO getUsuarioEnSesion() { return usuarioEnSesion; }
    public void setUsuarioEnSesion(UsuarioDTO usuarioEnSesion) { this.usuarioEnSesion = usuarioEnSesion; }
    public IEmisor getEmisor() { return emisor; }
    public void setEmisor(IEmisor emisor) { this.emisor = emisor; }
    public IReceptor getReceptor() { return receptor; }
    public void setReceptor(IReceptor receptor) { this.receptor = receptor; }

    // ------------------- Métodos de negocio -------------------
    public void registrar(String usuario, String password, String ip, int puerto, String publicKey) {
        DTO.UsuarioDTO dto = new UsuarioDTO();
        dto.setNombreUsuario(usuario);
        dto.setContrasena(password);
        dto.setIp(ip);
        dto.setPuerto(puerto);
        dto.setPublicKey(publicKey);

        PaqueteDTO<UsuarioDTO> paquete = new PaqueteDTO<>();
        paquete.setTipoEvento("REGISTRAR_USUARIO");
        paquete.setContenido(dto);
        //Manda al servidor con el properties
        paquete.setHost(configServidor.getProperty("host"));
        paquete.setPuertoDestino(Integer.parseInt(configServidor.getProperty("puerto.entrada")));

        emisor.enviarCambio(paquete);
        System.out.println("[LogicaCliente] Registro enviado al ServidorMain");
    }

    public void login(String usuario, String password, String ip, int puerto, String publicKey) {
        DTO.UsuarioDTO dto = new UsuarioDTO();
        dto.setNombreUsuario(usuario);
        dto.setContrasena(password);
        dto.setIp(ip);
        dto.setPuerto(puerto);
        dto.setPublicKey(publicKey);

        PaqueteDTO<UsuarioDTO> paquete = new PaqueteDTO<>();
        paquete.setTipoEvento("SOLICITAR_LOGIN");
        paquete.setContenido(dto);
        //Manda al servidor con el properties
        paquete.setHost(configServidor.getProperty("host"));
        paquete.setPuertoDestino(Integer.parseInt(configServidor.getProperty("puerto.entrada")));

        emisor.enviarCambio(paquete);
        System.out.println("[LogicaCliente] Login enviado al ServidorMain");
    }
}
