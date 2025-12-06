/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ModeloChatTCP;

import org.itson.componentereceptor.IReceptor;
import org.itson.paquetedto.PaqueteDTO;

/**
 *
 * @author Jack Murrieta
 */
public class ReceptorPaquete implements IReceptor {
    
    private LogicaChatTCP logicaChat;
    private LogicaCliente logicaCliente;

    public ReceptorPaquete(LogicaChatTCP logicaChat, LogicaCliente logicaCliente) {
        this.logicaChat = logicaChat;
        this.logicaCliente = logicaCliente;
    }
    

    @Override
    public void recibirCambio(PaqueteDTO paquete) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
