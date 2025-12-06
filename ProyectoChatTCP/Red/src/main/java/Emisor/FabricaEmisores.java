/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Emisor;

/**
 *
 * @author Jck Murrieta
 */
public class FabricaEmisores {
   
    public Emisor getEmisor() {
        ColaEnvios cola = new ColaEnvios();
        return new Emisor(cola);
    }
}
