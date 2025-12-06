package org.itson.ensamblador;

import Cifrado.GestorSeguridad;
import Emisor.ClienteTCP;
import Emisor.ColaEnvios;
import Emisor.Emisor;
import Receptor.ColaRecibos;
import Receptor.Receptor;
import Receptor.ServidorTCP;
import org.itson.componenteemisor.IEmisor;
import org.itson.componentereceptor.IReceptor;

public class EnsambladorRed {

    private static EnsambladorRed instancia;
    
    private ServidorTCP servidor;
    private ClienteTCP clienteTCP;
    private Emisor emisor;
    private GestorSeguridad seguridad;
    
    // Configuración: El cliente escucha en un puerto libre y envía al puerto del Bus (5556)
    private int puertoEscucha = 0; 
    private String hostDestino = "localhost";
    private int puertoDestino = 5556;

    private EnsambladorRed() {
        try {
            this.seguridad = new GestorSeguridad();
        } catch (Exception e) {
            System.err.println("[Ensamblador] Error iniciando seguridad: " + e.getMessage());
        }
    }

    public static synchronized EnsambladorRed getInstancia() {
        if (instancia == null) {
            instancia = new EnsambladorRed();
        }
        return instancia;
    }

    public IEmisor ensamblar(IReceptor receptorLogica) {
        // 1. Preparar recepción (Escuchar respuestas del servidor)
        ColaRecibos colaRecibos = new ColaRecibos();
        
        this.servidor = new ServidorTCP(colaRecibos, puertoEscucha);
        this.servidor.setCifradoHabilitado(true);
        
        Receptor receptorObj = new Receptor();
        receptorObj.setCola(colaRecibos);
        receptorObj.setReceptor(receptorLogica);
        colaRecibos.agregarObservador(receptorObj);

        // Iniciar servidor en hilo separado
        new Thread(() -> servidor.iniciar()).start();

        // 2. Preparar emisión (Enviar al Bus)
        ColaEnvios colaEnvios = new ColaEnvios();
        this.clienteTCP = new ClienteTCP(colaEnvios, puertoDestino, hostDestino);
        this.clienteTCP.setCifradoHabilitado(true);
        colaEnvios.agregarObservador(this.clienteTCP);
        
        this.emisor = new Emisor(colaEnvios);
        
        return this.emisor;
    }
    
    public byte[] getPublicKey() {
        return seguridad != null ? seguridad.obtenerPublicaBytes() : null;
    }
    
    // Asumiremos puerto fijo 5555 para el cliente en este ejemplo para simplificar,
    // o el puerto configurado en properties si lo tienes.
    public int getPuertoEscucha() {
        return 5555; 
    }
}