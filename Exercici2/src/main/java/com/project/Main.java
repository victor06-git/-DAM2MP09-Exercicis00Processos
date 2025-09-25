package com.project;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws Exception {
        Random randomNumbers = new Random();
        // Estructura de dades concurrent per compartir informació entre les tasques
        ConcurrentHashMap<String, Integer> sharedData = new ConcurrentHashMap<>();

        // Latch 1: Sincronitza l'inici de la modificació després de la inserció
        CountDownLatch latchInsertionToModification = new CountDownLatch(1);
        // Latch 2: Sincronitza la consulta després de la modificació
        CountDownLatch latchModificationToFetch = new CountDownLatch(1);

        // Tasca 1: Runnable que introdueix les dades inicials (simula recepció d'operació bancària)
        Runnable dataInsertion = () -> {
            System.out.println("[Inserció] Iniciant inserció de dades bancàries...");
            try {
                for (int i = 0; i < 5; i++) {
                    int value = (i + 100) * 100; // Exemple: valors inicials com 10000, 10100, etc.
                    sharedData.put("xec" + i, value);
                    System.out.println("[Inserció] Insertat xec " + i + " amb valor inicial " + value + "€.");
                    Thread.sleep(400); // Simula processament de 0.4 segons
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
            System.out.println("[Inserció] Inserció de dades inicials finalitzada.");
            latchInsertionToModification.countDown(); // Permet que la modificació comenci
        };

        // Tasca 2: Runnable que modifica les dades (simula càlcul d'interessos o comissions)
        Runnable modifyData = () -> {
            try {
                latchInsertionToModification.await(); // Espera la inserció
                System.out.println("[Modificació] Iniciant modificació de dades (interessos/comissions)...");
                for (int i = 0; i < 5; i++) {
                    String key = "xec" + i;
                    if (sharedData.containsKey(key)) {
                        int currentValue = sharedData.get(key);
                        int op = randomNumbers.nextInt(4);
                        int newValue = currentValue;
                        if (op == 0) {
                            // Afegir (ex: interessos)
                            newValue = currentValue + (randomNumbers.nextInt(1000) + 1);
                        } else if (op == 1) {
                            // Restar (ex: comissions)
                            newValue = Math.max(0, currentValue - (randomNumbers.nextInt(1000) + 1)); // Evita negatius
                        } else if (op == 2) {
                            // Multiplicar (ex: compound interest)
                            newValue = currentValue * (randomNumbers.nextInt(3) + 1);
                        } else {
                            // Dividir (ex: repartiment)
                            newValue = currentValue / (randomNumbers.nextInt(3) + 1);
                        }
                        sharedData.put(key, newValue);
                        System.out.println("[Modificació] Modificat " + key + " de " + currentValue + "€ a " + newValue + "€ (operació: " + op + ").");
                        Thread.sleep(400); // Simula processament de 0.4 segons
                    }
                }
                System.out.println("[Modificació] Modificació de dades finalitzada.");
                latchModificationToFetch.countDown(); // Permet que la consulta comenci
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        };

        // Tasca 3: Callable que llegeix les dades modificades i retorna el saldo total actualitzat
        Callable<Integer> fetchData = () -> {
            try {
                latchModificationToFetch.await(); // Espera la modificació
                System.out.println("[Consulta] Iniciant lectura de dades modificades...");
                int total = sharedData.values().stream().mapToInt(Integer::intValue).sum(); // Suma tots els valors
                System.out.println("[Consulta] Saldo total actualitzat dels xecs: " + total + "€.");
                return total;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        };

        // ExecutorService amb pool de 3 fils
        ExecutorService executor = Executors.newFixedThreadPool(3);

        try {
            // Llançar les tasques en paral·lel (amb coordinació via latches per dependències)
            Future<?> insertionFuture = executor.submit(dataInsertion);
            Future<?> modificationFuture = executor.submit(modifyData);
            Future<Integer> fetchFuture = executor.submit(fetchData);

            // Recollir el resultat de la tasca Callable
            Integer saldoFinal = fetchFuture.get(); // Bloqueja fins que es completi i obté el resultat

            // Mostrar el resultat final al client
            System.out.println("\n[Resultat Final] Operació bancària processada. Saldo actualitzat presentat al client: " + saldoFinal + "€.");
        } finally {
            executor.shutdown();
            // Opcional: esperar terminació si cal, però get() ja ho gestiona
            if (!executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }
}
