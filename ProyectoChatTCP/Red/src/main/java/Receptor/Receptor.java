/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Receptor;


import ObserverReceptor.ObservadorRecibos;
import org.itson.componentereceptor.IReceptor;
import org.itson.paquetedto.PaqueteDTO;

/**
 *
 * @author Jck Murrieta
 */
public class Receptor implements ObservadorRecibos {
    private ColaRecibos cola;
    private IReceptor receptor;
    
    public Receptor() {
    }

    public void setCola(ColaRecibos cola) {
        this.cola = cola;
    }

    public void setReceptor(IReceptor receptor) {
        this.receptor = receptor;
    }

    
    @Override
    public void actualizar() {
        PaqueteDTO paquete = cola.dequeue();

        if (paquete == null) {
            System.out.println("[Receptor] No hay paquetes en cola al recibir la notificación.");
            return;
        }

        System.out.println("[Receptor] Paquete desencriptado recibido:");
        System.out.println("  - Tipo: " + paquete.getTipoEvento());
        System.out.println("  - Host: " + paquete.getHost());
        System.out.println("  - Puerto destino: " + paquete.getPuertoDestino());

        if (receptor != null) {
            receptor.recibirCambio(paquete);
        } else {
            System.err.println("[Receptor] ¡Advertencia! No hay receptor configurado");
        }
    }
}
