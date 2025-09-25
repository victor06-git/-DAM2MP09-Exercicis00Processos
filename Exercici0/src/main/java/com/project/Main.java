package com.project;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        // Tasca 1: Runnable que simula operacions de manteniment de sistema (ex: registrar esdeveniments)
        Runnable systemTasks = () -> {
            System.out.println("System tasks running...");
            try {
                for (int i = 0; i < 5; i++) {
                    System.out.println("System task " + (i + 1) + " completed.");
                    Thread.sleep(500); // Simula una tasca que triga 0.5 segons
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restaura el flag d'interrupció
                System.out.println("System tasks interrupted.");
            }
            System.out.println("System tasks completed.");
        };

        // Tasca 2: Runnable que simula comprovació de l'estat de la xarxa
        Runnable networkState = () -> {
            System.out.println("Network state checking...");
            try {
                for (int i = 0; i < 3; i++) {
                    System.out.println("Network state check " + (i + 1) + " completed.");
                    Thread.sleep(500); // Simula una tasca que triga 0.5 segons
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restaura el flag d'interrupció
                System.out.println("Network state check interrupted.");
            }
            System.out.println("Network state checked.");
        };

        // Crear un ExecutorService amb un pool de 2 fils
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // Enviar les tasques a l'executor perquè s'executin en paral·lel
            executor.submit(systemTasks);
            executor.submit(networkState);

            // Opcional: Missatge inicial
            System.out.println("Tasques enviades a l'executor. Executant en paral·lel...");
        } finally {
            // Aturar l'executor: no accepta més tasques
            executor.shutdown();
            try {
                // Esperar fins que totes les tasques es completin (màxim 10 segons)
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.out.println("Algunes tasques no es van completar a temps.");
                    // Forçar interrupció si cal
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Espera interrompuda.");
                executor.shutdownNow();
            }
            System.out.println("Executor tancat. Manteniment completat.");
        }
    }
}
