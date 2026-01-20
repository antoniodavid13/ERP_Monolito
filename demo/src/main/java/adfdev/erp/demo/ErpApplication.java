package adfdev.erp.demo;

import adfdev.erp.demo.database.database;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;
import java.sql.SQLException;

@SpringBootApplication
public class ErpApplication {

	public static void main(String[] args) {
		SpringApplication.run(ErpApplication.class, args);
		Comprobar();

	}
	public static void Comprobar(){
		try(Connection conn = database.getConection()) {
			if(conn != null){
				System.out.println("**************************************");
				System.out.println("✅ CONEXIÓN EXITOSA (DriverManager)");
				System.out.println("Conectado a: " + conn.getMetaData().getDatabaseProductName());
				System.out.println("**************************************");
				conn.close();
			}
        } catch (SQLException e) {
			System.out.println("**************************************");
			System.out.println("CONEXIÓN FALLIDA (DriverManager)");
			System.out.println("**************************************");
        }
    }

}
