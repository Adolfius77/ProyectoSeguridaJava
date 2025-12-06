/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package ModeloChatTCP;

import DTO.UsuarioDTO;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.itson.componenteemisor.IEmisor;
import org.itson.paquetedto.PaqueteDTO;

/**
 * Lógica del cliente que se comunica directamente con el ServidorMain. Login y registro se envían usando un IEmisor que ya fue configurado por el ensamblador. Host y puerto del servidor se leen desde un archivo .properties para construir los paquetes.
 *
 * @author Jack
 */
public class LogicaCliente {

    private UsuarioDTO usuarioEnSesion;
    private IEmisor emisor;
    private Properties configServidor;

    /**
     * Constructor que recibe un IEmisor ya inicializado y el archivo .properties
     *
     * @param emisor Emisor ya configurado por el ensamblador
     * @param archivoPropertiesServidor Archivo .properties con host y puerto del servidor
     */
    public LogicaCliente(IEmisor emisor, String archivoPropertiesServidor) {
        this.emisor = emisor;
        try {
            // Cargar configuración del servidor
            configServidor = new Properties();
            InputStream input = getClass().getClassLoader().getResourceAsStream(archivoPropertiesServidor);
            if (input == null) {
                throw new IOException("No se encontró el archivo de configuración: " + archivoPropertiesServidor);
            }
            configServidor.load(input);
        } catch (IOException e) {
            System.err.println("[LogicaCliente] Error cargando configuración del servidor: " + e.getMessage());
        }
    }

    public UsuarioDTO getUsuarioEnSesion() {
        return usuarioEnSesion;
    }

    public void setUsuarioEnSesion(UsuarioDTO usuarioEnSesion) {
        this.usuarioEnSesion = usuarioEnSesion;
    }

    public IEmisor getEmisor() {
        return emisor;
    }

    public void setEmisor(IEmisor emisor) {
        this.emisor = emisor;
    }

    /**
     * Envía una solicitud de registro al ServidorMain usando el emisor
     *
     * @param usuario nombre de usuario
     * @param password contraseña
     * @param ip IP del cliente
     * @param puerto Puerto de escucha del cliente
     * @param publicKey llave pública del cliente (opcional)
     */
    public void registrar(String usuario, String password, String ip, int puerto, String publicKey) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombreUsuario(usuario);
        dto.setContrasena(password);
        dto.setIp(ip);
        dto.setPuerto(puerto);
        dto.setPublicKey(publicKey);

        PaqueteDTO<UsuarioDTO> paquete = new PaqueteDTO<>();
        paquete.setTipoEvento("REGISTRAR_USUARIO");
        paquete.setContenido(dto);
        paquete.setHost(configServidor.getProperty("host"));
        paquete.setPuertoDestino(Integer.parseInt(configServidor.getProperty("puerto.entrada")));

        emisor.enviarCambio(paquete);
        System.out.println("[LogicaCliente] Registro enviado al ServidorMain");
    }

    /**
     * Envía una solicitud de login al ServidorMain usando el emisor
     *
     * @param usuario nombre de usuario
     * @param password contraseña
     * @param ip IP del cliente
     * @param puerto Puerto de escucha del cliente
     * @param publicKey llave pública del cliente (opcional)
     */
    public void login(String usuario, String password, String ip, int puerto, String publicKey) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombreUsuario(usuario);
        dto.setContrasena(password);
        dto.setIp(ip);
        dto.setPuerto(puerto);
        dto.setPublicKey(publicKey);

        PaqueteDTO<UsuarioDTO> paquete = new PaqueteDTO<>();
        paquete.setTipoEvento("SOLICITAR_LOGIN");
        paquete.setContenido(dto);
        paquete.setHost(configServidor.getProperty("host"));
        paquete.setPuertoDestino(Integer.parseInt(configServidor.getProperty("puerto.entrada")));

        emisor.enviarCambio(paquete);
        System.out.println("[LogicaCliente] Login enviado al ServidorMain");
    }
}
