
# HealthCheck API

## Prerequisites
Before running the application, ensure you have the following installed:
1. **Java Development Kit (JDK)**:
    - Version 21 required.
2. **Apache Maven** :
    - Maven is used for building the project and resolving dependencies.
    - You can download and install Maven from [Maven's Official Site](https://maven.apache.org/).
3. **MySQL**
4. **Postman** (or any API testing tool)
5. An **IDE** of your choice (e.g., IntelliJ, VS Code, Eclipse)

## Getting Started

1. **Clone the repository**:
  
   git clone <repository_url>
   cd <project_directory>

2. **install all dependincies**:
      mvn clean install
3. **Add Run Configiration**:
   under enviourment variable add following
    1. DB_URL=<db url>;
    2. DB_USERNAME=<db username>;
    3. DB_PASSWORD=<db password>; 

4. Start the MySQL server and ensure it is running.

5. Run the application and access the API at:
   
   http://localhost:5000/healthz
  
   This endpoint supports only the `GET` method.
 
## API Behavior

- A successful `GET /healthz` request returns **200 OK**.
- Any method other than `GET` returns **405 Method Not Allowed**.
- If the database is down, the API returns **503 Service Unavailable**.
- If query parameters or request body parameters are passed, the API returns **400 Bad Request**.

## Testing with Postman

1. Open Postman and make a `GET` request to:

   http://localhost:5000/healthz
   
2. Check the response based on the API behavior described above.


## Troubleshooting

- If there is a database connection error, ensure MySQL is running.
- If you get a "405 Method Not Allowed" response, make sure you are using the `GET` method.
- If you get a "400 Bad Request" response, remove any query or body parameters.

## Added Packer troubleshooting process

