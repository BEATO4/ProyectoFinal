package logico;

import java.util.*;

public class Grafo {
    private static Map<String, Nodo> nodos = new HashMap<>();
    private static Map<Nodo, List<Arista>> adyacencias = new HashMap<>();


    public List<Nodo> encontrarAristaMasCorta(String idInicio, String idFin, String criterio, SimuladorTrafico simuladorTrafico) {
        Nodo inicio = nodos.get(idInicio);
        Nodo fin = nodos.get(idFin);

        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Nodo de inicio o fin no encontrado.");
        }

        Map<Nodo, Double> distancias = new HashMap<>();
        Map<Nodo, Nodo> predecesores = new HashMap<>();
        PriorityQueue<Nodo> queue = new PriorityQueue<>(Comparator.comparing(distancias::get));

        // Inicialización
        for (Nodo nodo : nodos.values()) {
            distancias.put(nodo, Double.MAX_VALUE);
            predecesores.put(nodo, null);
        }
        distancias.put(inicio, 0.0);
        queue.add(inicio);

        // Algoritmo de Dijkstra
        while (!queue.isEmpty()) {
            Nodo actual = queue.poll();

            if (actual.equals(fin)) break;

            List<Arista> aristasAdyacentes = adyacencias.getOrDefault(actual, Collections.emptyList());

            for (Arista arista : aristasAdyacentes) {
                Nodo vecino = arista.getDestino();

                double peso = calcularPesoSegunCriterio(arista, criterio, simuladorTrafico);
                if (peso < 0) continue; // Ignorar si hay errores en el cálculo.

                double nuevaDistancia = distancias.get(actual) + peso;

                if (nuevaDistancia < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDistancia);
                    predecesores.put(vecino, actual);
                    queue.add(vecino);
                }
            }
        }

        // Reconstrucción del camino
        List<Nodo> camino = reconstruirCamino(predecesores, inicio, fin);

        if (camino.isEmpty()) {
            throw new IllegalStateException("No existe un camino posible entre los nodos seleccionados.");
        }

        return camino;
    }

    private double calcularPesoSegunCriterio(Arista arista, String criterio, SimuladorTrafico simuladorTrafico) {
        switch (criterio.toLowerCase()) {
            case "distancia":
                return arista.getDistancia();
            case "tiempo":
                double tiempoBase = arista.getTiempo();
                double tiempoAdicional = simuladorTrafico.calcularTiempoAdicional(arista);
                return tiempoBase * tiempoAdicional; // Tiempo ajustado por tráfico
            default:
                throw new IllegalArgumentException("Criterio desconocido: " + criterio);
        }
    }


    private List<Nodo> reconstruirCamino(Map<Nodo, Nodo> predecesores, Nodo inicio, Nodo fin) {
        List<Nodo> camino = new ArrayList<>();
        for (Nodo nodo = fin; nodo != null; nodo = predecesores.get(nodo)) {
            camino.add(nodo);
        }
        Collections.reverse(camino);

        if (!camino.isEmpty() && !camino.get(0).equals(inicio)) {
            camino.clear(); // No es un camino válido
        }

        return camino;
    }


    public static void agregarNodo(Nodo nodo) {
        if (!nodos.containsKey(nodo.getId())) {
            nodos.put(nodo.getId(), nodo);
        } else {
            System.out.println("La Nodo ya existe: " + nodo.getNombre());
        }
    }


    public static void agregarArista(Arista arista) {
        if (nodos.containsKey(arista.getOrigen().getId()) && nodos.containsKey(arista.getDestino().getId())) {
            adyacencias.computeIfAbsent(arista.getOrigen(), k -> new ArrayList<>()).add(arista);
            Arista aristaInversa = new Arista(arista.getDestino(), arista.getOrigen(),
                    arista.getDistancia(), arista.getTiempo(), arista.getCosto());
            adyacencias.computeIfAbsent(arista.getDestino(), k -> new ArrayList<>()).add(aristaInversa);
        } else {
            System.err.println("Error: Nodo origen o destino no existe en el grafo.");
        }
    }


    public List<Nodo> getListaNodos() {
        return new ArrayList<>(nodos.values());
    }

    public static List<Arista> getListaAristas() {
        List<Arista> listaDeAristas = new ArrayList<>();
        for (List<Arista> AristasNodo : adyacencias.values()) {
            listaDeAristas.addAll(AristasNodo);
        }
        return listaDeAristas;
    }

}