package com.project;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        
        //Dues tasques amb runnable
        Runnable systemTasks = () -> {
            System.out.println("System tasks running...");
            try {
                for (int i = 0; i < 5; i++) {
                    System.out.println("System task " + (i + 1) + " completed.");
                    Thread.sleep(500); // Simula una tasca que triga 0.5 segons
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("System tasks completed.");
        };

        // Tasca 2: modificar (consumeix 100 i publica 200)
        Runnable networkState = () -> {
            System.out.println("Network state checking...");
            try {
                for (int i = 0; i < 3; i++) {
                    System.out.println("Network state check " + (i + 1) + " completed.");
                    Thread.sleep(500); // Simula una tasca que triga 0.5 segons
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Network state checked.");
        };
        
        
        // Crear un executor amb un pool de 3 fils
        ExecutorService executor = Executors.newFixedThreadPool(2);
    

        // Enviar les tasques a l'executor
        executor.submit(systemTasks);
        executor.submit(networkState);

        // Aturar l'executor
        executor.shutdown();
    }
}
