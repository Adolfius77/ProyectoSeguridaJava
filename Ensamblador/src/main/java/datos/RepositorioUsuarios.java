package datos;

import Cifrado.GestorSeguridad;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Repositorio simple para persistencia de usuarios en JSON.
 */
public class RepositorioUsuarios {
    private static final String ARCHIVO = "usuarios.json";
    private static final Gson gson = new Gson();
    // Mapa: Usuario -> HashPassword
    private static Map<String, String> usuarios = new HashMap<>();

    static {
        cargar();
    }

    private static void cargar() {
        try {
            File f = new File(ARCHIVO);
            if (f.exists()) {
                Type type = new TypeToken<HashMap<String, String>>(){}.getType();
                usuarios = gson.fromJson(new FileReader(f), type);
            }
        } catch (Exception e) {
            System.err.println("[Repositorio] Error cargando usuarios: " + e.getMessage());
        }
    }

    private static void guardar() {
        try (FileWriter writer = new FileWriter(ARCHIVO)) {
            gson.toJson(usuarios, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean registrar(String usuario, String passwordRaw) {
        if (usuarios.containsKey(usuario)) return false;
        try {
            // Guardamos el hash, no la contraseña plana
            String hash = GestorSeguridad.hashPassword(passwordRaw);
            usuarios.put(usuario, hash);
            guardar();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean validar(String usuario, String passwordRaw) {
        if (!usuarios.containsKey(usuario)) return false;
        try {
            String hashAlmacenado = usuarios.get(usuario);
            String hashIntento = GestorSeguridad.hashPassword(passwordRaw);
            return hashAlmacenado.equals(hashIntento);
        } catch (Exception e) {
            return false;
        }
    }
}