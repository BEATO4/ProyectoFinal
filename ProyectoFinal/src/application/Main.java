package application;
import com.google.gson.JsonSyntaxException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
		cbxDatosArista.getItems().addAll("Distancia", "Tiempo");
		cbxDatosArista.setValue("Distancia");

		// Área de dibujo
		canvas = new Pane();
		canvas.setStyle("-fx-background-color: #f0f0f0;");

		// Panel lateral con botones
		VBox controlPanel = new VBox(10, btnAgregarNodo, btnAgregarRuta, btnModificarNodo, btnModificarArista,
				btnEliminarNodo, btnEliminarArista, btnSeleccionarNodos, btnCalcularRutaCorta, cbxDatosArista,btnAtras, btnGuardar);
		controlPanel.setStyle("-fx-padding: 10; -fx-background-color: #dcdcdc;");
		controlPanel.setPrefWidth(200);


		// Layout principal
		BorderPane root = new BorderPane();
		root.setLeft(controlPanel);  // Colocamos los controles a la izquierda
		root.setCenter(canvas);       // El área de dibujo en el centro

		// Eventos
		btnAgregarNodo.setOnAction(event -> activarModoAgregarNodo());
		btnAgregarRuta.setOnAction(event -> activarModoAgregarRuta());
		btnModificarNodo.setOnAction(event -> activarModoModificarNodo());
		btnModificarArista.setOnAction(event -> activarModoModificarArista());
		btnEliminarNodo.setOnAction(event -> eliminarNodo());
		btnEliminarArista.setOnAction(event -> eliminarArista());
		// Botón para seleccionar nodos
		btnSeleccionarNodos.setOnAction(event -> {
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

// Botón para calcular ruta
		btnCalcularRutaCorta.setOnAction(event -> {
			if (nodoInicio == null || nodoFin == null) {
				mostrarMensaje("Primero selecciona los nodos de inicio y fin usando el botón 'Seleccionar Nodos'.");
				return;
			}

			String criterio = cbxDatosArista.getValue();
			calcularRutaCorta(criterio);
		});

		btnAtras.setOnAction(event -> {
			// Return to the start screen
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
					// Preparar los datos para guardar
					MapaData mapaData = new MapaData(grafo);

					// Usar Gson para convertir a JSON
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					String jsonData = gson.toJson(mapaData);

					// Escribir el archivo
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
			actualizarEtiquetasAristas(selectedCriterion); // Updates labels
			actualizarColorAristas(selectedCriterion);    // Updates colors
			mostrarMensaje("Colores de las aristas actualizados según el criterio: " + selectedCriterion);
		});


		System.out.println("Nodos en el grafo:");
		for (Nodo nodo : grafo.getListaNodos()) {
			System.out.println("- Nodo: " + nodo.getNombre() + " (ID: " + nodo.getId() + ")");
		}

		System.out.println("Aristas en el grafo:");
		for (Arista arista : grafo.getListaAristas()) {
			System.out.println("- Arista de " + arista.getOrigen().getNombre() +
					" a " + arista.getDestino().getNombre() +
					" (Distancia: " + arista.getDistancia() + ", Tiempo: " + arista.getTiempo() + ")");
		}

		if (archivoPendienteCargar != null) {
			cargarMapaDesdeArchivo(archivoPendienteCargar);
			archivoPendienteCargar = null; // Limpia el archivo pendiente después de cargar
		}

		primaryStage.setScene(new Scene(root, 1000, 600));
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
				Circle nodoVisual = new Circle(x, y, 10, Color.BLUE);
				nodoVisual.setUserData(nuevoNodo.getId()); // Almacena el ID del nodo
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
		canvas.setOnMouseClicked(null); // Desactiva eventos de agregar nodos
		selectedNode = null;

		for (Circle nodo : nodos) {
			nodo.setOnMouseClicked(event -> {
				if (selectedNode == null) {
					selectedNode = nodo;
					nodo.setFill(Color.GREEN); // Marca el nodo seleccionado
				} else {
					Circle selectedNode2 = nodo;
					nodo.setFill(Color.GREEN);

					if (!selectedNode.equals(selectedNode2)) {
						crearRuta(selectedNode, selectedNode2);
					}

					// Restablece colores
					selectedNode.setFill(Color.BLUE);
					selectedNode2.setFill(Color.BLUE);
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

		// Validación de entrada
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

		// Convertir resultado del diálogo
		dialog.setResultConverter(button -> {
			if (button == btnOk) {
				try {
					double distancia = Double.parseDouble(txtDistancia.getText());
					int tiempo = Integer.parseInt(txtTiempo.getText());
					return new AristaInfo(distancia, tiempo);
				} catch (NumberFormatException e) {
					mostrarMensaje("Por favor, ingrese valores numéricos válidos.");
					return null;
				}
			}
			return null;
		});

		Optional<AristaInfo> result = dialog.showAndWait();
		result.ifPresent(info -> {
			// Encontrar los nodos lógicos correspondientes
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
				// Crear y añadir la arista al grafo
				Arista nuevaArista = new Arista(origen, destino, info.getDistancia(), info.getTiempo(), 0);
				grafo.agregarArista(nuevaArista);

				// Crear la representación visual de la ruta
				Line ruta = new Line(nodo1.getCenterX(), nodo1.getCenterY(),
						nodo2.getCenterX(), nodo2.getCenterY());
				ruta.setStrokeWidth(2);
				ruta.setStroke(Color.GRAY);
				ruta.setOnMouseClicked(e -> seleccionarArista(ruta));  // Hacer clic en la arista
				aristas.add(ruta);
				aristasInfo.add(info);

				// Crear etiqueta para la ruta
				Label label = new Label();
				label.setLayoutX((nodo1.getCenterX() + nodo2.getCenterX()) / 2);
				label.setLayoutY((nodo1.getCenterY() + nodo2.getCenterY()) / 2);

				canvas.getChildren().add(ruta);
				canvas.getChildren().add(label);
				aristaLabels.add(label);

				// Mostrar el dato por defecto
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
					actualizarEtiquetas(); // Actualiza etiquetas con el nuevo nombre
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
			grid.add(new Label("Distancia:"), 0, 0);
			grid.add(txtDistancia, 1, 0);
			grid.add(new Label("Tiempo:"), 0, 1);
			grid.add(txtTiempo, 1, 1);

			dialog.getDialogPane().setContent(grid);
			ButtonType btnOk = new ButtonType("Actualizar", ButtonBar.ButtonData.OK_DONE);
			dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

			dialog.setResultConverter(button -> {
				if (button == btnOk) {
					try {
						return new AristaInfo(Double.parseDouble(txtDistancia.getText()), Integer.parseInt(txtTiempo.getText()));
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
				actualizarEtiquetasAristas("Distancia");
			});
		}
	}


	private void eliminarNodo() {
		if (selectedNode != null) {
			// Find and remove the corresponding label
			Label labelToRemove = null;
			for (javafx.scene.Node child : canvas.getChildren()) {
				if (child instanceof Label) {
					double labelX = ((Label) child).getLayoutX();
					double labelY = ((Label) child).getLayoutY();
					if (labelX == selectedNode.getCenterX() + 12 && labelY == selectedNode.getCenterY() - 5) {
						labelToRemove = (Label) child;
						break;
					}
				}
			}

			// Remove nodo and its associated routes
			nodos.remove(selectedNode);
			List<Line> aristasAEliminar = new ArrayList<>();
			for (Line ruta : aristas) {
				if (ruta.getStartX() == selectedNode.getCenterX() && ruta.getStartY() == selectedNode.getCenterY() ||
						ruta.getEndX() == selectedNode.getCenterX() && ruta.getEndY() == selectedNode.getCenterY()) {
					aristasAEliminar.add(ruta);
				}
			}
			aristas.removeAll(aristasAEliminar);
			aristasInfo.removeAll(aristasAEliminar);
			aristaLabels.removeAll(aristasAEliminar);

			canvas.getChildren().removeAll(aristasAEliminar);
			canvas.getChildren().remove(selectedNode);

			// Remove the label if found
			if (labelToRemove != null) {
				canvas.getChildren().remove(labelToRemove);
			}

			selectedNode = null;
		}
	}

	private void eliminarArista() {
		if (selectedArista != null) {
			// Find the index of the selected arista
			int index = aristas.indexOf(selectedArista);

			// Remove from visual collections
			aristas.remove(selectedArista);
			aristasInfo.remove(index);

			// Remove the corresponding line and label from canvas
			canvas.getChildren().remove(selectedArista);
			canvas.getChildren().remove(aristaLabels.get(index));
			aristaLabels.remove(index);

			// Find and remove the corresponding logical edge
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

	// Variables para selección
	private Circle nodoInicio = null;
	private Circle nodoFin = null;



	private void calcularRutaCorta(String criterio) {
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

			restablecerColores();

			for (int i = 0; i < ruta.size() - 1; i++) {
				Nodo origen = ruta.get(i);
				Nodo destino = ruta.get(i + 1);

				Line lineaRuta = aristas.stream()
						.filter(line -> conectaNodos(line, origen, destino))
						.findFirst()
						.orElse(null);

				if (lineaRuta != null) {
					lineaRuta.setStroke(Color.RED);
				}
			}

			for (Nodo nodo : ruta) {
				Circle nodoVisual = nodos.stream()
						.filter(circle -> circle.getUserData().equals(nodo.getId()))
						.findFirst()
						.orElse(null);

				if (nodoVisual != null) {
					nodoVisual.setFill(Color.ORANGE);
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

	// Método auxiliar para restablecer colores
	private void restablecerColores() {
		// Restablece colores originales de nodos
		for (Circle nodo : nodos) {
			nodo.setFill(Color.BLUE);
		}
		// Restablece colores originales de líneas
		for (Line arista : aristas) {
			arista.setStroke(Color.GRAY);
		}
	}

	// Método auxiliar para verificar si una línea conecta dos nodos
	private boolean conectaNodos(Line linea, Nodo origen, Nodo destino) {
		double x1 = origen.getLatitud(); // Latitud como X
		double y1 = origen.getLongitud(); // Longitud como Y
		double x2 = destino.getLatitud();
		double y2 = destino.getLongitud();

		double tolerancia = 0.001; // Define una tolerancia razonable

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
			nodoInicio.setFill(Color.BLUE); // Restablece el color del nodo anterior
		}
		nodoInicio = nodoVisual;
		nodoVisual.setFill(Color.GREEN); // Marca como nodo de inicio

		// Verificar que el nodo existe en el grafo
		String idNodo = (String) nodoVisual.getUserData();
		Nodo nodoEnGrafo = grafo.getListaNodos().stream()
				.filter(n -> n.getId().equals(idNodo))
				.findFirst()
				.orElse(null);

		if (nodoEnGrafo == null) {
			mostrarMensaje("El nodo seleccionado no existe en el grafo.");
			nodoInicio = null; // Reiniciar la selección
		} else {
			mostrarMensaje("Nodo de inicio seleccionado: " + nodoEnGrafo.getNombre());
		}
	}

	private void seleccionarNodoFin(Circle nodoVisual) {
		if (nodoFin != null) {
			nodoFin.setFill(Color.BLUE); // Restablece el color del nodo anterior
		}
		nodoFin = nodoVisual;
		nodoVisual.setFill(Color.YELLOW); // Marca como nodo de fin

		// Verificar que el nodo existe en el grafo
		String idNodo = (String) nodoVisual.getUserData();
		Nodo nodoEnGrafo = grafo.getListaNodos().stream()
				.filter(n -> n.getId().equals(idNodo))
				.findFirst()
				.orElse(null);

		if (nodoEnGrafo == null) {
			mostrarMensaje("El nodo seleccionado no existe en el grafo.");
			nodoFin = null; // Reiniciar la selección
		} else {
			mostrarMensaje("Nodo de fin seleccionado: " + nodoEnGrafo.getNombre());
		}
	}



	private void seleccionarNodo(Circle nodo) {
		if (selectedNode == null) {
			selectedNode = nodo;
			nodo.setFill(Color.GREEN);
		} else {
			selectedNode.setFill(Color.BLUE);
			selectedNode = null;
		}
	}

	private void seleccionarArista(Line arista) {
		if (selectedArista == null) {
			selectedArista = arista;
			arista.setStroke(Color.RED);
		} else {
			selectedArista.setStroke(Color.GRAY);
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
				Label etiqueta = (Label) canvas.getChildren().get(nodos.size() + i);
				etiqueta.setText(nodoLogico.getNombre());
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
		// Busca la arista lógica que coincida con la posición de los extremos de la arista visual
		return Grafo.getListaAristas().stream()
				.filter(arista -> conectaNodos(aristaVisual, arista.getOrigen(), arista.getDestino()))
				.findFirst()
				.orElse(null); // Devuelve null si no encuentra ninguna coincidencia
	}


	private void actualizarColorAristas(String criterio) {
		for (Line aristaVisual : aristas) {
			Arista arista = obtenerAristaLogica(aristaVisual); // Método auxiliar para mapear lógica a visual
			if (criterio.equals("Tiempo")) {
				SimuladorTrafico.NivelTrafico nivel = simuladorTrafico.evaluarTrafico(arista);
				aristaVisual.setStroke(getColorPorNivelTrafico(nivel));
			} else {
				aristaVisual.setStroke(Color.GRAY);
			}
		}
	}

	private Color getColorPorNivelTrafico(SimuladorTrafico.NivelTrafico nivel) {
		switch (nivel) {
			case BAJO: return Color.GREEN;
			case MEDIO: return Color.YELLOW;
			case ALTO: return Color.RED;
			default: return Color.GRAY;
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
				if (nodo == null) continue; // Validar nodo no nulo

				Circle nodoVisual = new Circle(nodo.getLatitud(), nodo.getLongitud(), 10, Color.BLUE);
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

				Line linea = new Line(origen.getLatitud(), origen.getLongitud(),
						destino.getLatitud(), destino.getLongitud());
				linea.setStrokeWidth(2);
				linea.setStroke(Color.GRAY);
				linea.setOnMouseClicked(e -> seleccionarArista(linea));
				aristas.add(linea);
				canvas.getChildren().add(linea);

				// Etiquetas de aristas
				Label etiquetaArista = new Label();
				etiquetaArista.setLayoutX((origen.getLatitud() + destino.getLatitud()) / 2);
				etiquetaArista.setLayoutY((origen.getLongitud() + destino.getLongitud()) / 2);
				canvas.getChildren().add(etiquetaArista);
				aristaLabels.add(etiquetaArista);

				aristasInfo.add(new AristaInfo(arista.getDistancia(), arista.getTiempo()));
			}

			// Mostrar etiquetas iniciales
			actualizarEtiquetasAristas("Distancia");
		} catch (NullPointerException e) {
			System.err.println("NullPointerException detectada en actualizarPantalla.");
			e.printStackTrace();
		}
	}



	public void cargarMapaDesdeArchivo(String rutaArchivo) {
		if (canvas == null) {
			// Si el canvas aún no está inicializado, almacena el archivo para cargarlo después
			archivoPendienteCargar = rutaArchivo;
			return;
		}

		try (Reader reader = new FileReader(rutaArchivo)) {
			// Leer los datos del archivo
			Gson gson = new Gson();
			MapaData mapaData = gson.fromJson(reader, MapaData.class);

			// Actualizar el grafo lógico
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
					Arista arista = new Arista(origen, destino, aristaData.distancia, aristaData.tiempo, 0);
					grafo.agregarArista(arista);
				}
			});

			// Actualizar la representación visual
			actualizarPantalla();

			System.out.println("Mapa cargado exitosamente desde " + rutaArchivo);
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

		public AristaData(Arista arista) {
			this.origenId = arista.getOrigen().getId();
			this.destinoId = arista.getDestino().getId();
			this.distancia = arista.getDistancia();
			this.tiempo = arista.getTiempo();
		}
	}

	// Clase para guardar información de las aristas
	private static class AristaInfo {
		private double distancia;
		private int tiempo;

		public AristaInfo(double distancia, int tiempo) {
			this.distancia = distancia;
			this.tiempo = tiempo;
		}

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

