package logico;
import java.util.Random;

public class SimuladorTrafico {
    public enum NivelTrafico {
        BAJO(30), MEDIO(60), ALTO(90);

        private final int tiempoEsperaMaximo;

        NivelTrafico(int tiempoEsperaMaximo) {
            this.tiempoEsperaMaximo = tiempoEsperaMaximo;
        }

    }

    private Random random = new Random();

    public NivelTrafico evaluarTrafico() {
        int nivelAleatorio = random.nextInt(3);

        switch (nivelAleatorio) {
            case 0: return NivelTrafico.BAJO;  // 0: Nivel bajo
            case 1: return NivelTrafico.MEDIO; // 1: Nivel medio
            case 2: return NivelTrafico.ALTO;  // 2: Nivel alto
            default: return NivelTrafico.BAJO; // Por defecto
        }
    }

    public double calcularTiempoAdicional(Arista arista) {
        NivelTrafico nivel = evaluarTrafico();
        double tiempoBase = arista.getTiempo();

        switch (nivel) {
            case BAJO:
                return tiempoBase + random.nextInt(10); // Variabilidad pequeña para tráfico bajo
            case MEDIO:
                return tiempoBase + (random.nextInt(30) + 10); // Variabilidad moderada para tráfico medio
            case ALTO:
                return tiempoBase + (random.nextInt(60) + 30); // Variabilidad más alta para tráfico alto
            default:
                return tiempoBase;
        }
    }
}