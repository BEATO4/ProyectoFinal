package logico;

public class Parada {
	private String id;
    private String nombre;
    private double latitud;
    private double longitud;

    // Constructor y otros métodos

    @Override
    public String toString() {
        return nombre;
    }
    
    public Parada(String id, String nombre, double latitud, double longitud) {
        this.id = id;
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
    }
    
    // Getters y setters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
}