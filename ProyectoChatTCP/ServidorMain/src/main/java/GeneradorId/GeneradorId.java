package GeneradorId;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generador de IDs enteros únicos. Garantiza que cada ID generado no se repita.
 *
 * @author Jack Murrieta
 */
public class GeneradorId {

    private final AtomicInteger contador; // Para generar IDs secuenciales
    private final Set<Integer> usados;    // IDs ya asignados

    public GeneradorId() {
        this.contador = new AtomicInteger(1); // empieza en 1
        this.usados = new HashSet<>();
    }

    /**
     * Genera un nuevo ID único.
     *
     * @return ID único como entero
     */
    public synchronized int generarId() {
        int id;
        do {
            id = contador.getAndIncrement();
        } while (usados.contains(id));
        usados.add(id);
        return id;
    }

    /**
     * Libera un ID previamente generado para que pueda ser reutilizado.
     *
     * @param id ID a liberar
     */
    public synchronized void liberarId(int id) {
        usados.remove(id);
    }

    /**
     * Verifica si un ID ya fue usado
     *
     * @param id ID a verificar
     * @return true si ya está en uso
     */
    public synchronized boolean estaUsado(int id) {
        return usados.contains(id);
    }
}
