/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DTO;

/**
 *
 * @author Jack Murrrieta
 */
public class UsuarioDTO {

    private String nombreUsuario;
    private String contrasena;
    private String ip;
    private int puerto;
    private String color;
    private String publicKey;

    public UsuarioDTO() {
    }

    public UsuarioDTO(String nombreUsuario, String contrasena, String ip, int puerto, String color, String publicKey) {
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.ip = ip;
        this.puerto = puerto;
        this.color = color;
        this.publicKey = publicKey;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPuerto() {
        return puerto;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    
    

}
