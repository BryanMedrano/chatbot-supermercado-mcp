# ChatBot de Pedidos de Supermercado a Domicilio (`chatbot-supermercado-mcp`)

Asistente virtual conversacional desarrollado en **Java 21** con **Google Agent Development Kit (`google-adk 0.6.0`)** e impulsado por **Gemini (`gemini-3-flash-preview`)**. Se conecta como cliente MCP vía **Streamable HTTP** al servidor [`supermercado-mcp`](https://github.com/BryanMedrano/supermercado-mcp) (`http://localhost:8088/mcp`) para gestionar compras de supermercado a domicilio en lenguaje natural desde una interfaz web interactiva.

---

## Arquitectura y Flujo Conversacional (`SupermercadoAgent`)

- **Host MCP (`AdkWebServer`):** Servidor web embebido en el puerto `8080` que administra las sesiones de usuario, la interfaz conversacional y el panel de trazabilidad (`Events`, `Trace` y `State`).
- **Cliente MCP (`McpToolset`):** Conecta mediante `StreamableHttpServerParameters` a `http://localhost:8088/mcp`, realiza el handshake JSON-RPC 2.0 y descubre dinámicamente las **7 herramientas** expuestas por el servidor `supermercado-mcp`.
- **Agente Inteligente (`SupermercadoAgent`):** Configurado con `LlmAgent` y `gemini-3-flash-preview`, sigue un flujo guiado para la atención al cliente:
  1. **Consulta en Tiempo Real:** Verifica catálogo, categorías, precios y existencias reales en MySQL antes de confirmar disponibilidad.
  2. **Armado de Carrito:** Calcula subtotales y presupuesto estimado según las unidades solicitadas por el comprador.
  3. **Confirmación de Datos de Entrega:** Solicita y valida nombre completo, dirección exacta y teléfono antes de invocar `crearPedido`.
  4. **Gestión Post-Venta:** Permite consultar el estado de órdenes existentes y cancelar pedidos reintegrando automáticamente el stock.

---

## Herramientas MCP Consumidas (`supermercado-mcp`)

El agente descubre y orquesta dinámicamente las siguientes herramientas del backend:

- `listAllProductosDisponible` — Consulta todos los productos con existencias disponibles (`stock > 0`).
- `findProductos` — Busca productos por nombre o palabra clave.
- `filterProductos` — Filtra productos por categoría (`Despensa`, `Lácteos`, `Carnes`, `Frutas y Verduras`, `Panadería`, `Aseo Hogar`).
- `crearPedido` — Registra un pedido a domicilio, calcula totales y descuenta inventario en MySQL.
- `findDetallesProducto` — Consulta los detalles, productos y estado de un pedido por su ID.
- `listPedidoCliente` — Lista el historial de pedidos de un cliente por su nombre.
- `cancelarPedido` — Cancela un pedido existente y devuelve las unidades al stock.

---

## Requisitos Previos

- **Java JDK 21** (con la variable de entorno `JAVA_HOME` configurada y agregada al `PATH`).
- **Servidor MCP [`supermercado-mcp`](https://github.com/BryanMedrano/supermercado-mcp) ejecutándose** previamente en `http://localhost:8088/mcp` con MySQL y el catálogo inicial cargados.
- **Clave API de Google AI Studio** con acceso habilitado al modelo `gemini-3-flash-preview`.
- **Variable de entorno `GOOGLE_API_KEY`** configurada en el sistema o sesión de terminal con la clave de Google AI Studio.
- **Puerto `8080` libre** en el equipo local (`localhost:8080`).
- **Acceso a Internet** para comunicarse con la API de Google Gemini y descargar las dependencias Maven (`google-adk`, `google-adk-dev`, `httpclient5`, `rxjava`) en la primera compilación.
- **Maven Wrapper** incluido en el repositorio (`.\mvnw.cmd` en Windows o `./mvnw` en Linux/macOS).

---

## Configuración y Ejecución

1. **Clonar el repositorio:**
   ```powershell
   git clone https://github.com/BryanMedrano/chatbot-supermercado-mcp.git
   cd chatbot-supermercado-mcp
   ```

2. **Configurar la variable de entorno con la API Key de Gemini:**
   ```powershell
   $env:GOOGLE_API_KEY = "TU_API_KEY_DE_GOOGLE_AI_STUDIO"
   ```

3. **Compilar e instalar el proyecto en `target/`:**
   ```powershell
   .\mvnw.cmd clean install
   ```
   *(Este paso es obligatorio antes de arrancar `AdkWebServer`, ya que el servidor carga los agentes compilados desde `--adk.agents.source-dir=target`).*

4. **Levantar la interfaz web de Google ADK:**
   ```powershell
   .\mvnw.cmd exec:java@runAdkWebServer
   ```
   El servidor web iniciará en `http://localhost:8080`.

5. **Probar el ChatBot en el navegador:**
   - Abre `http://localhost:8080`.
   - Selecciona **`SupermercadoAgent`** en el menú superior izquierdo.
   - Asegúrate de mantener el interruptor **Token Streaming** **apagado** para ejecutar correctamente las herramientas MCP síncronas.
   - Ejemplo de flujo de prueba:
     1. *"Hola, muéstrame los productos disponibles en el supermercado con sus precios y stock."*
     2. *"Quiero pedir 2 bolsas de Arroz Blanco Premium, 1 panal de Huevos Rojos y 1 caja de Leche Entera Deslactosada. Mi nombre es juanito perez, dirección Calle 67 # 53-108 y teléfono 3001234567."*
     3. *"Confirmo el pedido."*

---
