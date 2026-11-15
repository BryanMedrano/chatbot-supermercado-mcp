package com.udea.supermercado_chatbot;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.mcp.McpToolset;
import com.google.adk.tools.mcp.StreamableHttpServerParameters;

import java.time.Duration;
import java.util.List;

public class SupermercadoAgent {

    public static BaseAgent ROOT_AGENT = buildAgent();

    private static BaseAgent buildAgent() {
        StreamableHttpServerParameters params =
                StreamableHttpServerParameters.builder()
                        .url("http://localhost:8088/mcp")
                        .timeout(Duration.ofSeconds(30))
                        .build();

        McpToolset mcpToolset = new McpToolset(params);

        try {
            List<BaseTool> mcpTools = mcpToolset.getTools(null)
                    .toList()
                    .blockingGet();

            System.out.println("Herramientas MCP del Supermercado descubiertas: " + mcpTools.size());

            return LlmAgent.builder()
                    .name("SupermercadoAgent")
                    .model("gemini-3-flash-preview")
                    .description("Asistente ChatBot de pedidos a domicilio para Supermercado UdeA.")
                    .instruction("""
                            Eres el asistente virtual de pedidos a domicilio del Supermercado.
                            1. Consulta siempre el catálogo real y el stock disponible usando las herramientas MCP ('listAllProductosDisponible', 'findProductos', 'filterProductos') antes de dar precios o confirmar disponibilidad.
                            2. Ayuda al cliente a armar su carrito calculando subtotales y el total estimado.
                            3. Antes de invocar 'crearPedido', solicita y confirma con el cliente: nombre completo, dirección exacta de entrega, teléfono de contacto y el resumen de productos con cantidades.
                            4. Cuando el cliente confirme, invoca 'crearPedido' pasando los ítems en formato 'productoId:cantidad,productoId:cantidad' y entrega el número de pedido, los productos incluidos y el total cobrado.
                            5. Si el cliente desea cancelar un pedido, solicita confirmación explícita del ID antes de ejecutar 'cancelarPedido'.
                            """)
                    .tools(mcpTools)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "No fue posible conectar con el servidor MCP del supermercado en http://localhost:8088/mcp", e);
        }
    }
}