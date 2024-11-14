package logico;

public class Ruta {
	private Parada origen;
    private Parada destino;
    private double distancia;
    private int tiempo;
    private double costo;


    @Override
    public String toString() {
        return origen.getNombre() + "-" + destino.getNombre();  // Mostrar ruta en el formato deseado
    }
    
    public Ruta(Parada origen, Parada destino, double distancia, int tiempo, double costo) {
        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
        this.tiempo = tiempo;
        this.costo = costo;
    }
    
    // Getters y setters
    public Parada getOrigen() { return origen; }
    public Parada getDestino() { return destino; }
    public double getDistancia() { return distancia; }
    public int getTiempo() { return tiempo; }
    public double getCosto() { return costo; }
}