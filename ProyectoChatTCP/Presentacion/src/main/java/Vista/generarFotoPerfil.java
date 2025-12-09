/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;

/**
 *
 * @author riosr
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

public class generarFotoPerfil {

    /**
     * Genera una imagen de perfil circular con un color de fondo aleatorio
     * y las iniciales del primer nombre y primer apellido.
     *
     * @param nombreCompleto El nombre completo de la persona (ej: "Juan Pérez").
     * @param tamano El tamaño de la imagen en píxeles (ej: 200x200).
     * @return Los datos binarios (byte array) de la imagen PNG generada.
     * @throws IOException Si ocurre un error al escribir la imagen.
     */
    public static byte[] generarFotoPerfilIniciales(String nombreCompleto, int tamano) throws IOException {
        // 1. Obtener las iniciales
        String iniciales = obtenerIniciales(nombreCompleto);

        // 2. Generar Color Aleatorio
        Color colorFondo = generarColorFondoAleatorio();

        // 3. Crear la Imagen Cuadrada con Transparencia (ARGB)
        // Usamos ARGB para soportar transparencia (el círculo se genera como un área no transparente)
        BufferedImage imagen = new BufferedImage(tamano, tamano, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = imagen.createGraphics();

        // 4. Configurar el Dibujo y la Antialias
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 5. Dibujar el Círculo de Fondo
        g2.setColor(colorFondo);
        // Dibujar el círculo que llenará todo el espacio (0, 0, tamano, tamano)
        g2.fillOval(0, 0, tamano, tamano);
        
        // 6. Configurar el Texto
        g2.setColor(Color.WHITE); // Color blanco para las iniciales
        
        // Establecer fuente y tamaño (aproximadamente 50% del tamaño de la imagen)
        int tamanoFuente = (int) (tamano * 0.5);
        // Usamos una fuente común como Arial o SansSerif
        Font fuente = new Font("SansSerif", Font.BOLD, tamanoFuente); 
        g2.setFont(fuente);
        
        // Obtener métricas para centrar el texto
        FontMetrics fm = g2.getFontMetrics();
        int anchoTexto = fm.stringWidth(iniciales);
        int altoTexto = fm.getHeight();

        // 7. Calcular posición para centrar el texto
        int x = (tamano - anchoTexto) / 2;
        // Ajuste vertical: centrar la línea base de la fuente, luego subir la mitad de su altura.
        int y = (tamano - altoTexto) / 2 + fm.getAscent(); 
        
        // 8. Dibujar el Texto
        g2.drawString(iniciales, x, y);

        // 9. Limpiar el contexto gráfico
        g2.dispose();

        // 10. Guardar la imagen en un array de bytes (PNG)
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(imagen, "png", baos);
            return baos.toByteArray();
        }
    }

    /**
     * Extrae hasta dos iniciales del primer nombre y primer apellido.
     */
    private static String obtenerIniciales(String nombreCompleto) {
        String[] partes = nombreCompleto.trim().split("\\s+");
        StringBuilder iniciales = new StringBuilder();

        if (partes.length > 0) {
            // Inicial del primer nombre
            iniciales.append(partes[0].toUpperCase().charAt(0));
            
            // Inicial del apellido, si existe
            if (partes.length > 1) {
                 // Busca el primer componente que no sea una preposición común para el apellido
                 for (int i = 1; i < partes.length; i++) {
                    String parte = partes[i].toLowerCase();
                    // Evitar palabras comunes en apellidos
                    if (!parte.matches("de|del|la|las|los|el|un|una|y|a|e|i|o|u")) {
                        iniciales.append(partes[i].toUpperCase().charAt(0));
                        break;
                    }
                 }
                 // Si solo se obtuvo una inicial y hay más de una palabra, forzar la segunda
                 if (iniciales.length() == 1) {
                     iniciales.append(partes[1].toUpperCase().charAt(0));
                 }
            }
        }
        // Limitar a 2 iniciales para el diseño
        return iniciales.length() > 2 ? iniciales.substring(0, 2) : iniciales.toString();
    }

    /**
     * Genera un color RGB oscuro aleatorio.
     */
    private static Color generarColorFondoAleatorio() {
        Random random = new Random();
        // Generar colores que no sean demasiado claros para el texto blanco.
        int r = random.nextInt(170); // 0 - 169
        int g = random.nextInt(170);
        int b = random.nextInt(170);
        return new Color(r, g, b);
    }
}
