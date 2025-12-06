package Servicio;



/**
 * 
 * @author Jck Murrieta
 */
public class Servicio  {
    
    private int puerto;
    private String host;

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



}
