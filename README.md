# Indoor Climate Monitor API

This is an API (using Spring Boot) that stores and fetches measurements from the sets of sensors. Measurements that can be gathered 
are CO2 levels, temperature, brightness and dust density.

## How to run the API on Ubuntu 18.04

Update system and install Maven and Java 8:
```bash
sudo apt update
sudo apt install maven -y
sudo apt install openjdk-8-jre -y
sudo apt install mysql-server -y
```

Configure the MySQL database:
```bash
sudo mysql_secure_installation
```

Import the database inside `src/main/resources/extras/icm_api.sql` to the database.

Clone the project:
```bash
git clone https://github.com/Majestic-Diamond-Ducks/icm-api.git
```

Copy `src/main/resources/application.properties.template`  to `src/main/resources/application.properties` and set
appropriate values for database.

Run the API (while in the root directory of the project):
```bash
mvn spring-boot:start
```

Done! The API is running on http://localhost:8080/measurements.
Remember to open port 8080 if you want it public.

To stop the API:
```bash
mvn spring-boot:stop
```

## Endpoints

Method | URL | Type
------ | --- | ----
GET | /measurements | Get all the measurements as JSON. You can also define the optional parameters `from`, `to`, `device` to narrow down the results. <br> <b>Example:</b> http://localhost:8080/measurements?device=test&from=1571777668227
DELETE | /measurements/delete/all | Erase all measurement recorded in the API.
