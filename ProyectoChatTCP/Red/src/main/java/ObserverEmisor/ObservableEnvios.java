/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package ObserverEmisor;

/**
 *
 * @author Jck Murrieta
 */
public interface ObservableEnvios {
    public void agregarObservador(ObservadorEnvios ob);
    public void notificar();
}
