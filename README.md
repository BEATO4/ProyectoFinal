# Sistema de Gestión de Rutas

El **Sistema de Gestión de Rutas** es una aplicación Java diseñada para la gestión de ubicaciones y rutas entre ellas. Utiliza diversos algoritmos de grafos para calcular rutas más cortas, encontrar árboles de expansión mínima y calcular caminos más cortos entre todas las ubicaciones.

Rafael Jose Ramirez Jorge 10149810 (DELL)
Jostin de Jesus Beato Caceres 10150326

## 🎯 **Objetivo**
Optimizar la codificación y el manejo eficiente de nodos y rutas para mejorar la planificación de rutas de transporte público.

## 📝 **Funcionalidades**

- **Gestión de Mapas:**
  - Crear un mapa desde cero o cargar uno existente en formato JSON.
  - Guardar mapas en JSON para reutilizarlos.

- **Gestión de Nodos:**
  - Agregar nodos con nombres únicos.
  - Modificar nombres de nodos existentes.
  - Eliminar nodos.

- **Gestión de Aristas (Rutas):**
  - Conectar nodos con aristas que contienen:
    - **Distancia**
    - **Tiempo**
    - **Costo**
  - Modificar o eliminar las aristas según sea necesario.

- **Visualización Dinámica:**
  - Mostrar distancia, tiempo o costo de las aristas usando un `ComboBox`.
  - Actualización visual del mapa tras cada cambio.

- **Cálculo de Rutas:**
  - Selección de nodos inicial y final.
  - Cálculo de la ruta más corta utilizando algoritmos como **Dijkstra**.
  - Visualización de la ruta en el mapa y detalles de la misma en un mensaje emergente.

---
## Uso

Para ejecutar el programa, simplemente sigue estos pasos:

1. Compila el archivo `ProyectoFinal.java` utilizando tu compilador Java favorito.
2. Ejecuta el archivo compilado (`ProyectoFinal.java`) desde tu terminal o IDE.

Al ejecutar el programa, se mostrará un menú interactivo que te guiará a través de las diferentes funcionalidades disponibles.

## ⚙️ **Requisitos del Sistema**

### **Software**
- **Java SE 8** o superior.
- Biblioteca gráfica: **JavaFX**.
- Biblioteca JSON: **Gson**.

## 🛠 **Estructura del Proyecto**

### **Packages y Clases Principales**

#### **`application`**
- **Inicio:** Clase que permite al usuario elegir entre abrir un mapa guardado previamente o crear uno nuevo.
- **Main:** Clase principal que gestiona la interfaz y proporciona botones para:
  - Agregar, modificar y eliminar nodos y aristas.
  - Seleccionar nodos inicial y final para calcular la ruta más corta.

#### **`logico`**
- **Nodo:** Representa una ubicación con un nombre único.
- **Arista:** Modela una conexión entre dos nodos con propiedades de distancia, tiempo y costo.
- **Grafo:** Gestiona la lógica de las relaciones entre nodos y aristas, incluyendo el cálculo de rutas más cortas.
- **SimuladorTrafico:** Coordina la simulación del tráfico, integrando la lógica de nodos, aristas y cálculos.

## 📂 **Formato de Almacenamiento JSON**

```json
{
  "nodos": [
    {
      "id": "15311537-5088-4c34-afa6-15d0ad75274b",
      "nombre": "A",
      "latitud": 369.0,
      "longitud": 311.0
    },
    {
      "id": "532ddc6b-3bc0-4f80-9547-6f57d61ca916",
      "nombre": "B",
      "latitud": 365.0,
      "longitud": 113.0
    }
  ]
  "aristas": [
    {
      "origenId": "15311537-5088-4c34-afa6-15d0ad75274b",
      "destinoId": "532ddc6b-3bc0-4f80-9547-6f57d61ca916",
      "distancia": 40.0,
      "tiempo": 50
    }
  ]
}
