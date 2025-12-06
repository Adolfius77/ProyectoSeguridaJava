/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ServidorMain;

import DTO.UsuarioDTO;
import Datos.RepositorioUsuarios;
import org.itson.componenteemisor.IEmisor;
import org.itson.paquetedto.PaqueteDTO;

/**
 *
 * @author Jack Murrieta
 */
public class ServidorMain {

    private final RepositorioUsuarios repositorioUsuarios;
    private IEmisor emisor;

    // Constructor
    public ServidorMain(IEmisor emisor) {
        this.emisor = emisor;
        this.repositorioUsuarios = new RepositorioUsuarios();
    }

    // Getters
    public RepositorioUsuarios getRepositorioUsuarios() {
        return repositorioUsuarios;
    }

    public IEmisor getEmisor() {
        return emisor;
    }

    public void setEmisor(IEmisor emisor) {
        this.emisor = emisor;
    }

    // Enviar respuesta al EventBus / cliente
    public void enviarRespuesta(PaqueteDTO paquete) {
        System.out.println("[ServidorMain.enviarRespuesta] Enviando paquete:");
        System.out.println("  - Tipo: " + paquete.getTipoEvento());
        System.out.println("  - Host: " + paquete.getHost());
        System.out.println("  - Puerto destino: " + paquete.getPuertoDestino());
        System.out.println("  - Contenido: " + paquete.getContenido());

        if (emisor != null) {
            emisor.enviarCambio(paquete);
        } else {
            System.err.println("[ServidorMain.enviarRespuesta] ERROR: Emisor es null!");
        }
    }

    // Registrar usuario completo
    public boolean registrarUsuario(UsuarioDTO usuario) {
        return RepositorioUsuarios.registrar(usuario);
    }

    // Validar login
    public boolean validarUsuario(String nombreUsuario, String password) {
        return RepositorioUsuarios.validar(nombreUsuario, password);
    }

    // Obtener información completa de usuario
    public UsuarioDTO obtenerUsuario(String nombreUsuario) {
        return RepositorioUsuarios.obtener(nombreUsuario);
    }

}
