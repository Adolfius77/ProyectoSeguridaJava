package Datos;

import Cifrado.GestorSeguridad;
import Logs.Log;
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
                // Log de exito
                Log.registrar("INFO", "Base de datos cargada. Usuarios registrados: " + usuarios.size());
            } else {
                Log.registrar("WARNING", "No se encontró usuarios.json. Se creará uno nuevo.");
            }
        } catch (Exception e) {
            // Log de error
            Log.registrar("ERROR", "Fallo al cargar los datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void guardar() {
        try (FileWriter writer = new FileWriter(ARCHIVO)) {
            gson.toJson(usuarios, writer);
        } catch (Exception e) {
            Log.registrar("ERROR", "No se pudo guardar el usuario en disco: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean registrar(String usuario, String password) {
        if (usuarios.containsKey(usuario)) return false;
        try {
            String hash = GestorSeguridad.hashPassword(password);
            usuarios.put(usuario, hash);
            guardar();
            return true;
        } catch (Exception e) {
            Log.registrar("ERROR", "Error al encriptar contraseña para " + usuario + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean validar(String usuario, String password) {
        if (!usuarios.containsKey(usuario)) return false;
        try {
            String hashInput = GestorSeguridad.hashPassword(password);
            return usuarios.get(usuario).equals(hashInput);
        } catch (Exception e) {
            Log.registrar("ERROR", "Error al validar al usuario: " + e.getMessage());
            return false;
        }
    }
}