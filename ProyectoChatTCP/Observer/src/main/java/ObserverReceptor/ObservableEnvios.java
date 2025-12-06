/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ObserverReceptor;

/**
 *
 * @author USER
 */
public interface ObservableEnvios {
    public void agregarObservador(ObservadorEnvios ob);
    public void notificar();
}
