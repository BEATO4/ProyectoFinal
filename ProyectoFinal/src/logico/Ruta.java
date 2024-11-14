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
    
    public Ruta(Parada origen, Parada destino, double distancia, int tiempoViaje, double tarifa) {
        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
        this.tiempoViaje = tiempoViaje;
        this.tarifa = tarifa;
    }
    
    // Getters y setters
    public Parada getOrigen() { return origen; }
    public Parada getDestino() { return destino; }
    public double getDistancia() { return distancia; }
    public int getTiempoViaje() { return tiempoViaje; }
    public double getTarifa() { return tarifa; }
}