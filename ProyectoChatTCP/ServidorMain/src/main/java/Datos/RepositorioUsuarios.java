package Datos;

import Cifrado.GestorSeguridad;
import DTO.UsuarioDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RepositorioUsuarios {

    private static final String ARCHIVO = "usuarios.json";
    private static final Gson gson = new Gson();

    // Guardamos usuario → UsuarioDTO
    private static Map<String, UsuarioDTO> usuarios = new HashMap<>();

    static {
        cargar();
    }

    private static void cargar() {
        try {
            File f = new File(ARCHIVO);
            if (f.exists()) {
                Type type = new TypeToken<HashMap<String, UsuarioDTO>>(){}.getType();
                usuarios = gson.fromJson(new FileReader(f), type);
            }
        } catch (Exception e) {
            System.err.println("Error cargando usuarios: " + e.getMessage());
        }
    }

    private static void guardar() {
        try (FileWriter writer = new FileWriter(ARCHIVO)) {
            gson.toJson(usuarios, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------- REGISTRO COMPLETO DE USUARIO ----------
    public static boolean registrar(UsuarioDTO usuario) {
        String nombre = usuario.getNombreUsuario();

        if (usuarios.containsKey(nombre)) {
            return false; // Ya existe
        }

        try {
            // Hash a la contraseña ANTES de guardar
            String hash = GestorSeguridad.hashPassword(usuario.getContrasena());
            usuario.setContrasena(hash);

            usuarios.put(nombre, usuario);
            guardar();
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    // ---------- VALIDACIÓN ----------
    public static boolean validar(String usuario, String password) {
        if (!usuarios.containsKey(usuario)) {
            return false;
        }

        try {
            UsuarioDTO u = usuarios.get(usuario);

            String hash = GestorSeguridad.hashPassword(password);
            return u.getContrasena().equals(hash);

        } catch (Exception e) {
            return false;
        }
    }

    // ---------- OBTENER USUARIO COMPLETO ----------
    public static UsuarioDTO obtener(String usuario) {
        return usuarios.get(usuario);
    }

    // ---------- ACTUALIZAR USUARIO (IP, PUERTO, COLOR, KEYS, ETC) ----------
    public static void actualizar(UsuarioDTO usuario) {
        usuarios.put(usuario.getNombreUsuario(), usuario);
        guardar();
    }
}
