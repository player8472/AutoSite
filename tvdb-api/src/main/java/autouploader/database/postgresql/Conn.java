/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autouploader.database.postgresql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;


public class Conn {

     public static Connection connection() throws Exception {
    Class.forName("org.postgresql.Driver").newInstance();

    final String     url        = "jdbc:postgresql://localhost/test";
    final Properties properties = new Properties();
    properties.setProperty("user", "postgres");
    properties.setProperty("password", "Melissa32");
    

    return DriverManager.getConnection(url, properties);

  }
}
