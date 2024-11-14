package logico;

import java.util.*;

public class GrafoTransporte {
    private Map<String, Parada> paradas = new HashMap<>();
    private Map<Parada, List<Ruta>> adyacencias = new HashMap<>();

    // Método para agregar paradas y rutas

    public void eliminarParada(Parada parada) {
        paradas.remove(parada.getId());
        adyacencias.remove(parada);  // Elimina la parada del grafo

        // Elimina todas las rutas que apuntan a esta parada
        for (List<Ruta> rutas : adyacencias.values()) {
            rutas.removeIf(ruta -> ruta.getDestino().equals(parada) || ruta.getOrigen().equals(parada));
        }
    }
    
    public void eliminarRuta(Ruta ruta) {
        List<Ruta> rutasOrigen = adyacencias.get(ruta.getOrigen());
        if (rutasOrigen != null) {
            rutasOrigen.remove(ruta);
        }
    }
    
    public List<Parada> encontrarRutaMasCorta(String idInicio, String idFin) {
        Parada inicio = paradas.get(idInicio);
        Parada fin = paradas.get(idFin);

        Map<Parada, Double> distancias = new HashMap<>();
        Map<Parada, Parada> predecesores = new HashMap<>();
        PriorityQueue<Parada> queue = new PriorityQueue<>(Comparator.comparing(distancias::get));

        for (Parada parada : paradas.values()) {
            distancias.put(parada, Double.MAX_VALUE);
            predecesores.put(parada, null);
        }
        distancias.put(inicio, 0.0);
        queue.add(inicio);

        while (!queue.isEmpty()) {
            Parada actual = queue.poll();
            if (actual.equals(fin)) break;

            for (Ruta ruta : adyacencias.get(actual)) {
                Parada vecino = ruta.getDestino();
                double nuevaDistancia = distancias.get(actual) + ruta.getDistancia();
                if (nuevaDistancia < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDistancia);
                    predecesores.put(vecino, actual);
                    queue.add(vecino);
                }
            }
        }

        List<Parada> camino = new ArrayList<>();
        for (Parada parada = fin; parada != null; parada = predecesores.get(parada)) {
            camino.add(parada);
        }
        Collections.reverse(camino);
        return camino;
    }
}