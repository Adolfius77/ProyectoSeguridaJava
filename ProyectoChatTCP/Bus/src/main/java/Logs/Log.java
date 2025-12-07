/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logs;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author garfi
 */
public class Log {
    private static final String LOG_FILE = "server_logs.txt";

    public static void registrar(String nivel, String mensaje) {
        
        String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        String logMsg = String.format("[%s] [%s] %s", fechaHora, nivel, mensaje);

        System.out.println(logMsg);


        try (FileWriter fw = new FileWriter(LOG_FILE, true); 
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(logMsg);
        } catch (IOException e) {
            System.err.println("Error al escribir el log: " + e.getMessage());
        }
    }
}
