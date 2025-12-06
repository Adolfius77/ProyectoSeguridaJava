/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package ObserverReceptor;

/**
 *
 * @author Jck Murrieta
 */
public interface ObservableRecibos {
    void agregarObservador(ObservadorRecibos ob);
    void notificar();
}
