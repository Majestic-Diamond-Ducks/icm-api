# Indoor Climate Monitor API

This is an API (using Spring Boot) that stores and fetches measurements from the sets of sensors. Measurements that can be gathered 
are CO2 levels, temperature, brightness and dust density.

## How to run the API on Ubuntu 18.04

Update system and install Maven and Java 8:
```bash
sudo apt update
sudo apt install maven -y
sudo apt install openjdk-8-jre -y
```

Clone the project:
```bash
git clone https://github.com/Majestic-Diamond-Ducks/icm-api.git
```

Run the API (while in the root directory of the project):
```bash
mvn spring-boot:start
```

Done! The API is running on http://localhost:8080/measurementss.
Remember to open port 8080 if you want it public.

To stop the API:
```bash
mvn spring-boot:stop
```

## Endpoints

Method | URL | Type
------ | --- | ----
GET | /sensors | Get all the measurements as JSON. You can also define the optional parameters `from`, `to`, `device` to narrow down the results. <br> <b>Example:</b> http://localhost:8080/measurementss?device=test&from=1571777668227
POST | /sensors | Add a new measurement. The body must be a JSON in [this format](https://raw.githubusercontent.com/Majestic-Diamond-Ducks/Simple-Json-Client/master/document.json).
DELETE | /sensors/delete/all | Erase all measurement recorded in the API.
GET | /sensors/populate | Add a set of new measurement with different measurements and timestamp for testing.