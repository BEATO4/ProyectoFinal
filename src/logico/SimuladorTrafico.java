package logico;

public class SimuladorTrafico {
    public enum NivelTrafico {
        BAJO(30), MEDIO(60), ALTO(90);

        private final int tiempoEsperaMaximo;

        NivelTrafico(int tiempoEsperaMaximo) {
            this.tiempoEsperaMaximo = tiempoEsperaMaximo;
        }

        public int getTiempoEsperaMaximo() {
            return tiempoEsperaMaximo;
        }
    }

    public NivelTrafico evaluarTrafico(Arista Arista) {
        int tiempo = Arista.getTiempo();

        if (tiempo <= NivelTrafico.BAJO.getTiempoEsperaMaximo()) {
            return NivelTrafico.BAJO;
        } else if (tiempo <= NivelTrafico.MEDIO.getTiempoEsperaMaximo()) {
            return NivelTrafico.MEDIO;
        } else {
            return NivelTrafico.ALTO;
        }
    }

    public double calcularTiempoAdicional(Arista arista) {
        NivelTrafico nivel = evaluarTrafico(arista);
        switch (nivel) {
            case BAJO: return arista.getTiempo(); // Sin ajuste
            case MEDIO: return arista.getTiempo() * 1.5; // Incremento del 50%
            case ALTO: return arista.getTiempo() * 2.0; // Duplicar tiempo
            default: return arista.getTiempo(); // Por seguridad
        }
    }

    }