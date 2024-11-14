package logico;

import java.util.*;

public class GrafoTransporte {
    private Map<String, Parada> paradas = new HashMap<>();
    private Map<Parada, List<Ruta>> adyacencias = new HashMap<>();


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

        if (inicio == null || fin == null) {
            System.err.println("Error: Parada de inicio o fin no encontrada.");
            return Collections.emptyList();
        }

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
            System.out.println("Evaluando parada actual: " + actual.getNombre());

            if (actual.equals(fin)) break;

            List<Ruta> rutasAdyacentes = adyacencias.get(actual);
            if (rutasAdyacentes == null) continue;

            for (Ruta ruta : rutasAdyacentes) {
                Parada vecino = ruta.getDestino();
                double nuevaDistancia = distancias.get(actual) + ruta.getDistancia();

                if (nuevaDistancia < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDistancia);
                    predecesores.put(vecino, actual);
                    queue.add(vecino);
                    System.out.println("Actualizando distancia para " + vecino.getNombre() + ": " + nuevaDistancia);
                }
            }
        }

        List<Parada> camino = new ArrayList<>();
        for (Parada parada = fin; parada != null; parada = predecesores.get(parada)) {
            camino.add(parada);
        }
        Collections.reverse(camino);

        if (camino.size() == 1 && !camino.get(0).equals(inicio)) {
            System.err.println("No existe una ruta posible entre las paradas seleccionadas.");
            return Collections.emptyList();
        }

        System.out.println("Ruta encontrada: ");
        camino.forEach(p -> System.out.print(p.getNombre() + " -> "));
        System.out.println("FIN");

        return camino;
    }

    
    public void agregarParada(Parada parada) {
        if (!paradas.containsKey(parada.getId())) {
            paradas.put(parada.getId(), parada);
        } else {
            System.out.println("La parada ya existe: " + parada.getNombre());
        }
    }


    public void agregarRuta(Ruta ruta) {
        // Evitar agregar la ruta si ya existe una igual entre origen y destino
        List<Ruta> rutasOrigen = adyacencias.computeIfAbsent(ruta.getOrigen(), k -> new ArrayList<>());
        boolean rutaYaExiste = rutasOrigen.stream().anyMatch(r -> r.getDestino().equals(ruta.getDestino()));
        if (!rutaYaExiste) {
            rutasOrigen.add(ruta);
        }

        // Agregar la ruta inversa si es bidireccional
        List<Ruta> rutasDestino = adyacencias.computeIfAbsent(ruta.getDestino(), k -> new ArrayList<>());
        boolean rutaInversaExiste = rutasDestino.stream().anyMatch(r -> r.getDestino().equals(ruta.getOrigen()));
        if (!rutaInversaExiste) {
            Ruta rutaInversa = new Ruta(ruta.getDestino(), ruta.getOrigen(), ruta.getDistancia(), ruta.getTiempo(), ruta.getCosto());
            rutasDestino.add(rutaInversa);
        }
        
    }



    public List<Parada> getListaParadas() {
        return new ArrayList<>(paradas.values());
    }

    public List<Ruta> getListaRutas() {
        List<Ruta> listaDeRutas = new ArrayList<>();
        for (List<Ruta> rutasParada : adyacencias.values()) {
            listaDeRutas.addAll(rutasParada);
        }
        return listaDeRutas;
    }

}