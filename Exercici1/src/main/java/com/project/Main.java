package com.project;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws Exception {

        Random randomNumbers = new Random();
        // Compartir dades entre les tasques
        ConcurrentHashMap<String, Integer> sharedData = new ConcurrentHashMap<>();

        // Latch per sincronitzar l'inici de les tasques
        CountDownLatch latch = new CountDownLatch(1);

        // Tasca 1: Inserció de dades
        Runnable dataInsertion = () -> {
            System.out.println("[Inserció] Iniciant inserció de diners...");
            try {
                for (int i = 0; i < 5; i++) {
                    sharedData.put("xec" + i, (i + 100) * 100);
                    System.out.println("[Inserció] Insertat xec " + i + " amb valor " + ((i + 100) * 100) + "$.");
                    Thread.sleep(400); // Simula una tasca que triga 0.4 segons
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("[Inserció] Inserció finalitzada.");
            latch.countDown(); // Permet que les altres tasques comencin a processar
        };

        // Tasca 2: Modificació de dades
        Runnable modifyData = () -> {
            try {
                latch.await(); // Espera fins que la inserció hagi acabat
                System.out.println("[Modificació] Iniciant modificació de diners...");
                for (int i = 0; i < 5; i++) {
                    String key = "xec" + i;
                    if (sharedData.containsKey(key)) {
                        int op = randomNumbers.nextInt(4);
                        int newValue = 0;
                        if (op == 0) {
                            newValue = sharedData.get(key) + (randomNumbers.nextInt(1000) + 1);
                        }
                        else if (op == 1) {
                            newValue = sharedData.get(key) - (randomNumbers.nextInt(1000) + 1);
                        } 
                        else if (op == 2) {
                            newValue = sharedData.get(key) * (randomNumbers.nextInt(3) + 1);
                        } 
                        else {
                            newValue = sharedData.get(key) / (randomNumbers.nextInt(3) + 1);
                        }
                        
                        sharedData.put(key, newValue);
                        System.out.println("[Modificació] Modificat " + key + " a valor " + newValue + "$.");
                    }
                    Thread.sleep(400); // Simula una tasca que triga 0.4 segons
                }
                System.out.println("[Modificació] Modificació finalitzada.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        // Tasca 3: Callable que retorna el total
        Callable<Integer> fetchData = () -> {
            latch.await(); // Espera fins que la inserció hagi acabat
            System.out.println("[Consulta] Calculant total...");
            int total = sharedData.values().stream().mapToInt(Integer::intValue).sum();
            System.out.println("[Consulta] Suma total dels xecs: " + total + "$.");
            return total;
        };

        // Executor amb 3 fils
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Llançar les tasques en paral·lel
        executor.submit(dataInsertion);
        executor.submit(modifyData);
        executor.submit(fetchData);

        executor.shutdown();
    }
}
