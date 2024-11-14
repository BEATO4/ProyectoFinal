package application;

import java.util.*;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import logico.GrafoTransporte;
import logico.Parada;
import logico.Ruta;

public class InterfazDeTransporte extends Application {
	private ObservableList<Parada> paradas = FXCollections.observableArrayList();
    private ObservableList<Ruta> rutas = FXCollections.observableArrayList();
    private GrafoTransporte grafo = new GrafoTransporte();

    @Override
    public void start(Stage primaryStage) {
        // Crear componentes de la interfaz
        Label labelParadas = new Label("Paradas");
        ListView<Parada> listaParadas = new ListView<>();

        Label labelRutas = new Label("Rutas");
        ListView<Ruta> listaRutas = new ListView<>();

        Button btnAgregarParada = new Button("Agregar Parada");
        Button btnAgregarRuta = new Button("Agregar Ruta");
        Button btnCalcularRuta = new Button("Calcular Ruta");

        // Organizar componentes en un GridPane
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        grid.add(labelParadas, 0, 0);
        grid.add(listaParadas, 0, 1);
        grid.add(btnAgregarParada, 0, 2);

        grid.add(labelRutas, 1, 0);
        grid.add(listaRutas, 1, 1);
        grid.add(btnAgregarRuta, 1, 2);
        grid.add(btnCalcularRuta, 1, 3);

        // Crear escena y establecer la ventana principal
        Scene scene = new Scene(grid, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sistema de Gestión de Rutas de Transporte Público");
        primaryStage.show();

        // Agregar funcionalidad a los botones
        btnAgregarParada.setOnAction(event -> agregarParada(listaParadas));
        btnAgregarRuta.setOnAction(event -> agregarRuta(listaParadas, listaRutas));
        btnCalcularRuta.setOnAction(event -> calcularRutaMasCorta());
        
        Button btnEliminarParada = new Button("Eliminar Parada");
        Button btnEliminarRuta = new Button("Eliminar Ruta");
        grid.add(btnEliminarParada, 0, 3);  
        grid.add(btnEliminarRuta, 1, 4);    
        
        btnEliminarParada.setOnAction(event -> eliminarParada(listaParadas));
        btnEliminarRuta.setOnAction(event -> eliminarRuta(listaRutas));
    }

    private void agregarParada(ListView<Parada> listaParadas) {
        TextInputDialog dialogNombre = new TextInputDialog();
        dialogNombre.setTitle("Agregar Parada");
        dialogNombre.setHeaderText("Ingrese el nombre de la nueva parada:");
        Optional<String> resultNombre = dialogNombre.showAndWait();

        TextInputDialog dialogLatitud = new TextInputDialog();
        dialogLatitud.setTitle("Agregar Parada");
        dialogLatitud.setHeaderText("Ingrese la latitud de la nueva parada:");
        Optional<String> resultLatitud = dialogLatitud.showAndWait();

        TextInputDialog dialogLongitud = new TextInputDialog();
        dialogLongitud.setTitle("Agregar Parada");
        dialogLongitud.setHeaderText("Ingrese la longitud de la nueva parada:");
        Optional<String> resultLongitud = dialogLongitud.showAndWait();

        if (resultNombre.isPresent() && resultLatitud.isPresent() && resultLongitud.isPresent()) {
            String nombre = resultNombre.get();
            double latitud = Double.parseDouble(resultLatitud.get());
            double longitud = Double.parseDouble(resultLongitud.get());
            Parada nuevaParada = new Parada(UUID.randomUUID().toString(), nombre, latitud, longitud);
            paradas.add(nuevaParada);
            grafo.agregarParada(nuevaParada);
            actualizarListaParadas(listaParadas);
        }
    }

    private void agregarRuta(ListView<Parada> listaParadas, ListView<Ruta> listaRutas) {
        ChoiceDialog<Parada> dialogOrigen = new ChoiceDialog<>(listaParadas.getItems().get(0), listaParadas.getItems());
        dialogOrigen.setTitle("Agregar Ruta");
        dialogOrigen.setHeaderText("Seleccione la parada de origen:");
        Optional<Parada> resultOrigen = dialogOrigen.showAndWait();

        ChoiceDialog<Parada> dialogDestino = new ChoiceDialog<>(listaParadas.getItems().get(1), listaParadas.getItems());
        dialogDestino.setTitle("Agregar Ruta");
        dialogDestino.setHeaderText("Seleccione la parada de destino:");
        Optional<Parada> resultDestino = dialogDestino.showAndWait();

        if (resultOrigen.isPresent() && resultDestino.isPresent()) {
            Parada origen = resultOrigen.get();
            Parada destino = resultDestino.get();

            TextInputDialog dialogDistancia = new TextInputDialog();
            dialogDistancia.setTitle("Agregar Ruta");
            dialogDistancia.setHeaderText("Ingrese la distancia de la ruta (en km):");
            Optional<String> resultDistancia = dialogDistancia.showAndWait();

            TextInputDialog dialogTiempo = new TextInputDialog();
            dialogTiempo.setTitle("Agregar Ruta");
            dialogTiempo.setHeaderText("Ingrese el tiempo de la ruta (en minutos):");
            Optional<String> resultTiempo = dialogTiempo.showAndWait();

            if (resultDistancia.isPresent() && resultTiempo.isPresent()) {
                double distancia = Double.parseDouble(resultDistancia.get());
                int tiempo = Integer.parseInt(resultTiempo.get());
                Ruta nuevaRuta = new Ruta(origen, destino, distancia, tiempo, 5.0);
                rutas.add(nuevaRuta);
                grafo.agregarRuta(nuevaRuta);
                actualizarListaRutas(listaRutas);
            }
        }
    }

    private void calcularRutaMasCorta() {
        // Lógica para calcular la ruta más corta
        String idInicio = paradas.get(0).getId();
        String idFin = paradas.get(paradas.size() - 1).getId();
        grafo.encontrarRutaMasCorta(idInicio, idFin);
        ChoiceDialog<Parada> dialogInicio = new ChoiceDialog<>(paradas.get(0), paradas);
        dialogInicio.setTitle("Calcular Ruta Más Corta");
        dialogInicio.setHeaderText("Seleccione la parada de inicio:");
        Optional<Parada> resultInicio = dialogInicio.showAndWait();
        ChoiceDialog<Parada> dialogFin = new ChoiceDialog<>(paradas.get(1), paradas);
        dialogFin.setTitle("Calcular Ruta Más Corta");
        dialogFin.setHeaderText("Seleccione la parada de destino:");
        Optional<Parada> resultFin = dialogFin.showAndWait();
        if (resultInicio.isPresent() && resultFin.isPresent()) {
            Parada inicio = resultInicio.get();
            Parada fin = resultFin.get();
            List<Parada> rutaCorta = grafo.encontrarRutaMasCorta(inicio.getId(), fin.getId());
            if (!rutaCorta.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Ruta Más Corta");
                alert.setHeaderText("Ruta desde " + inicio + " hasta " + fin);
                StringBuilder rutaDetalles = new StringBuilder("Ruta: ");
                for (Parada parada : rutaCorta) {
                    rutaDetalles.append(parada).append(" -> ");
                }
                rutaDetalles.setLength(rutaDetalles.length() - 4);  // Eliminar el último " -> "
                alert.setContentText(rutaDetalles.toString());
                alert.showAndWait();
            }
        }
    }
    
    private void eliminarParada(ListView<Parada> listaParadas) {
        Parada seleccionada = listaParadas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            grafo.eliminarParada(seleccionada);
            actualizarListaParadas(listaParadas);
        } else {
            mostrarAlerta("Eliminar Parada", "Seleccione una parada para eliminar.");
        }
    }
    private void eliminarRuta(ListView<Ruta> listaRutas) {
        Ruta seleccionada = listaRutas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            grafo.eliminarRuta(seleccionada);
            actualizarListaRutas(listaRutas);
        } else {
            mostrarAlerta("Eliminar Ruta", "Seleccione una ruta para eliminar.");
        }
    }
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void actualizarListaParadas(ListView<Parada> listaParadas) {
        ObservableList<Parada> nuevasParadas = FXCollections.observableArrayList(grafo.getListaParadas());
        listaParadas.setItems(nuevasParadas);
    }

    private void actualizarListaRutas(ListView<Ruta> listaRutas) {
        ObservableList<Ruta> nuevasRutas = FXCollections.observableArrayList(grafo.getListaRutas());
        listaRutas.setItems(nuevasRutas);
    }

    public static void main(String[] args) {
        launch(args);
    }
}