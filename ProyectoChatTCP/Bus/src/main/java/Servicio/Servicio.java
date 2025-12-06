package Servicio;

/**
 *
 * @author Jck Murrieta
 */
public class Servicio<T> {

    private int id;
    private int puerto;
    private String host;
    private String publicKey;
    private T informacionServicio;

    public Servicio(int puerto, String host) {
        this.puerto = puerto;
        this.host = host;
    }

    public int getPuerto() {
        return puerto;
    }

    public String getHost() {
        return host;
    }

    @Override
    public String toString() {
        return "Servicio{" + "puerto=" + puerto + ", host=" + host + '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public T getInformacionServicio() {
        return informacionServicio;
    }

    public void setInformacionServicio(T informacionServicio) {
        this.informacionServicio = informacionServicio;
    }

}
