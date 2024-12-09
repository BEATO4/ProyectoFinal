package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Inicio extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Gestor de Mapas - Inicio");

        // Botones
        Button btnCargarMapa = new Button("Cargar Mapa");
        Button btnCrearMapa = new Button("Crear Mapa Nuevo");

        // Añadir clases CSS
        btnCargarMapa.getStyleClass().add("button");
        btnCrearMapa.getStyleClass().add("button");

        // Eventos
        btnCargarMapa.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar archivo de mapa");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos JSON", "*.json"));

            File archivoSeleccionado = fileChooser.showOpenDialog(primaryStage);
            if (archivoSeleccionado != null) {
                System.out.println("Archivo seleccionado: " + archivoSeleccionado.getAbsolutePath());

                Main pantallaPrincipal = new Main();
                try {
                    Stage stage = new Stage();
                    pantallaPrincipal.start(stage);
                    pantallaPrincipal.cargarMapaDesdeArchivo(archivoSeleccionado.getAbsolutePath());

                    primaryStage.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        btnCrearMapa.setOnAction(event -> {
            Main pantallaPrincipal = new Main();
            try {
                pantallaPrincipal.start(new Stage());
                primaryStage.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Layout
        VBox layout = new VBox(30, btnCargarMapa, btnCrearMapa);
        layout.setStyle("-fx-alignment: center;");

        // Fondo con gradiente
        StackPane root = new StackPane(layout);

        // Crear escena con tamaño similar a la pantalla principal
        Scene escena = new Scene(root, 1000, 600);

        // Aplicar estilos
        escena.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        primaryStage.setScene(escena);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
