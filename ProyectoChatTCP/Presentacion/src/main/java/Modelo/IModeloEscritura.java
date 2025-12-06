/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Modelo;

import ObjetoPresentacion.UsuarioOP;
import Observador.INotificadorNuevoMensaje;

/**
 *
 * @author Jck Murrieta
 */
public interface IModeloEscritura {

    void iniciarSesion(String nombreUsuario, String contrasena);

    void registrarUsuario(String nombreUsuario, String contrasena);

    void enviarMensaje(String mensaje, UsuarioOP destinatario);

    void mostrarChatFrame(UsuarioOP usuarioOP);
    
    void mostrarChatGrupal();

    void enviarMensajePrivado(UsuarioOP usuarioDestino, String contenido);

    void enviarMensajeGrupal(String contenido);
    
    void solicitarUsuarios();
    
    void agregarObservador(INotificadorNuevoMensaje observador);
    

}
