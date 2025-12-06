/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author
 */
public class Validaciones {

    // --- Atributo opcional para centrar los JOptionPane ---
    private static JFrame owner;

    /**
     * Permite asignar el frame padre para los JOptionPane.
     * @param frame
     */
    public static void setOwner(JFrame frame) {
        owner = frame;
    }

    // Constructor privado para evitar instanciación
    private Validaciones() {
    }

    /**
     * Muestra un mensaje de error si la validación falla.
     */
    public static void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                owner,
                mensaje,
                "Error de validación",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // -------------------------------------------------------------
    // VALIDACIONES
    // -------------------------------------------------------------
    public static boolean esTextoVacio(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            mostrarError("El campo no puede estar vacío.");
            return true;
        }
        return false;
    }

    public static boolean esContrasenaValida(String contrasena) {
        if (contrasena == null || contrasena.length() < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres.");
            return false;
        }
        return true;
    }

    public static boolean esPuertoValido(int puerto) {
        if (puerto < 0 || puerto > 65535) {
            mostrarError("El puerto debe estar entre 0 y 65535.");
            return false;
        }
        return true;
    }

    public static boolean esNombreUsuarioValido(String nombre) {
        if (nombre == null) {
            mostrarError("El nombre de usuario no puede ser nulo.");
            return false;
        }
        String n = nombre.trim();
        if (n.isEmpty() || n.length() < 3 || n.length() > 20) {
            mostrarError("El nombre debe tener entre 3 y 20 caracteres.");
            return false;
        }
        return true;
    }

    public static boolean esHostValido(String host) {
        if (host == null || host.trim().isEmpty()) {
            mostrarError("El host no puede estar vacío.");
            return false;
        }
        return true;
    }

    public static boolean esMensajeValido(String mensaje) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            mostrarError("El mensaje no puede estar vacío.");
            return false;
        }
        return true;
    }
    
    public static boolean esUsuarioDuplicado(String mensajeServidor, String nombreUsuario) {
        if ("Usuario ya existe".equalsIgnoreCase(mensajeServidor)) {
            JOptionPane.showMessageDialog(
                    owner,
                    "El nombre de usuario '" + nombreUsuario + "' ya existe.",
                    "Usuario Duplicado",
                    JOptionPane.WARNING_MESSAGE
            );
            return true;
        }
        return false;
    }
    
    public static boolean sonContrasenasIguales(String pass1, String pass2) {
        if (pass1 != null && !pass1.equals(pass2)) {
            JOptionPane.showMessageDialog(owner, 
                    "Las contraseñas no coinciden.", 
                    "Error de validación", 
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
}
