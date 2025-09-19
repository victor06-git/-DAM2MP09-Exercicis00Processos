package com.project;

import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        ExecutorService exec = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);     // ajuda a no retenir el JVM
            return t;
        });

        ConcurrentHashMap<String, Integer> sharedData = new ConcurrentHashMap<>();

        try {
            CompletableFuture<Integer> tasca1 =
                CompletableFuture.supplyAsync(() -> {
                    System.out.println("Tasques en Inserció...");
                    //Validació de dades d'una sol·licitud
                    int total = 0;
                    for (int i = 0; i < 5; i++) {
                        sharedData.put("usuari" + i, i + 100);
                        total += i + 100;
                        System.out.println("Validant sol·licituds usuari " + (i + 1));
                        try {
                            Thread.sleep(500); // Simula una tasca que triga 0.5 segons
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    return "Nombre de sol·licituds d'usuari: " + total;
                }, exec);

            CompletableFuture<Integer> tasca2 = tasca1.thenApplyAsync(result -> {
                System.out.println("Tasques en Modificació...");
                for (int i = 0; i < 5; i++) {
                    String key = "xec" + i;
                    if (sharedData.containsKey(key)) {
                        int newValue = sharedData.get(key) + 100;
                        sharedData.put(key, newValue);
                        System.out.println("Processant sol·licituds usuari " + (i + 1) + " amb xec " + key + " a valor " + newValue + "$.");
                    }
                }
                return sharedData.size();
            }, exec);

            CompletableFuture<Integer> tasca3 = tasca2.thenApplyAsync(result -> {
                System.out.println("Tasques en Future3...");
                
                return result * 2;
            }, exec);

            Integer finalResult = tasca3.join();   // Espera fins que la tasca acabi i obté el resultat
            System.out.println("Resultat final: " + finalResult);
        } finally {
            exec.shutdown();                   // important!
        }
    }
}
