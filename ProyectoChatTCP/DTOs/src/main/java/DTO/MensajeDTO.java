/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DTO;

import ObjetoPresentacion.UsuarioOP;
import java.time.LocalDateTime;

/**
 *
 * @author Jack Murrieta
 */
public class MensajeDTO {
    
    private String nombreUsuario;
    private String contenidoMensaje;
    private LocalDateTime fechaHora = LocalDateTime.now();
    private UsuarioOP usuarioDestino;   // usuario a quien se envía
    private UsuarioDTO usuarioOrigen;   // usuario que envía

    public MensajeDTO(String nombreUsuario, String contenidoMensaje, UsuarioOP usuarioDestino, UsuarioDTO usuarioOrigen) {
        this.nombreUsuario = nombreUsuario;
        this.contenidoMensaje = contenidoMensaje;
        this.usuarioDestino = usuarioDestino;
        this.usuarioOrigen = usuarioOrigen;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContenidoMensaje() {
        return contenidoMensaje;
    }

    public void setContenidoMensaje(String contenidoMensaje) {
        this.contenidoMensaje = contenidoMensaje;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public UsuarioOP getUsuarioDestino() {
        return usuarioDestino;
    }

    public void setUsuarioDestino(UsuarioOP usuarioDestino) {
        this.usuarioDestino = usuarioDestino;
    }

    public UsuarioDTO getUsuarioOrigen() {
        return usuarioOrigen;
    }

    public void setUsuarioOrigen(UsuarioDTO usuarioOrigen) {
        this.usuarioOrigen = usuarioOrigen;
    }
    
    
    
}
