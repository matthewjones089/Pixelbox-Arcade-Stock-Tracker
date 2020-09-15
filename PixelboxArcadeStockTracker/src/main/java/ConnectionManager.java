import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionManager {
	/**
    Initializes the data source.
    @param fileName the name of the property file that
    contains the database driver, url, username and password
  */
 public static void init(InputStream stream)
    throws IOException, ClassNotFoundException
 {
	 
	 //Determines whether to get connection details from the properties file
	 //This is true if using maven
	 //The maven side is not fully working this is why there is the second option
	 boolean useFile = false;
	 String driver = "";
	 
	if (useFile) {
	    Properties props = new Properties();
	    props.load(stream);
	    
	    driver = props.getProperty("jdbc.driver");
	    url = props.getProperty("jdbc.url");
	    username = props.getProperty("jdbc.username");
	    password = props.getProperty("jdbc.password");
	    
	} else {
	    
	   	driver = "com.mysql.cj.jdbc.Driver"; 
	    url = "jdbc:mysql://localhost:3306/pixelboxarcade?serverTimezone=UTC";
	    username = "JDBC USERNAME";
	    password = "JDBC PASSWORD";
    
	}
    Class.forName(driver);
    
 }

 /**
    Gets a connection to the database.
    @return the database connection
 */
 public static Connection getConnection() throws SQLException
 {
    return DriverManager.getConnection(url, 
       username, password);
 }

 private static String url;
 private static String username;
 private static String password;
}
