# 🚀 ERP Monolith System v1.0

Sistema de gestión empresarial integral diseñado bajo una **arquitectura monolítica**. Esta primera versión centraliza las operaciones clave de la empresa con una interfaz moderna y un flujo de datos dinámico.



---

## 📋 Funcionalidades del Módulo Pedidos (v1.0)

Esta versión implementa un **CRUD completo y funcional** con lógica de negocio integrada:

* **Dashboard de Control:** Visualización de métricas clave mediante indicadores circulares dinámicos (Entregados, Enviados, En Preparación, En Espera).
* **Gestión de Pedidos:** Creación, edición, consulta y eliminación de pedidos de clientes.
* **Cálculos en Tiempo Real:** Procesamiento automático de totales, aplicación de descuentos y gestión de impuestos (IVA).
* **Paginación y Ordenación:** Tablas de datos optimizadas con filtrado por estado y ordenación dinámica por columnas.
* **Historial Reciente:** Sección de acceso rápido a los últimos movimientos registrados.

---

## 🛠️ Stack Tecnológico

* **Backend:** Java 24 & Spring Boot 4.0.1
* **Persistencia:** Spring Data JPA con Hibernate 7.2.0.Final
* **Base de Datos:** MySQL 9.1
* **Vistas:** Thymeleaf (Motor de plantillas dinámico)
* **Frontend:** Bootstrap 5.3, Bootstrap Icons y Google Fonts (Poppins)
* **Productividad:** Project Lombok

---

## 🏗️ Arquitectura y Organización

El proyecto sigue un patrón de diseño por capas para garantizar un código limpio y fácil de mantener:



* `controller/`: Maneja las peticiones HTTP y la comunicación con las vistas de Thymeleaf.
* `service/`: Contiene la lógica de negocio y cálculos de estadísticas.
* `repository/`: Interfaces JPA para la persistencia de datos.
* `model/`: Entidades del dominio y enumeraciones de estado.
* `resources/static/`: Estilos CSS personalizados para una interfaz "chula" y responsive.

---

## 🎨 Interfaz de Usuario (UX/UI)

* **Diseño Moderno:** Sidebar lateral para navegación rápida entre módulos.
* **Feedback Visual:** Alertas dinámicas de éxito y error tras operaciones CRUD.
* **Estadísticas Gráficas:** Uso de SVGs dinámicos para mostrar el progreso de logística.
* **Responsive:** Adaptado para su uso en diferentes resoluciones de pantalla.

---

## 🚀 Instalación Rápida

1. **Base de Datos:** Crear esquema `erp` en MySQL.
2. **Configuración:** Ajustar credenciales en `src/main/resources/application.properties`.
3. **Lanzamiento:**
   ```bash
   mvn spring-boot:run
