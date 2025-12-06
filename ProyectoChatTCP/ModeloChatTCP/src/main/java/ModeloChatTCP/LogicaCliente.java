package ModeloChatTCP;

import DTO.UsuarioDTO;
import DTO.MensajeDTO;
import java.util.Base64;
import org.itson.componenteemisor.IEmisor;
import org.itson.componentereceptor.IReceptor;
import org.itson.ensamblador.EnsambladorRed; // Asegúrate de tener la dependencia en pom.xml
import org.itson.paquetedto.PaqueteDTO;
import ObjetoPresentacion.UsuarioOP;
import Observador.IPublicadorNuevoMensaje;
import Observador.INotificadorNuevoMensaje;
import ensamblador.EnsambladorRed;
import java.util.ArrayList;
import java.util.List;

public class LogicaCliente implements IReceptor, IPublicadorNuevoMensaje {
    
    private static LogicaCliente instancia;
    private IEmisor emisor;
    private EnsambladorRed ensamblador;
    private List<INotificadorNuevoMensaje> observadores = new ArrayList<>();
    private UsuarioOP usuarioActual; 

    private LogicaCliente() {
        this.ensamblador = EnsambladorRed.getInstancia();
    }
    
    public static synchronized LogicaCliente getInstance() {
        if (instancia == null) instancia = new LogicaCliente();
        return instancia;
    }
    
    public void conectar() {
        if (emisor == null) {
            this.emisor = ensamblador.ensamblar(this);
            System.out.println("[LogicaCliente] Conectado a la red.");
        }
    }

    // --- MÉTODOS DE NEGOCIO ---

    public void registrar(String usuario, String password) {
        conectar();
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombreUsuario(usuario);
        dto.setContrasena(password);
        // Adjuntar llave pública
        byte[] key = ensamblador.getPublicKey();
        if(key != null) dto.setPublicKey(Base64.getEncoder().encodeToString(key));
        
        enviarPaquete("REGISTRO", dto);
    }

    public void login(String usuario, String password) {
        conectar();
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombreUsuario(usuario);
        dto.setContrasena(password);
        
        enviarPaquete("LOGIN", dto);
    }
    
    public void enviarMensaje(String texto, UsuarioOP destino) {
        // En este ejemplo simple enviamos un Map o el DTO
        // Usamos un objeto simple compatible con JSON
        MensajeDTO msj = new MensajeDTO(
            usuarioActual != null ? usuarioActual.getNombre() : "Anon",
            texto,
            destino,
            null
        );
        
        enviarPaquete("MENSAJE", msj);
    }

    private void enviarPaquete(String tipo, Object contenido) {
        PaqueteDTO paquete = new PaqueteDTO();
        paquete.setTipoEvento(tipo);
        paquete.setContenido(contenido);
        paquete.setHost("localhost");
        paquete.setPuertoOrigen(ensamblador.getPuertoEscucha());
        paquete.setPuertoDestino(5556); // Bus
        
        emisor.enviarCambio(paquete);
    }

    // --- RECEPCIÓN DE MENSAJES ---

    @Override
    public void recibirCambio(PaqueteDTO paquete) {
        String tipo = paquete.getTipoEvento();
        System.out.println("[Cliente] Recibido: " + tipo);
        
        if (tipo.equals("LOGIN_OK")) {
            String nombre = paquete.getContenido().toString();
            this.usuarioActual = new UsuarioOP(0, nombre, "", "", 0);
        }
        
        // Notificar a la UI (Observadores)
        // Convertimos el contenido a UsuarioOP si es un mensaje, o un objeto especial para alertas
        // Para simplificar, pasamos un UsuarioOP "dummy" con el contenido en el último mensaje
        // Idealmente usarías una interfaz más rica para notificar eventos distintos
        
        UsuarioOP notificacion = new UsuarioOP(0, tipo, paquete.getContenido().toString(), "", 0);
        notificar(notificacion);
    }

    @Override
    public void agregarObservador(INotificadorNuevoMensaje observador) {
        observadores.add(observador);
    }

    @Override
    public void notificar(UsuarioOP usuarioOP) {
        for(INotificadorNuevoMensaje obs : observadores) {
            obs.actualizar(usuarioOP);
        }
    }
}