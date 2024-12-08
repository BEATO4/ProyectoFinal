package logico;

public class Arista {
	private Nodo origen;
	private Nodo destino;
    private double distancia;
    private int tiempo;
    private double costo;


    @Override
    public String toString() {
        return origen.getNombre() + "-" + destino.getNombre();  // Mostrar ruta en el formato deseado
    }
    
    public Arista(Nodo origen, Nodo destino, double distancia, int tiempo, double costo) {
        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
        this.tiempo = tiempo;
        this.costo = costo;
    }
    
    // Getters y setters
    public Nodo getOrigen() { return origen; }
    public Nodo getDestino() { return destino; }
    public double getDistancia() { return distancia; }
    public int getTiempo() { return tiempo; }
    public double getCosto() { return costo; }
    
    public void setOrigen(Nodo origen) {
		this.origen = origen;
	}

	public void setDestino(Nodo destino) {
		this.destino = destino;
	}

	public void setDistancia(double distancia) {
		this.distancia = distancia;
	}

	public void setTiempo(int tiempo) {
		this.tiempo = tiempo;
	}

	public void setCosto(double costo) {
		this.costo = costo;
	}

}