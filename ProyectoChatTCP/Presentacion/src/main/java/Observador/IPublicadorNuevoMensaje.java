package Observador;

import ObjetoPresentacion.UsuarioOP;

public interface IPublicadorNuevoMensaje {
    void agregarObservador(INotificadorNuevoMensaje observador);
    void notificar(UsuarioOP usuarioOP);
}