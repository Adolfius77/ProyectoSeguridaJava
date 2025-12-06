/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import Modelo.IModeloEscritura;
import ObjetoPresentacion.UsuarioOP;

/**
 *
 * @author Jack Murrieta
 */
public class Controlador implements IControlador {
    
    private IModeloEscritura modelo;
    
    @Override
    public void iniciarSesion(String nombreUsuario, String contrasena) {
        modelo.iniciarSesion(nombreUsuario, contrasena);
    }
    
    @Override
    public void registrarUsuario(String nombreUsuario, String contrasena) {
        modelo.registrarUsuario(nombreUsuario, contrasena);
    }
    
    @Override
    public void abrirChatUsuario(UsuarioOP usuarioOP) {
        modelo.mostrarChatFrame(usuarioOP);
    }
    
    @Override
    public void enviarMensajePrivado(UsuarioOP usuarioDestino, String contenido) {
        modelo.enviarMensajePrivado(usuarioDestino, contenido);
    }
    
    @Override
    public void enviarMensajeGrupal(String contenido) {
        modelo.enviarMensajeGrupal(contenido);
        
    }
    
}
