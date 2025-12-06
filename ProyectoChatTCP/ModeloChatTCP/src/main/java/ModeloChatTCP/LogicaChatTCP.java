package ModeloChatTCP;
import ensamblador.EnsambladorRed;
import DTO.UsuarioDTO;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import org.itson.componenteemisor.IEmisor;
import org.itson.componentereceptor.IReceptor;
import org.itson.paquetedto.PaqueteDTO;
import org.itson.ensamblador.EnsambladorRed; // Asegúrate de importar tu ensamblador
import ObjetoPresentacion.UsuarioOP;
import Observador.IPublicadorNuevoMensaje;
import Observador.INotificadorNuevoMensaje;
import ensamblador.EnsambladorRed;

public class LogicaCliente implements IReceptor, IPublicadorNuevoMensaje {
    
    private static LogicaCliente instancia;
    private IEmisor emisor;
    private EnsambladorRed ensamblador;
    private List<INotificadorNuevoMensaje> observadores = new ArrayList<>();

    private LogicaCliente() {
        this.ensamblador = EnsambladorRed.getInstancia();
    }
    
    public static synchronized LogicaCliente getInstance() {
        if (instancia == null) instancia = new LogicaCliente();
        return instancia;
    }
    S
    public void conectar() {
        if (emisor == null) {
            // Pasamos 'this' para que LogicaCliente reciba las respuestas del servidor
            this.emisor = ensamblador.ensamblar(this);
        }
    }

    public void registrar(String usuario, String password) {
        conectar();
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombreUsuario(usuario);
        dto.setContrasena(password);
        
        // Enviamos llave pública para cifrado
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

    private void enviarPaquete(String tipo, Object contenido) {
        PaqueteDTO paquete = new PaqueteDTO();
        paquete.setTipoEvento(tipo);
        paquete.setContenido(contenido);
        paquete.setHost("localhost");
        paquete.setPuertoOrigen(ensamblador.getPuertoEscucha());
        paquete.setPuertoDestino(5556); // Puerto del Bus
        
        emisor.enviarCambio(paquete);
    }

    @Override
    public void recibirCambio(PaqueteDTO paquete) {
        String tipo = paquete.getTipoEvento();
        String contenido = paquete.getContenido().toString();
        System.out.println("[Cliente] Recibido: " + tipo + " - " + contenido);
        
        // Usamos UsuarioOP como transporte de la notificación a la GUI
        // Nombre = Tipo de Evento (LOGIN_OK, ERROR, etc)
        // UltimoMensaje = Contenido
        UsuarioOP notificacion = new UsuarioOP(0, tipo, contenido, "", 0);
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