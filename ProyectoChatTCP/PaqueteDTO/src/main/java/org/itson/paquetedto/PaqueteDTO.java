package org.itson.paquetedto;

/**
 *
 * @author Jck Murrieta
 */
public class PaqueteDTO<T> {

    private T contenido;
    private String tipoEvento;
    private String host;
    private int puertoOrigen;
    private int puertoDestino; // del eventBus

    //puerto y host a donde tiene que ir en realidad
    private String hostDestinatario;
    private int puertoDestinatario;
    private String publicKeyDestinatario;

    public PaqueteDTO() {
    }

    public String getHostDestinatario() {
        return hostDestinatario;
    }

    public void setHostDestinatario(String hostDestinatario) {
        this.hostDestinatario = hostDestinatario;
    }

    public int getPuertoDestinatario() {
        return puertoDestinatario;
    }

    public void setPuertoDestinatario(int puertoDestinatario) {
        this.puertoDestinatario = puertoDestinatario;
    }

    public String getPublicKeyDestinatario() {
        return publicKeyDestinatario;
    }

    public void setPublicKeyDestinatario(String publicKeyDestinatario) {
        this.publicKeyDestinatario = publicKeyDestinatario;
    }

    public PaqueteDTO(T contenido, String tipoEvento) {
        this.contenido = contenido;
        this.tipoEvento = tipoEvento;

    }

    public PaqueteDTO(T contenido, String tipoEvento, String host, int puertoOrigen, int puertoDestino) {
        this.contenido = contenido;
        this.tipoEvento = tipoEvento;
        this.host = host;
        this.puertoOrigen = puertoOrigen;
        this.puertoDestino = puertoDestino;
    }

    public T getContenido() {
        return contenido;
    }

    public void setContenido(T contenido) {
        this.contenido = contenido;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPuertoOrigen() {
        return puertoOrigen;
    }

    public void setPuertoOrigen(int puertoOrigen) {
        this.puertoOrigen = puertoOrigen;
    }

    public int getPuertoDestino() {
        return puertoDestino;
    }

    public void setPuertoDestino(int puertoDestino) {
        this.puertoDestino = puertoDestino;
    }

}
