/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package prueba;

import Emisor.ClienteTCP;
import Emisor.ColaEnvios;
import Emisor.Emisor;
import ObserverReceptor.ObservadorRecibos;
import Receptor.ColaRecibos;
import Receptor.Receptor;
import Receptor.ServidorTCP;
import org.itson.componentereceptor.IReceptor;
import org.itson.paquetedto.PaqueteDTO;

/**
 *
 * @author Jck Murrieta
 */
public class MainPrueba {

    public static void main(String[] args) throws InterruptedException {
//        int puerto = 5000;
//        
//        IReceptor componenteReceptor = new IReceptor() {
//            @Override
//            public void recibirCambio(PaqueteDTO paquete) {
//                System.out.println("[IReceptor] Recibí paquete:");
//                System.out.println("  Tipo: " + paquete.getTipoEvento());
//                System.out.println("  Contenido: " + paquete.getContenido());
//            }
//        };
//        
//        ColaRecibos colaRecibos = new ColaRecibos();
//        Receptor receptor = new Receptor(colaRecibos, componenteReceptor);
//
//        // Observador de recibos
//        ObservadorRecibos obsRecibos = receptor;
//        colaRecibos.agregarObservador(obsRecibos);
//
//        // Servidor TCP
//        ServidorTCP servidor = new ServidorTCP(colaRecibos, puerto);
//        new Thread(() -> servidor.iniciar()).start();
//
//        Thread.sleep(500);
//        
//        ColaEnvios colaEnvios = new ColaEnvios();
//        ClienteTCP cliente = new ClienteTCP(colaEnvios, puerto, "localhost");
//
//        // Observador de envíos
//        colaEnvios.agregarObservador(cliente);
//
//        Emisor emisor = new Emisor(colaEnvios);
//        
//        PaqueteDTO<String> paquete = new PaqueteDTO<>("Hola receptor!", "MENSAJE");
//
//        System.out.println("[MAIN] Enviando paquete del emisor...");
//        emisor.enviarCambio(paquete);
//
//        // Esperar para ver toda la secuencia
//        Thread.sleep(2000);
    }
}

