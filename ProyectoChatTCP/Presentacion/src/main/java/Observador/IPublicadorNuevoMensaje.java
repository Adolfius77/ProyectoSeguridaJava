/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Observador;

import ObjetoPresentacion.UsuarioOP;

/**
 *
 * @author Usuario
 */
public interface IPublicadorNuevoMensaje {

    void agregarObservador(INotificadorNuevoMensaje observador);

    void notificar(UsuarioOP usuarioOP);

}
