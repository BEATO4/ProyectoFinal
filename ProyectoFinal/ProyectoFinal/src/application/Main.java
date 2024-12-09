package application;
import com.google.gson.JsonSyntaxException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import logico.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

public class Main extends Application {

	private Pane canvas;
	private List<Circle> nodos = new ArrayList<>();
	private List<Line> aristas = new ArrayList<>();
	private List<AristaInfo> aristasInfo = new ArrayList<>();
	private List<Label> aristaLabels = new ArrayList<>();
	private Circle selectedNode = null;
	private Line selectedArista = null;
	private SimuladorTrafico simuladorTrafico = new SimuladorTrafico();
	private Grafo grafo = new Grafo();
	private Circle nodoInicio = null;
	private Circle nodoFin = null;
	private String archivoPendienteCargar = null;


    @Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Gestor de Rutas");

		// Botones
		Button btnAgregarNodo = new Button("Agregar Nodo");
		Button btnAgregarRuta = new Button("Agregar Ruta");
		Button btnModificarNodo = new Button("Modificar Nodo");
		Button btnModificarArista = new Button("Modificar Arista");
		Button btnEliminarNodo = new Button("Eliminar Nodo");
		Button btnEliminarArista = new Button("Eliminar Arista");
		Button btnCalcularRutaCorta = new Button("Calcular Ruta Más Corta");
		Button btnSeleccionarNodos = new Button("Seleccionar Nodos");
		Button btnAtras = new Button("Atrás");
		Button btnGuardar = new Button("Guardar");

		// ComboBox para seleccionar el dato de las aristas
		ComboBox<String> cbxDatosArista = new ComboBox<>();
		cbxDatosArista.getItems().addAll("Distancia", "Tiempo", "Costo");
		cbxDatosArista.setValue("Distancia");

		// Área de dibujo
		canvas = new Pane();
		canvas.getStyleClass().add("canvas");

		// Panel lateral con botones
        VBox controlPanel = new VBox(10, btnAgregarNodo, btnAgregarRuta, btnModificarNodo, btnModificarArista,
                btnEliminarNodo, btnEliminarArista, btnSeleccionarNodos, btnCalcularRutaCorta, cbxDatosArista, btnAtras, btnGuardar);
        controlPanel.getStyleClass().add("vbox");

        TextArea resultadoRuta = new TextArea();
        resultadoRuta.setEditable(false);
        resultadoRuta.setWrapText(true);
		resultadoRuta.setPrefHeight(150);
		resultadoRuta.setPrefWidth(180);
		controlPanel.getChildren().add(resultadoRuta);

		// Layout principal
		BorderPane root = new BorderPane();
		root.setLeft(controlPanel);
		root.setCenter(canvas);
		root.getStyleClass().add("root");

		// Eventos
		btnAgregarNodo.setOnAction(event -> activarModoAgregarNodo());
		btnAgregarRuta.setOnAction(event -> activarModoAgregarRuta());
		btnModificarNodo.setOnAction(event -> activarModoModificarNodo());
		btnModificarArista.setOnAction(event -> activarModoModificarArista());
		btnEliminarNodo.setOnAction(event -> eliminarNodo());
		btnEliminarArista.setOnAction(event -> eliminarArista());

		btnSeleccionarNodos.setOnAction(event -> {
			nodoInicio = null;
			nodoFin = null;

			for (Circle nodo : nodos) {
				nodo.getStyleClass().clear();
				nodo.getStyleClass().add("node"); // Clase CSS para nodos normales
			}
			mostrarMensaje("Haz clic en los nodos para seleccionar inicio y fin.");
			canvas.setOnMouseClicked(null);

			for (Circle nodo : nodos) {
				nodo.setOnMouseClicked(e -> {
					if (nodoInicio == null) {
						seleccionarNodoInicio(nodo);
						mostrarMensaje("Nodo de inicio seleccionado.");
					} else if (nodoFin == null) {
						seleccionarNodoFin(nodo);
						mostrarMensaje("Nodo de fin seleccionado.");
					} else {
						mostrarMensaje("Ya seleccionaste los nodos de inicio y fin.");
					}
				});
			}
		});

		btnCalcularRutaCorta.setOnAction(event -> {
			if (nodoInicio == null || nodoFin == null) {
				mostrarMensaje("Primero selecciona los nodos de inicio y fin usando el botón 'Seleccionar Nodos'.");
				return;
			}

			String criterio = cbxDatosArista.getValue();
            calcularRutaCorta(criterio, resultadoRuta);
		});

		btnAtras.setOnAction(event -> {
			Inicio inicioScreen = new Inicio();
			try {
				inicioScreen.start(new Stage());
				((Stage) canvas.getScene().getWindow()).close();
			} catch (Exception e) {
				e.printStackTrace();
				mostrarMensaje("Error al volver a la pantalla de inicio: " + e.getMessage());
			}
		});

		btnGuardar.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Guardar Mapa");
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos JSON", "*.json"));

			File archivoGuardar = fileChooser.showSaveDialog(canvas.getScene().getWindow());
			if (archivoGuardar != null) {
				try {
					MapaData mapaData = new MapaData(grafo);

					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					String jsonData = gson.toJson(mapaData);

					try (java.io.FileWriter writer = new java.io.FileWriter(archivoGuardar)) {
						writer.write(jsonData);
						mostrarMensaje("Mapa guardado exitosamente en " + archivoGuardar.getAbsolutePath());
					}
				} catch (IOException e) {
					e.printStackTrace();
					mostrarMensaje("Error al guardar el mapa: " + e.getMessage());
				}
			}
		});

		cbxDatosArista.setOnAction(event -> {
			String selectedCriterion = cbxDatosArista.getValue();
			actualizarEtiquetasAristas(selectedCriterion);
			actualizarColorAristas(selectedCriterion);
			mostrarMensaje("Colores de las aristas actualizados según el criterio: " + selectedCriterion);
		});

		// Cargar el CSS
		Scene scene = new Scene(root, 1000, 600);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void activarModoAgregarNodo() {
		canvas.setOnMouseClicked(event -> {
			double x = event.getX();
			double y = event.getY();

			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("Nombre del Nodo");
			dialog.setHeaderText("Ingrese el nombre para este nodo:");
			dialog.setContentText("Nombre:");

			Optional<String> result = dialog.showAndWait();
			result.ifPresent(nombre -> {
				// Crear el nodo lógico
				Nodo nuevoNodo = new Nodo(UUID.randomUUID().toString(), nombre, x, y);
				grafo.agregarNodo(nuevoNodo);

				// Crear el nodo visual
				Circle nodoVisual = new Circle(x, y, 10);
				nodoVisual.getStyleClass().add("node");
				nodoVisual.setUserData(nuevoNodo.getId());
				nodoVisual.setOnMouseClicked(e -> seleccionarNodo(nodoVisual));
				nodos.add(nodoVisual);
				canvas.getChildren().add(nodoVisual);

				// Añadir una etiqueta con el nombre
				Label label = new Label(nombre);
				label.setLayoutX(x + 12);
				label.setLayoutY(y - 5);
				canvas.getChildren().add(label);

				mostrarMensaje("Nodo agregado: " + nombre);
				canvas.setOnMouseClicked(null);
			});
		});
	}

	private void activarModoAgregarRuta() {
		canvas.setOnMouseClicked(null);
		selectedNode = null;

		for (Circle nodo : nodos) {
			nodo.setOnMouseClicked(event -> {
				if (selectedNode == null) {
					selectedNode = nodo;
					nodo.getStyleClass().add("node-selected");
				} else {
					Circle selectedNode2 = nodo;
					selectedNode2.getStyleClass().add("node-selected");

					if (!selectedNode.equals(selectedNode2)) {
						crearRuta(selectedNode, selectedNode2);
					}

					// Restablece estilos
					selectedNode.getStyleClass().remove("node-selected");
					selectedNode.getStyleClass().add("node");

					selectedNode2.getStyleClass().remove("node-selected");
					selectedNode2.getStyleClass().add("node");

					selectedNode = null;
				}
			});
		}
	}

	private void crearRuta(Circle nodo1, Circle nodo2) {
		Dialog<AristaInfo> dialog = new Dialog<>();
		dialog.setTitle("Crear Ruta");
		dialog.setHeaderText("Ingrese los detalles de la nueva ruta:");

		// Layout del formulario
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);

		TextField txtDistancia = new TextField();
		TextField txtTiempo = new TextField();
		grid.add(new Label("Distancia:"), 0, 0);
		grid.add(txtDistancia, 1, 0);
		grid.add(new Label("Tiempo:"), 0, 1);
		grid.add(txtTiempo, 1, 1);
		TextField txtCosto = new TextField();
		grid.add(new Label("Costo:"), 0, 2);
		grid.add(txtCosto, 1, 2);

		txtCosto.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*(\\.\\d*)?")) {
				txtCosto.setText(oldValue);
			}
		});

		txtDistancia.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*(\\.\\d*)?")) {
				txtDistancia.setText(oldValue);
			}
		});

		txtTiempo.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				txtTiempo.setText(oldValue);
			}
		});

		dialog.getDialogPane().setContent(grid);
		ButtonType btnOk = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

		dialog.setResultConverter(button -> {
			if (button == btnOk) {
				try {
					double distancia = Double.parseDouble(txtDistancia.getText());
					int tiempo = Integer.parseInt(txtTiempo.getText());
					double costo = Double.parseDouble(txtCosto.getText());
					return new AristaInfo(distancia, tiempo, costo);
				} catch (NumberFormatException e) {
					mostrarMensaje("Por favor, ingrese valores numéricos válidos.");
					return null;
				}
			}
			return null;
		});

		Optional<AristaInfo> result = dialog.showAndWait();
		result.ifPresent(info -> {
			String idNodo1 = (String) nodo1.getUserData();
			String idNodo2 = (String) nodo2.getUserData();

			Nodo origen = grafo.getListaNodos().stream()
					.filter(n -> n.getId().equals(idNodo1))
					.findFirst()
					.orElse(null);

			Nodo destino = grafo.getListaNodos().stream()
					.filter(n -> n.getId().equals(idNodo2))
					.findFirst()
					.orElse(null);

			if (origen != null && destino != null) {
				Arista nuevaArista = new Arista(origen, destino, info.getDistancia(), info.getTiempo(), info.getCosto());
				grafo.agregarArista(nuevaArista);

				Line ruta = new Line(nodo1.getCenterX(), nodo1.getCenterY(), nodo2.getCenterX(), nodo2.getCenterY());
				ruta.getStyleClass().add("route-line");
				ruta.setOnMouseClicked(e -> seleccionarArista(ruta));
				aristas.add(ruta);
				aristasInfo.add(info);

				Label label = new Label();
				label.setLayoutX((nodo1.getCenterX() + nodo2.getCenterX()) / 2);
				label.setLayoutY((nodo1.getCenterY() + nodo2.getCenterY()) / 2);

				canvas.getChildren().add(ruta);
				canvas.getChildren().add(label);
				aristaLabels.add(label);

				actualizarEtiquetasAristas("Distancia");

				mostrarMensaje("Ruta creada exitosamente entre " + origen.getNombre() + " y " + destino.getNombre());
			} else {
				mostrarMensaje("No se pudieron encontrar los nodos para crear la ruta.");
			}
		});
	}

	private void activarModoModificarNodo() {
		for (Circle nodo : nodos) {
			nodo.setOnMouseClicked(event -> {
				String idNodo = (String) nodo.getUserData();
				Nodo nodoLogico = grafo.getListaNodos().stream()
						.filter(n -> n.getId().equals(idNodo))
						.findFirst()
						.orElse(null);

				if (nodoLogico == null) {
					mostrarMensaje("Nodo no encontrado en el grafo.");
					return;
				}

				TextInputDialog dialog = new TextInputDialog(nodoLogico.getNombre());
				dialog.setTitle("Modificar Nodo");
				dialog.setHeaderText("Edite el nombre del nodo:");
				dialog.setContentText("Nombre:");

				Optional<String> result = dialog.showAndWait();
				result.ifPresent(nombreNuevo -> {
					nodoLogico.setNombre(nombreNuevo);
					actualizarEtiquetas();
				});
			});
		}
	}


	private void activarModoModificarArista() {
		if (selectedArista != null) {
			int index = aristas.indexOf(selectedArista);
			AristaInfo info = aristasInfo.get(index);

			Dialog<AristaInfo> dialog = new Dialog<>();
			dialog.setTitle("Modificar Arista");
			dialog.setHeaderText("Modifique los datos de la arista:");

			// Layout del formulario
			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);

			TextField txtDistancia = new TextField(String.valueOf(info.getDistancia()));
			TextField txtTiempo = new TextField(String.valueOf(info.getTiempo()));
			TextField txtCosto = new TextField(String.valueOf(info.getCosto()));
			grid.add(new Label("Distancia:"), 0, 0);
			grid.add(txtDistancia, 1, 0);
			grid.add(new Label("Tiempo:"), 0, 1);
			grid.add(txtTiempo, 1, 1);
			grid.add(new Label("Costo:"), 0, 2);
			grid.add(txtCosto, 1, 2);


			dialog.getDialogPane().setContent(grid);
			ButtonType btnOk = new ButtonType("Actualizar", ButtonBar.ButtonData.OK_DONE);
			dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

			dialog.setResultConverter(button -> {
				if (button == btnOk) {
					try {
						return new AristaInfo(
								Double.parseDouble(txtDistancia.getText()),
								Integer.parseInt(txtTiempo.getText()),
								Double.parseDouble(txtCosto.getText())
						);
					} catch (NumberFormatException e) {
						mostrarMensaje("Por favor, ingrese valores válidos.");
					}
				}
				return null;
			});

			Optional<AristaInfo> result = dialog.showAndWait();
			result.ifPresent(newInfo -> {
				info.setDistancia(newInfo.getDistancia());
				info.setTiempo(newInfo.getTiempo());
				info.setCosto(newInfo.getCosto());
				actualizarEtiquetasAristas("Distancia");
			});
		}
	}


	private void eliminarNodo() {
		if (selectedNode != null) {
			Label labelToRemove = null;
			for (javafx.scene.Node child : canvas.getChildren()) {
				if (child instanceof Label) {
					double labelX = ((Label) child).getLayoutX();
					double labelY = ((Label) child).getLayoutY();
					if (Math.abs(labelX - (selectedNode.getCenterX() + 12)) < 1 &&
							Math.abs(labelY - (selectedNode.getCenterY() - 5)) < 1) {
						labelToRemove = (Label) child;
						break;
					}
				}
			}

			List<Line> aristasAEliminar = new ArrayList<>();
			List<Label> etiquetasAEliminar = new ArrayList<>();

			for (int i = 0; i < aristas.size(); i++) {
				Line ruta = aristas.get(i);

				if (ruta.getStartX() == selectedNode.getCenterX() && ruta.getStartY() == selectedNode.getCenterY() ||
						ruta.getEndX() == selectedNode.getCenterX() && ruta.getEndY() == selectedNode.getCenterY()) {
					aristasAEliminar.add(ruta);

					etiquetasAEliminar.add(aristaLabels.get(i));
				}
			}

			aristas.removeAll(aristasAEliminar);
			aristasInfo.removeAll(aristasAEliminar);
			aristaLabels.removeAll(etiquetasAEliminar);

			canvas.getChildren().removeAll(aristasAEliminar);
			canvas.getChildren().removeAll(etiquetasAEliminar);
			canvas.getChildren().remove(selectedNode);

			if (labelToRemove != null) {
				canvas.getChildren().remove(labelToRemove);
			}

			// Eliminar el nodo visual de la lista de nodos
			nodos.remove(selectedNode);
			selectedNode = null;
		}
	}

	private void eliminarArista() {
		if (selectedArista != null) {
			int index = aristas.indexOf(selectedArista);

			aristas.remove(selectedArista);
			aristasInfo.remove(index);

			canvas.getChildren().remove(selectedArista);
			canvas.getChildren().remove(aristaLabels.get(index));
			aristaLabels.remove(index);

			Nodo origen = grafo.getListaNodos().stream()
					.filter(nodo -> nodo.getLatitud() == selectedArista.getStartX() &&
							nodo.getLongitud() == selectedArista.getStartY())
					.findFirst()
					.orElse(null);

			Nodo destino = grafo.getListaNodos().stream()
					.filter(nodo -> nodo.getLatitud() == selectedArista.getEndX() &&
							nodo.getLongitud() == selectedArista.getEndY())
					.findFirst()
					.orElse(null);

			if (origen != null && destino != null) {
				grafo.getListaAristas().removeIf(
						arista -> arista.getOrigen().getId().equals(origen.getId()) &&
								arista.getDestino().getId().equals(destino.getId())
				);
			}

			selectedArista = null;
		}
	}

    private void calcularRutaCorta(String criterio, TextArea resultadoRuta) {
        if (nodoInicio == null || nodoFin == null) {
            mostrarMensaje("Debe seleccionar un nodo de inicio y uno de fin.");
            return;
        }

        try {
            String idInicio = (String) nodoInicio.getUserData();
            String idFin = (String) nodoFin.getUserData();

            Nodo inicio = grafo.getListaNodos().stream()
                    .filter(n -> n.getId().equals(idInicio))
                    .findFirst()
                    .orElse(null);

            Nodo fin = grafo.getListaNodos().stream()
                    .filter(n -> n.getId().equals(idFin))
                    .findFirst()
                    .orElse(null);

            if (inicio == null || fin == null) {
                mostrarMensaje("No se encontraron los nodos seleccionados en el grafo.");
                return;
            }

            List<Nodo> ruta = grafo.encontrarAristaMasCorta(inicio.getId(), fin.getId(), criterio, simuladorTrafico);

            if (ruta.isEmpty()) {
                mostrarMensaje("No se encontró una ruta entre los nodos seleccionados.");
                return;
            }

            // Calcular el total según el criterio
            double total = 0;
            for (int i = 0; i < ruta.size() - 1; i++) {
                Nodo origen = ruta.get(i);
                Nodo destino = ruta.get(i + 1);
                Arista arista = grafo.getListaAristas().stream()
                        .filter(a -> a.getOrigen().equals(origen) && a.getDestino().equals(destino))
                        .findFirst()
                        .orElse(null);

                if (arista != null) {
                    switch (criterio) {
                        case "Distancia":
                            total += arista.getDistancia();
                            break;
                        case "Tiempo":
                            total += arista.getTiempo();
                            break;
                        case "Costo":
                            total += arista.getCosto();
                            break;
                    }
                }
            }

            // Mostrar la ruta en el TextArea
            StringBuilder resultado = new StringBuilder("Ruta calculada:\n");
            for (Nodo nodo : ruta) {
                resultado.append(nodo.getNombre()).append(" -> ");
            }
            resultado.delete(resultado.length() - 4, resultado.length()); // Eliminar la última flecha
            resultado.append("\nCriterio: ").append(criterio)
                    .append("\nTotal ").append(criterio.toLowerCase()).append(": ").append(total);

            resultadoRuta.setText(resultado.toString());

            // Resaltar visualmente la ruta
            restablecerColores();
			for (int i = 0; i < ruta.size() - 1; i++) {
				Nodo origen = ruta.get(i);
				Nodo destino = ruta.get(i + 1);

				Line lineaRuta = aristas.stream()
						.filter(line -> conectaNodos(line, origen, destino))
						.findFirst()
						.orElse(null);

				if (lineaRuta != null) {
					lineaRuta.getStyleClass().clear();
					lineaRuta.getStyleClass().add("route-line-selected");
				}
			}

			for (Nodo nodo : ruta) {
				Circle nodoVisual = nodos.stream()
						.filter(circle -> circle.getUserData().equals(nodo.getId()))
						.findFirst()
						.orElse(null);

				if (nodoVisual != null) {
					nodoVisual.getStyleClass().clear();  // Eliminar cualquier estilo anterior
					nodoVisual.getStyleClass().add("node-start");  // Aplicar el estilo de nodo seleccionado
				}
			}

		} catch (Exception e) {
            mostrarMensaje("Error al calcular la ruta: " + e.getMessage());
        }
    }


    private void mostrarMensaje(String s) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Información");
		alert.setHeaderText(null);
		alert.setContentText(s);
		alert.showAndWait();
	}

	private void restablecerColores() {
		for (Circle nodo : nodos) {
			nodo.getStyleClass().clear();
			nodo.getStyleClass().add("node");
		}
		for (Line arista : aristas) {
			arista.getStyleClass().clear();
			arista.getStyleClass().add("route-line");
		}
	}

	// Método auxiliar para verificar si una línea conecta dos nodos
	private boolean conectaNodos(Line linea, Nodo origen, Nodo destino) {
		double x1 = origen.getLatitud();
		double y1 = origen.getLongitud();
		double x2 = destino.getLatitud();
		double y2 = destino.getLongitud();

		double tolerancia = 0.001;

		return (esCercano(linea.getStartX(), x1, tolerancia) &&
				esCercano(linea.getStartY(), y1, tolerancia) &&
				esCercano(linea.getEndX(), x2, tolerancia) &&
				esCercano(linea.getEndY(), y2, tolerancia)) ||
				(esCercano(linea.getStartX(), x2, tolerancia) &&
						esCercano(linea.getStartY(), y2, tolerancia) &&
						esCercano(linea.getEndX(), x1, tolerancia) &&
						esCercano(linea.getEndY(), y1, tolerancia));
	}

	private boolean esCercano(double valor1, double valor2, double tolerancia) {
		return Math.abs(valor1 - valor2) <= tolerancia;
	}


	private void seleccionarNodoInicio(Circle nodoVisual) {
		if (nodoInicio != null) {
			nodoInicio.getStyleClass().remove("node-selected"); // Eliminar estilo de nodo de inicio
			nodoInicio.getStyleClass().add("node"); // Restablecer estilo original
		}
		nodoInicio = nodoVisual;
		nodoVisual.getStyleClass().clear();
		nodoVisual.getStyleClass().add("node-selected"); // Aplicar estilo para nodo de inicio

		String idNodo = (String) nodoVisual.getUserData();
		Nodo nodoEnGrafo = grafo.getListaNodos().stream()
				.filter(n -> n.getId().equals(idNodo))
				.findFirst()
				.orElse(null);

		if (nodoEnGrafo == null) {
			mostrarMensaje("El nodo seleccionado no existe en el grafo.");
			nodoInicio = null;
		} else {
			mostrarMensaje("Nodo de inicio seleccionado: " + nodoEnGrafo.getNombre());
		}
	}


	private void seleccionarNodoFin(Circle nodoVisual) {
		if (nodoFin != null) {
			nodoFin.getStyleClass().remove("node-finish"); // Eliminar clase de nodo de fin
			nodoFin.getStyleClass().add("node"); // Restablecer estilo original
		}
		nodoFin = nodoVisual;
		nodoVisual.getStyleClass().clear();
		nodoVisual.getStyleClass().add("node-finish"); // Aplicar estilo para nodo de fin

		String idNodo = (String) nodoVisual.getUserData();
		Nodo nodoEnGrafo = grafo.getListaNodos().stream()
				.filter(n -> n.getId().equals(idNodo))
				.findFirst()
				.orElse(null);

		if (nodoEnGrafo == null) {
			mostrarMensaje("El nodo seleccionado no existe en el grafo.");
			nodoFin = null;
		} else {
			mostrarMensaje("Nodo de fin seleccionado: " + nodoEnGrafo.getNombre());
		}
	}

	private void seleccionarNodo(Circle nodo) {
		if (selectedNode == null) {
			selectedNode = nodo;
			nodo.getStyleClass().clear();
			nodo.getStyleClass().add("node-selected"); // Aplicar estilo para nodo seleccionado
		} else {
			selectedNode.getStyleClass().clear();
			selectedNode.getStyleClass().add("node"); // Restablecer estilo
			selectedNode = null;
		}
	}

	private void seleccionarArista(Line arista) {
		if (selectedArista == null) {
			selectedArista = arista;
			arista.getStyleClass().clear();
			arista.getStyleClass().add("route-line-selected"); // Aplicar estilo para arista seleccionada
		} else {
			selectedArista.getStyleClass().clear();
			selectedArista.getStyleClass().add("route-line"); // Restablecer estilo
			selectedArista = null;
		}
	}

	private void actualizarEtiquetas() {
		for (int i = 0; i < nodos.size(); i++) {
			Circle nodoVisual = nodos.get(i);
			String idNodo = (String) nodoVisual.getUserData();

			Nodo nodoLogico = grafo.getListaNodos().stream()
					.filter(n -> n.getId().equals(idNodo))
					.findFirst()
					.orElse(null);

			if (nodoLogico != null) {
				// Busca el Label correspondiente al nodo en el canvas
				Label etiqueta = (Label) canvas.getChildren().stream()
						.filter(node -> node instanceof Label)
						.map(node -> (Label) node)
						.filter(label ->
								Math.abs(label.getLayoutX() - (nodoVisual.getCenterX() + 12)) < 1 &&
										Math.abs(label.getLayoutY() - (nodoVisual.getCenterY() - 5)) < 1)
						.findFirst()
						.orElse(null);

				if (etiqueta != null) {
					etiqueta.setText(nodoLogico.getNombre());
				}
			}
		}
	}

	private void actualizarEtiquetasAristas(String datoSeleccionado) {
		for (int i = 0; i < aristas.size(); i++) {
			Line ruta = aristas.get(i);
			Label etiqueta = aristaLabels.get(i);
			AristaInfo info = aristasInfo.get(i);

			String texto;
			switch (datoSeleccionado) {
				case "Distancia":
					texto = "Dist: " + info.getDistancia();
					break;
				case "Tiempo":
					texto = "Tiempo: " + info.getTiempo();
					break;
				case "Costo":
					texto = "Costo: " + info.getCosto();
					break;
				default:
					texto = "";
					break;
			}

			etiqueta.setText(texto);
			etiqueta.setLayoutX((ruta.getStartX() + ruta.getEndX()) / 2);
			etiqueta.setLayoutY((ruta.getStartY() + ruta.getEndY()) / 2);
		}
	}

	private Arista obtenerAristaLogica(Line aristaVisual) {
		return Grafo.getListaAristas().stream()
				.filter(arista -> conectaNodos(aristaVisual, arista.getOrigen(), arista.getDestino()))
				.findFirst()
				.orElse(null);
	}


	private void actualizarColorAristas(String criterio) {
		for (Line aristaVisual : aristas) {
			Arista arista = obtenerAristaLogica(aristaVisual); // Método auxiliar para mapear lógica a visual

			// Limpiar clases anteriores
			aristaVisual.getStyleClass().clear();

			if (criterio.equals("Tiempo")) {
				SimuladorTrafico.NivelTrafico nivel = simuladorTrafico.evaluarTrafico();
				aristaVisual.getStyleClass().add(getClaseCssPorNivelTrafico(nivel)); // Asignar la clase CSS según el nivel de tráfico
			} else {
				aristaVisual.getStyleClass().add("route-line"); // Clase CSS por defecto
			}
		}
	}

	private String getClaseCssPorNivelTrafico(SimuladorTrafico.NivelTrafico nivel) {
		switch (nivel) {
			case BAJO: return "route-line-low";
			case MEDIO: return "route-line-medium";
			case ALTO: return "route-line-high";
			default: return "route-line";
		}
	}


	private void actualizarPantalla() {
		try {
			// Validar si canvas está inicializado
			if (canvas == null) {
				System.err.println("Error: el objeto canvas no está inicializado.");
				return;
			}

			// Reiniciar colecciones
			nodos.clear();
			aristas.clear();
			aristasInfo.clear();
			aristaLabels.clear();

			// Limpiar canvas si está inicializado
			canvas.getChildren().clear();

			// Dibujar nodos
			for (Nodo nodo : grafo.getListaNodos()) {
				if (nodo == null) continue;

				// Crear el nodo visual
				Circle nodoVisual = new Circle(nodo.getLatitud(), nodo.getLongitud(), 10);
				nodoVisual.getStyleClass().add("node");
				nodoVisual.setUserData(nodo.getId());
				nodoVisual.setOnMouseClicked(e -> seleccionarNodo(nodoVisual));
				nodos.add(nodoVisual);
				canvas.getChildren().add(nodoVisual);

				// Etiquetas de nodos
				Label etiquetaNodo = new Label(nodo.getNombre());
				etiquetaNodo.setLayoutX(nodo.getLatitud() + 12);
				etiquetaNodo.setLayoutY(nodo.getLongitud() - 5);
				canvas.getChildren().add(etiquetaNodo);
			}

			// Dibujar aristas
			for (Arista arista : grafo.getListaAristas()) {
				if (arista == null || arista.getOrigen() == null || arista.getDestino() == null) continue;

				Nodo origen = arista.getOrigen();
				Nodo destino = arista.getDestino();

				// Crear la línea visual para la arista
				Line linea = new Line(origen.getLatitud(), origen.getLongitud(),
						destino.getLatitud(), destino.getLongitud());
				linea.getStyleClass().add("route-line");
				linea.setOnMouseClicked(e -> seleccionarArista(linea));
				aristas.add(linea);
				canvas.getChildren().add(linea);

				// Etiquetas de aristas
				Label etiquetaArista = new Label();
				etiquetaArista.setLayoutX((origen.getLatitud() + destino.getLatitud()) / 2);
				etiquetaArista.setLayoutY((origen.getLongitud() + destino.getLongitud()) / 2);
				canvas.getChildren().add(etiquetaArista);
				aristaLabels.add(etiquetaArista);

				aristasInfo.add(new AristaInfo(
						arista.getDistancia(),
						arista.getTiempo(),
						arista.getCosto()
				));
			}
			canvas.getChildren().forEach(elemento -> System.out.println(elemento.toString()));

			// Mostrar etiquetas iniciales
			actualizarEtiquetasAristas("Distancia");
		} catch (NullPointerException e) {
			System.err.println("NullPointerException detectada en actualizarPantalla.");
			e.printStackTrace();
		}
	}

	public void cargarMapaDesdeArchivo(String rutaArchivo) {
		if (canvas == null) {
			archivoPendienteCargar = rutaArchivo;
			return;
		}

		try (Reader reader = new FileReader(rutaArchivo)) {
			Gson gson = new Gson();
			MapaData mapaData = gson.fromJson(reader, MapaData.class);

			grafo = new Grafo();
			mapaData.nodos.forEach(nodoData -> {
				Nodo nodo = new Nodo(nodoData.id, nodoData.nombre, nodoData.latitud, nodoData.longitud);
				grafo.agregarNodo(nodo);
			});

			mapaData.aristas.forEach(aristaData -> {
				Nodo origen = grafo.getListaNodos().stream()
						.filter(n -> n.getId().equals(aristaData.origenId))
						.findFirst()
						.orElse(null);

				Nodo destino = grafo.getListaNodos().stream()
						.filter(n -> n.getId().equals(aristaData.destinoId))
						.findFirst()
						.orElse(null);

				if (origen != null && destino != null) {
					Arista arista = new Arista(origen, destino, aristaData.distancia, aristaData.tiempo, aristaData.costo);
					grafo.agregarArista(arista);
				}
			});

			System.out.println("Nodos cargados:");
			grafo.getListaNodos().forEach(n ->
					System.out.println("ID: " + n.getId() + ", Nombre: " + n.getNombre() +
							", Latitud: " + n.getLatitud() + ", Longitud: " + n.getLongitud()));

			System.out.println("Aristas cargadas:");
			grafo.getListaAristas().forEach(a ->
					System.out.println("Origen: " + a.getOrigen().getId() +
							", Destino: " + a.getDestino().getId() +
							", Distancia: " + a.getDistancia() + ", Tiempo: " + a.getTiempo()));

			actualizarPantalla();

			System.out.println("Elementos en el canvas:");
			canvas.getChildren().forEach(elemento -> System.out.println(elemento.toString()));
		} catch (IOException | JsonSyntaxException e) {
			e.printStackTrace();
			mostrarAlertaError("Error", "Error al cargar el mapa: " + e.getMessage());
		}
	}


	private void mostrarAlertaError(String titulo, String mensaje) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(titulo);
		alert.setHeaderText(null);
		alert.setContentText(mensaje);
		alert.showAndWait();
	}


	public static void main(String[] args) {
		launch(args);
	}

	private static class MapaData {
		private List<NodoData> nodos;
		private List<AristaData> aristas;

		public MapaData(Grafo grafo) {
			this.nodos = grafo.getListaNodos().stream()
					.map(NodoData::new)
					.collect(Collectors.toList());

			this.aristas = grafo.getListaAristas().stream()
					.map(AristaData::new)
					.collect(Collectors.toList());
		}
	}

	private static class NodoData {
		private String id;
		private String nombre;
		private double latitud;
		private double longitud;

		public NodoData(Nodo nodo) {
			this.id = nodo.getId();
			this.nombre = nodo.getNombre();
			this.latitud = nodo.getLatitud();
			this.longitud = nodo.getLongitud();
		}
	}

	private static class AristaData {
		private String origenId;
		private String destinoId;
		private double distancia;
		private int tiempo;
		private double costo;

		public AristaData(Arista arista) {
			this.origenId = arista.getOrigen().getId();
			this.destinoId = arista.getDestino().getId();
			this.distancia = arista.getDistancia();
			this.tiempo = arista.getTiempo();
			this.costo = arista.getCosto();
		}

	}

	// Clase para guardar información de las aristas
	private static class AristaInfo {
		private double distancia;
		private int tiempo;
		private double costo;

		public AristaInfo(double distancia, int tiempo, double costo) {
			this.distancia = distancia;
			this.tiempo = tiempo;
			this.costo = costo;
		}

		public double getCosto() { return costo; }

		public void setCosto(double costo) { this.costo = costo;}

		public double getDistancia() {
			return distancia;
		}

		public void setDistancia(double distancia) {
			this.distancia = distancia;
		}

		public int getTiempo() {
			return tiempo;
		}

		public void setTiempo(int tiempo) {
			this.tiempo = tiempo;
		}
	}
}

