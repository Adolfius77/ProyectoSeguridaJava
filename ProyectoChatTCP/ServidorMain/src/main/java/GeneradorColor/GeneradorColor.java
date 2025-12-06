/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GeneradorColor;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author Jack Murrieta
 */
public class GeneradorColor {

    private static GeneradorColor instancia;
    private Set<String> coloresUsados;
    private Random random;

    private GeneradorColor() {
        coloresUsados = new HashSet<>();
        random = new Random();
    }

    // Obtener instancia del singleton
    public static synchronized GeneradorColor getInstancia() {
        if (instancia == null) {
            instancia = new GeneradorColor();
        }
        return instancia;
    }

    // Genera un color aleatorio en formato hexadecimal #RRGGBB
    public String generarColor() {
        String color;
        do {
            color = String.format("#%02X%02X%02X", random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256));
        } while (coloresUsados.contains(color));

        coloresUsados.add(color);
        return color;
    }

    // Permite "liberar" un color si es necesario reutilizarlo
    public void liberarColor(String color) {
        coloresUsados.remove(color);
    }

    // Devuelve los colores ya usados
    public Set<String> getColoresUsados() {
        return new HashSet<>(coloresUsados);
    }

}
