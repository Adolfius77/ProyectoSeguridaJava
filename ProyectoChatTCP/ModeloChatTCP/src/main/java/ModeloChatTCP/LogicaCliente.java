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
    private Properties configCliente; // Configuración del cliente para puerto de entrada

    public LogicaCliente(IEmisor emisor, IReceptor receptor, String archivoPropertiesServidor) {
        this.emisor = emisor;
        this.receptor = receptor;

        // Cargar configuración del servidor
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(archivoPropertiesServidor)) {
            if (input == null) {
                throw new IOException("No se encontró el archivo de configuración: " + archivoPropertiesServidor);
            }
            configServidor = new Properties();
            configServidor.load(input);
        } catch (IOException e) {
            System.err.println("[LogicaCliente] Error cargando configuración del servidor: " + e.getMessage());
        }

        // Cargar configuración del cliente para obtener puerto de entrada
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config_Usuario.properties")) {
            if (input == null) {
                throw new IOException("No se encontró config_Usuario.properties");
            }
            configCliente = new Properties();
            configCliente.load(input);
        } catch (IOException e) {
            System.err.println("[LogicaCliente] Error cargando config_Usuario.properties: " + e.getMessage());
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

        // Obtener IP y puerto del cliente desde la configuración
        String ipCliente = configCliente != null ? configCliente.getProperty("host") : "localhost";
        int puertoEntradaCliente = configCliente != null
            ? Integer.parseInt(configCliente.getProperty("puerto.entrada"))
            : puerto;

        dto.setIp(ipCliente);
        dto.setPuerto(puertoEntradaCliente);
        dto.setPublicKey(publicKey);

        PaqueteDTO<UsuarioDTO> paquete = new PaqueteDTO<>();
        paquete.setTipoEvento("REGISTRAR_USUARIO");
        paquete.setContenido(dto);

        // Destino: servidor
        paquete.setHost(configServidor.getProperty("host"));
        paquete.setPuertoDestino(Integer.parseInt(configServidor.getProperty("puerto.entrada")));

        // IMPORTANTE: Establecer el puerto de origen para que el servidor pueda responder
        paquete.setPuertoOrigen(puertoEntradaCliente);

        emisor.enviarCambio(paquete);
        System.out.println("[LogicaCliente] Registro enviado al ServidorMain");
        System.out.println("[LogicaCliente] Puerto de escucha del cliente: " + puertoEntradaCliente);
    }

    public void login(String usuario, String password, String ip, int puerto, String publicKey) {
        DTO.UsuarioDTO dto = new UsuarioDTO();
        dto.setNombreUsuario(usuario);
        dto.setContrasena(password);

        // Obtener IP y puerto del cliente desde la configuración
        String ipCliente = configCliente != null ? configCliente.getProperty("host") : "localhost";
        int puertoEntradaCliente = configCliente != null
            ? Integer.parseInt(configCliente.getProperty("puerto.entrada"))
            : puerto;

        dto.setIp(ipCliente);
        dto.setPuerto(puertoEntradaCliente);
        dto.setPublicKey(publicKey);

        PaqueteDTO<UsuarioDTO> paquete = new PaqueteDTO<>();
        paquete.setTipoEvento("SOLICITAR_LOGIN");
        paquete.setContenido(dto);

        // Destino: servidor
        paquete.setHost(configServidor.getProperty("host"));
        paquete.setPuertoDestino(Integer.parseInt(configServidor.getProperty("puerto.entrada")));

        // IMPORTANTE: Establecer el puerto de origen para que el servidor pueda responder
        paquete.setPuertoOrigen(puertoEntradaCliente);

        emisor.enviarCambio(paquete);
        System.out.println("[LogicaCliente] Login enviado al ServidorMain");
        System.out.println("[LogicaCliente] Puerto de escucha del cliente: " + puertoEntradaCliente);
    }
}
