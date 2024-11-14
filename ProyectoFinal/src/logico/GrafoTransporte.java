package logico;

import java.util.*;

public class GrafoTransporte {
	private Map<String, Parada> paradas;
    private Map<String, List<Ruta>> listaAdyacencia;
    
    public GrafoTransporte() {
        paradas = new HashMap<>();
        listaAdyacencia = new HashMap<>();
    }
    
    public void agregarParada(Parada parada) {
        paradas.put(parada.getId(), parada);
        listaAdyacencia.put(parada.getId(), new ArrayList<>());
    }
    
    public void agregarRuta(Ruta ruta) {
        String idOrigen = ruta.getOrigen().getId();
        listaAdyacencia.get(idOrigen).add(ruta);
    }
    
    // Implementación del algoritmo de Dijkstra para encontrar la ruta más corta
    public List<Ruta> encontrarRutaMasCorta(String idInicio, String idFin) {
        Map<String, Double> distancias = new HashMap<>();
        Map<String, String> paradasPrevias = new HashMap<>();
        PriorityQueue<String> cola = new PriorityQueue<>(
            (a, b) -> Double.compare(distancias.get(a), distancias.get(b))
        );
        
        // Inicializar distancias
        for (String idParada : paradas.keySet()) {
            distancias.put(idParada, Double.POSITIVE_INFINITY);
        }
        distancias.put(idInicio, 0.0);
        cola.add(idInicio);
        
        while (!cola.isEmpty()) {
            String idParadaActual = cola.poll();
            
            if (idParadaActual.equals(idFin)) {
                break;
            }
            
            for (Ruta ruta : listaAdyacencia.get(idParadaActual)) {
                String idVecino = ruta.getDestino().getId();
                double nuevaDistancia = distancias.get(idParadaActual) + ruta.getDistancia();
                
                if (nuevaDistancia < distancias.get(idVecino)) {
                    distancias.put(idVecino, nuevaDistancia);
                    paradasPrevias.put(idVecino, idParadaActual);
                    cola.add(idVecino);
                }
            }
        }
        
        // Reconstruir el camino
        return reconstruirCamino(idInicio, idFin, paradasPrevias);
    }
    
    private List<Ruta> reconstruirCamino(String idInicio, String idFin, Map<String, String> paradasPrevias) {
        List<Ruta> camino = new ArrayList<>();
        String idActual = idFin;
        
        while (paradasPrevias.containsKey(idActual)) {
            String idPrevio = paradasPrevias.get(idActual);
            for (Ruta ruta : listaAdyacencia.get(idPrevio)) {
                if (ruta.getDestino().getId().equals(idActual)) {
                    camino.add(0, ruta);
                    break;
                }
            }
            idActual = idPrevio;
        }
        
        return camino;
    }
}
