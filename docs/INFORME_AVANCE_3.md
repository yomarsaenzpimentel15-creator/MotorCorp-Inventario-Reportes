# AVANCE DE PROYECTO FINAL 3
## MotorCorp: Sistema de inventario y reportes para taller mecánico

**Curso:** Desarrollo de soluciones informáticas con Java  
**Semana:** 12  
**Autor(es):** [Completar]  
**Docente:** [Completar]  
**Fecha:** Octubre de 2026

### 1. Introducción
MotorCorp requiere controlar repuestos y materiales utilizados en reparaciones, consultar existencias y registrar entradas y salidas de almacén. Este avance implementa un sistema de escritorio en Java con persistencia MySQL para mejorar la trazabilidad del inventario.

### 2. Objetivos
**General:** desarrollar el módulo inicial de inventario y reportes de MotorCorp mediante arquitectura MVC, patrón DAO, principios SOLID y pruebas TDD.

**Específicos:** registrar repuestos, validar movimientos, mostrar alertas de stock, generar reportes y documentar el código para control de versiones.

### 3. Requisitos funcionales implementados
| ID | Requisito | Estado |
|---|---|---|
| RF01 | Registrar repuestos | Implementado |
| RF02 | Editar datos del repuesto | Implementado |
| RF03 | Listar existencias | Implementado |
| RF04 | Registrar entradas | Implementado |
| RF05 | Registrar salidas | Implementado |
| RF06 | Evitar salida sin stock | Implementado |
| RF07 | Alertas por stock mínimo | Implementado |
| RF08 | Reporte valorizado e histórico de movimientos | Implementado |
| RF09 | Exportar a Excel | Implementado |

El porcentaje logrado deberá calcularse con respecto al **catálogo completo de funcionalidades del proyecto final**, todavía no proporcionado.

### 4. Arquitectura MVC, DAO y SOLID
- **Modelo:** `Repuesto`, `Movimiento`.
- **Vista:** `Ventana` (interfaz Swing con pestañas).
- **Controlador:** `InventarioController`.
- **Servicio:** `InventarioService` (reglas del negocio y validación).
- **DAO:** interfaz `InventarioDao`, implementación `MySqlInventarioDao`.
- **SOLID:** responsabilidad separada entre capas (SRP); el servicio depende de una abstracción (`InventarioDao`) en vez de depender de JDBC (DIP); el DAO puede sustituirse para pruebas (OCP/DIP). No se afirma cobertura total de todos los principios SOLID.

### 5. Base de datos
MySQL 8: tabla `repuestos` con código único, precio y stock; tabla `movimientos` relacionada por FK, con fecha, tipo, cantidad y motivo. Script: `sql/motorcorp.sql`.

### 6. Librerías usadas
- **Apache Commons Lang:** validación de campos con `StringUtils.isBlank`.
- **Apache POI:** exportación de inventario y movimientos a XLSX.
- **Logback:** configuración de logs de aplicación.
- **JUnit 5:** pruebas unitarias del negocio.
- **MySQL Connector/J:** integración JDBC con MySQL.

### 7. Seguridad y calidad
Instrucciones SQL parametrizadas (`PreparedStatement`), validación de campos, operaciones atómicas en transacciones JDBC, controles para evitar stock negativo y credenciales mediante variables de entorno. Pendiente de fases posteriores: autenticación y autorización del personal.

### 8. Evidencia de TDD
Las pruebas de `InventarioServiceTest` definen: salida de 3 unidades desde 10, rechazo de salida con stock insuficiente, rechazo de cantidad negativa, rechazo de repuesto inválido y entrada positiva. Para mostrar la aplicación real del ciclo RED-GREEN-REFACTOR, adjuntar capturas del historial de commits o ejecuciones de pruebas durante el proceso; los archivos finales de pruebas por sí solos no demuestran el orden temporal.

### 9. Git y GitHub
Se incluye `.gitignore`, guía de comandos y flujo de integración continua `.github/workflows/java-ci.yml`. Adjuntar enlace del repositorio y capturas de commits una vez publicados. **La participación en el taller de versionamiento requiere constancia propia.**

### 10. Resultados y capturas solicitadas
Añadir capturas reales de las tres pestañas, listado de MySQL Workbench, resultado de `mvn test`, exportación Excel y registro del repositorio remoto. No inventar evidencias.

### 11. Conclusiones
Se implementaron módulos de inventario, movimientos y reportes sobre una arquitectura en capas. La consistencia de las existencias se protege mediante validaciones y transacciones SQL. El proyecto queda preparado para iteraciones de autenticación, compras, proveedores y órdenes de trabajo.

### 12. Referencias técnicas
- Oracle. Java Platform, Standard Edition Documentation: https://docs.oracle.com/en/java/javase/17/docs/api/
- JUnit Team. JUnit 5 User Guide: https://junit.org/junit5/docs/current/user-guide/
- MySQL. Connector/J Developer Guide: https://dev.mysql.com/doc/connector-j/en/
- Apache POI: https://poi.apache.org/
- Apache Commons Lang: https://commons.apache.org/proper/commons-lang/
- Git Documentation: https://git-scm.com/doc
