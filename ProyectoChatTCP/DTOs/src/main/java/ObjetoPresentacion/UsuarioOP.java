/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ObjetoPresentacion;

/**
 *
 * @author Jack Murrieta
 */
public class UsuarioOP {

    private int idUsuario;
    private String nombre;
    private String ultimoMensaje;
    private String color;          // Color en formato hexadecimal (ej: #A1B2C3)
    private int totalMsjNuevos;

    public UsuarioOP(int idUsuario, String nombre, String ultimoMensaje, String color, int totalMsjNuevos) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.ultimoMensaje = ultimoMensaje;
        this.color = color;
        this.totalMsjNuevos = totalMsjNuevos;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUltimoMensaje() {
        return ultimoMensaje;
    }

    public void setUltimoMensaje(String ultimoMensaje) {
        this.ultimoMensaje = ultimoMensaje;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getTotalMsjNuevos() {
        return totalMsjNuevos;
    }

    public void setTotalMsjNuevos(int totalMsjNuevos) {
        this.totalMsjNuevos = totalMsjNuevos;
    }

}
