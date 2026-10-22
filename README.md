# MotorCorp - Sistema de inventario y reportes (Java + MySQL)

Proyecto académico para la consigna **Avance de Proyecto Final 3**, semana 12.

## Requisitos
- JDK 17 o superior
- Maven 3.9+
- MySQL 8+
- IntelliJ IDEA, NetBeans (abrir como proyecto Maven) o VS Code Java

## Instalación y ejecución
1. Inicia el servicio MySQL.
2. Ejecuta en MySQL Workbench el archivo `sql/motorcorp.sql` completo.
3. Configura las variables de entorno `MOTORCORP_DB_URL`, `MOTORCORP_DB_USER` y `MOTORCORP_DB_PASSWORD` antes de lanzar Java. Alternativamente, se usa `root` con contraseña vacía **solo para pruebas locales**.
4. En una terminal abierta en la carpeta del proyecto, ejecuta:

```bash
mvn clean test
mvn exec:java
```

En Windows PowerShell (ejemplo, **cambia la contraseña**):
```powershell
$env:MOTORCORP_DB_USER="root"
$env:MOTORCORP_DB_PASSWORD="TU_CLAVE_MYSQL"
mvn clean test
mvn exec:java
```

## Módulos implementados
- **Inventario**: alta y edición de repuestos, stock inicial, alertas por stock mínimo.
- **Movimientos**: ingresos y salidas con cantidad y motivo. Operación transaccional para no dejar el stock desactualizado; rechaza salidas sin existencias.
- **Reportes**: inventario valorizado, productos por reabastecer, historial de movimientos y exportación Excel.
- **MySQL**: tablas `repuestos` y `movimientos`, claves, constraints e índices únicos.
- **Pruebas TDD**: 5 pruebas JUnit 5 aisladas mediante DAO falso.

## Arquitectura
`view` (Swing) → `controller` → `service` (validaciones) → `dao` (contrato) → `dao/mysql` (JDBC/MySQL); `model` (records), `util` (conexión).

## Seguridad
Consultas parametrizadas, reglas de negocio en servicio, transacciones y rollback, no guardar contraseñas en Git, integridad referencial. Para producción: usar usuario MySQL de permisos mínimos, TLS y autenticación de usuarios.

## Git / GitHub
```bash
git init
git add .
git commit -m "feat: estructura MVC y conexión MySQL"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/motorcorp-inventario.git
git push -u origin main
```
Los commits y repositorio **no están creados automáticamente**: se deben publicar desde tu cuenta. La carpeta `.github/workflows` ejecutará `mvn test` al subir commits.

## Alcance académico
Se entrega una base funcional demostrable para inventario y reportes. El porcentaje 30%-40% depende del listado total aprobado de requisitos; no se puede acreditar ese porcentaje sin dicha lista. Falta implementar autenticación, proveedores, compras, órdenes de trabajo y reportes avanzados si forman parte del alcance final. No se ha verificado conexión a tu instancia MySQL.
