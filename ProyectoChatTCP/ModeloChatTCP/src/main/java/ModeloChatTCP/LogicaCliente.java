package ModeloChatTCP;

import DTO.UsuarioDTO;
import DTO.MensajeDTO;

import ObjetoPresentacion.UsuarioOP;
import Observador.INotificadorNuevoMensaje;
import Observador.IPublicadorNuevoMensaje;
import com.google.gson.Gson;
import ensamblador.EnsambladorRed;
import org.itson.componenteemisor.IEmisor;
import org.itson.componentereceptor.IReceptor;
import org.itson.paquetedto.PaqueteDTO;
import com.google.gson.GsonBuilder;
import Util.LocalDateTimeAdapter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class LogicaCliente implements IReceptor, IPublicadorNuevoMensaje {
    
    private static LogicaCliente instancia;
    private IEmisor emisor;
    private EnsambladorRed ensamblador;
    private List<INotificadorNuevoMensaje> observadores = new ArrayList<>();
    private UsuarioOP usuarioActual; 

    private LogicaCliente() {
        // Obtenemos la instancia del Ensamblador que vive en el módulo Red
        this.ensamblador = EnsambladorRed.getInstancia();
    }
    
    public static synchronized LogicaCliente getInstance() {
        if (instancia == null) {
            instancia = new LogicaCliente();
        }
        return instancia;
    }
    
    /**
     * Inicia la conexión de red ensamblando los componentes.
     */
    public void conectar() {
        if (emisor == null) {
            // Pasamos 'this' para que esta misma clase reciba los mensajes del servidor
            this.emisor = ensamblador.ensamblar(this);
            System.out.println("[LogicaCliente] Conexión establecida.");
        }
    }

    // --- MÉTODOS DE NEGOCIO ---

    public void registrar(String usuario, String password) {
        conectar(); // Asegurar conexión
        
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombreUsuario(usuario);
        dto.setContrasena(password); // Se enviará para que el server la hashee
        
        // Adjuntamos nuestra llave pública para que el servidor pueda cifrarnos respuestas futuras
        byte[] key = ensamblador.getPublicKey();
        if (key != null) {
            dto.setPublicKey(Base64.getEncoder().encodeToString(key));
        }
        
        enviarPaquete("REGISTRO", dto);
    }

    public void login(String usuario, String password) {
        conectar(); // Asegurar conexión
        
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombreUsuario(usuario);
        dto.setContrasena(password);
        
        enviarPaquete("LOGIN", dto);
    }
    public void solicitarListaUsuarios(){
        enviarPaquete("SOLICITAR_USUARIOS","");
    }
    public void enviarMensaje(String texto, UsuarioOP destino) {
        // Creamos el DTO del mensaje
        MensajeDTO msj = new MensajeDTO(
            usuarioActual != null ? usuarioActual.getNombre() : "Anonimo",
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
        paquete.setPuertoDestino(5555); 
        
        if (emisor != null) {
            emisor.enviarCambio(paquete);
        } else {
            System.err.println("[LogicaCliente] Error: Emisor no inicializado.");
        }
    }

   
@Override
    public void recibirCambio(PaqueteDTO paquete) {
        String tipo = paquete.getTipoEvento();
        
        
        if(tipo.equals("MENSAJE")){
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
            String json = gson.toJson(paquete.getContenido());
            MensajeDTO msjDTO = gson.fromJson(json, MensajeDTO.class);
            
            String infoMensaje = msjDTO.getNombreUsuario() + ":" + msjDTO.getContenidoMensaje();
            UsuarioOP notificacion = new UsuarioOP(0, "MENSAJE", infoMensaje, "", 0);
            notificar(notificacion);
            return;
        }
        
        String contenido = paquete.getContenido().toString();
        
        System.out.println("[LogicaCliente] Paquete recibido: " + tipo);
        
        if (tipo.equals("LOGIN_OK")) {
            this.usuarioActual = new UsuarioOP(0, contenido, "", "", 0);
        }
        if(tipo.equals("LISTA_USUARIOS")){
            UsuarioOP notificacion = new UsuarioOP(0, "LISTA_USUARIOS", contenido, "", 0);
            notificar(notificacion);
            return;
        }
        
        UsuarioOP notificacion = new UsuarioOP(0, tipo, contenido, "", 0);
        notificar(notificacion);
    }

    // --- PATRÓN OBSERVER (IPublicadorNuevoMensaje) ---

    @Override
    public void agregarObservador(INotificadorNuevoMensaje observador) {
        observadores.clear();
        observadores.add(observador);
    }

    @Override
    public void notificar(UsuarioOP usuarioOP) {
        for (INotificadorNuevoMensaje obs : observadores) {
            obs.actualizar(usuarioOP);
        }
    }
}