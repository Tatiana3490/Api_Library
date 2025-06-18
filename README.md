### Api_Library - Proyecto Spring Boot ###
Aplicación RESTful para la gestión de una biblioteca. Proyecto realizado con Spring Boot, Spring Data JPA, Spring Security (JWT), y base de datos H2.
# Tecnologías utilizadas
  -Java 21
  -Spring Boot 3.4.3
  -Spring Data JPA (Hibernate)
  -Spring Security con JWT
  -Lombok
  -Base de datos H2
  -Postman (colección de pruebas)
  -Maven

# Requisitos previos
  -JDK 21
  -Maven
  -IDE (IntelliJ recomendado)
  -Postman

# Instalación y ejecución
  1️) Clonar el repositorio
      git clone [REPO_URL]
      cd Api_Library
  2) Compilar y ejecutar : mvn clean spring-boot:run

La aplicación se iniciará en http://localhost:8082.

  3) Base de datos H2
    Consola disponible en: http://localhost:8082/h2-console
    JDBC URL: jdbc:h2:file:~/api_library.db
    Usuario: sa
    Password: password

## Seguridad y autenticación (JWT)
  1️)Registro manual de usuarios: Los usuarios se insertaron previamente en la BBDD H2.
  2) Login
      Endpoint: POST http://localhost:8082/auth/login
      Body (JSON): 
        {
          "username": "isabel123",
          "password": "isabelmola"
        }
      Respuesta: 
      {
        "token": "eyJhbGciOiJIUzI1NiJ9..."
      }

  3) Acceso protegido
      Para consumir cualquier endpoint privado:
         Añadir en Headers: Authorization: Bearer <token_recibido>

## Endpoints principales
    POST	/auth/login	Login y generación de token JWT
    GET	/books	Listado de libros
    GET	/users	Listado de usuarios
    GET	/users/search?name=nombre	Búsqueda de usuarios por nombre (SQL Nativa)
    GET	/loans/search?quantity=5	Búsqueda de préstamos por cantidad (SQL Nativa)

  Nota: Todos los endpoints (excepto /auth/** y /h2-console/**) requieren token.

 ## Consultas SQL Nativas implementadas
    Usuarios activos:
        @Query(value = "SELECT * FROM users WHERE active = true", nativeQuery = true)
        List<User> findActiveUsersNative();
   
    Búsqueda por nombre: 
      @Query(value = "SELECT * FROM users WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))", nativeQuery = true)
      List<User> findUsersByNameContainingNative(@Param("name") String name);

    Préstamos por cantidad:
    @Query(value = "SELECT * FROM loan WHERE quantity >= :quantity", nativeQuery = true)
    List<Loan> findLoansWithQuantityGreaterThan(@Param("quantity") int quantity);

## Colección de Postman
  Incluye:
    -Login JWT
    -Test de todos los endpoints
    -Variables de entorno ({{baseUrl}}) parametrizadas




