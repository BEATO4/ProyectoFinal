public class Arista {
    private Nodo origen;
    private Nodo destino;
    private double distancia;

    public Arista(Nodo origen, Nodo destino, double distancia) {
        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
    }

    // Getters para obtener origen, destino y distancia de la arista
    public Nodo getOrigen() {
        return origen;
    }

    public Nodo getDestino() {
        return destino;
    }

    public double getDistancia() {
        return distancia;
    }
}