package Controlador;

import Modelo.IModeloEscritura;
import Modelo.Modelo; // Asegúrate de importar la implementación
import ObjetoPresentacion.UsuarioOP;
import Observador.INotificadorNuevoMensaje;

public class Controlador implements IControlador {

    private IModeloEscritura modelo;

    public Controlador(IModeloEscritura modelo) {
        this.modelo = modelo;
    }

    // Constructor por defecto si lo usas así en las vistas
    public Controlador() {
        this.modelo = new Modelo();
    }

    @Override
    public void iniciarSesion(String nombreUsuario, String contrasena) {
        modelo.iniciarSesion(nombreUsuario, contrasena);
    }

    @Override
    public void registrarUsuario(String nombreUsuario, String contrasena) {
        modelo.registrarUsuario(nombreUsuario, contrasena);
    }

    @Override
    public void enviarMensajePrivado(UsuarioOP usuarioDestino, String contenido) {
        modelo.enviarMensaje(contenido, usuarioDestino);
    }

    @Override
    public void enviarMensajeGrupal(String contenido) {
        // Implementar envío a todos
    }

    @Override
    public void abrirChatUsuario(UsuarioOP usuarioOP) {
        // Lógica de navegación
    }

    @Override
    public void pedirListaUsuarios() {
        modelo.solicitarUsuarios();
    }
    @Override
    public void agregarObservador(INotificadorNuevoMensaje observador) {
        modelo.agregarObservador(observador);
    }
    
}
