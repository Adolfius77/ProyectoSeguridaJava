package Modelo;

import ModeloChatTCP.LogicaCliente;
import ObjetoPresentacion.UsuarioOP;
import Observador.INotificadorNuevoMensaje;
import Observador.IPublicadorNuevoMensaje;

public class Modelo implements IModeloEscritura, IPublicadorNuevoMensaje {

    private LogicaCliente logicaCliente;

    public Modelo() {
        this.logicaCliente = LogicaCliente.getInstance();
    }

    @Override
    public void iniciarSesion(String nombreUsuario, String contrasena) {
        logicaCliente.login(nombreUsuario, contrasena);
    }

    @Override
    public void registrarUsuario(String nombreUsuario, String contrasena) {
        logicaCliente.registrar(nombreUsuario, contrasena);
    }

    @Override
    public void enviarMensaje(String mensaje, UsuarioOP destinatario) {
        logicaCliente.enviarMensaje(mensaje, destinatario);
    }

    // Métodos de navegación UI (puedes implementarlos para cambiar ventanas)
    @Override public void mostrarChatFrame(UsuarioOP usuarioOP) {}
    @Override public void mostrarChatGrupal() {}
    @Override public void enviarMensajePrivado(UsuarioOP usuarioDestino, String contenido) {}
    @Override public void enviarMensajeGrupal(String contenido) {}

    // Delegar patrón Observer a la lógica
    @Override
    public void agregarObservador(INotificadorNuevoMensaje observador) {
        logicaCliente.agregarObservador(observador);
    }

    @Override
    public void notificar(UsuarioOP usuarioOP) {
        // La lógica es quien notifica realmente
    }
}