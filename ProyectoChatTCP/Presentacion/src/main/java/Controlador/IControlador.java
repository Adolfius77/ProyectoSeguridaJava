/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Controlador;

import ObjetoPresentacion.UsuarioOP;
import Observador.INotificadorNuevoMensaje;

/**
 *
 * @author Jck Murrieta
 */
public interface IControlador {

    void iniciarSesion(String nombreUsuario, String contrasena);

    void registrarUsuario(String nombreUsuario, String contrasena);

    // Menú Users → abrir chat
    void abrirChatUsuario(UsuarioOP usuarioOP);

    // Mensajes privados
    void enviarMensajePrivado(UsuarioOP usuarioDestino, String contenido);

    // Mensaje grupal
    void enviarMensajeGrupal(String contenido);

    void pedirListaUsuarios();

    void agregarObservador(INotificadorNuevoMensaje observador);
}
