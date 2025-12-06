package ensamblador;

import Cifrado.GestorSeguridad;
import Emisor.*;
import Receptor.*;
import org.itson.componenteemisor.IEmisor;
import org.itson.componentereceptor.IReceptor;

public class EnsambladorRed {
    private static EnsambladorRed instancia;
    private ServidorTCP servidor;
    private ClienteTCP clienteTCP;
    private Emisor emisor;
    private GestorSeguridad seguridad;
    private int puertoEscucha = 0; // 0 = Dinámico

    private EnsambladorRed() {
        try { seguridad = new GestorSeguridad(); } catch (Exception e) {}
    }

    public static EnsambladorRed getInstancia() {
        if (instancia == null) instancia = new EnsambladorRed();
        return instancia;
    }

    public IEmisor ensamblar(IReceptor receptorLogica) {
        // Receptor (Escuchar respuestas)
        ColaRecibos colaRecibos = new ColaRecibos();
        servidor = new ServidorTCP(colaRecibos, puertoEscucha);
        servidor.setCifradoHabilitado(true);
        
        Receptor receptorObj = new Receptor();
        receptorObj.setCola(colaRecibos);
        receptorObj.setReceptor(receptorLogica);
        colaRecibos.agregarObservador(receptorObj);
        
        new Thread(() -> servidor.iniciar()).start();

        // Emisor (Enviar al Bus)
        ColaEnvios colaEnvios = new ColaEnvios();
        // Puerto 5556 es el Bus
        clienteTCP = new ClienteTCP(colaEnvios, 5556, "localhost"); 
        clienteTCP.setCifradoHabilitado(true);
        colaEnvios.agregarObservador(clienteTCP);
        emisor = new Emisor(colaEnvios);
        
        return emisor;
    }
    
    public byte[] getPublicKey() { return seguridad.obtenerPublicaBytes(); }
    // IMPORTANTE: Por ahora usamos 5555 fijo si no tienes lógica de puerto dinámico
    public int getPuertoEscucha() { return 5555; } 
}