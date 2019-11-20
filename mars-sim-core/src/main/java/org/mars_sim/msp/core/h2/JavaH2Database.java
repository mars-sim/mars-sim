package org.mars_sim.msp.core.h2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
 
public class JavaH2Database 
{
 
	private static final String DATABASE_DRIVER = "org.h2.Driver";
    private static final String DATABASE_CONNECTION = "jdbc:h2:./mars-sim";//IFEXISTS=TRUE";//;DB_CLOSE_DELAY=-1";
     
    private static final String DATABASE_USER = "sa";
    private static final String DATABASE_PASSWORD = "s$cret";
         
    //Create our H2 SQL Statement
    String QUERY = "CREATE TABLE SETTLERS(personid int auto_increment primary key, "
            + "                                firstname varchar(100), "
            + "                                lastname varchar(100), "
            + "                                hometown varchar(100),"
            + "                                location varchar(30))";
     
    
    public static void main(String[] args) {
    	new JavaH2Database();
    }
     

    public JavaH2Database() {
  
    	createDatabase();
    	
    	insertRecordInTableUsingStatement();
		
        insertRecordInTableUsingPreparedStatement();
        
        getRecordCount();
        
        printRecord();
        
        selectAllUsingPreparedStatement();
    }
    
     
    private void createDatabase() {
        Connection connection;
		try {
			connection = DriverManager.getConnection(DATABASE_CONNECTION, DATABASE_USER, DATABASE_PASSWORD);
			
			//Set auto commit to false  
          connection.setAutoCommit(false);
			
          //Create a Statement Object
          Statement statement = connection.createStatement();
           
          //Execute the statement
          statement.execute(QUERY);
			
          System.out.println("Successfully Created SETTLERS Table!");
         	  
	      //Close the Statement Object
	      statement.close();
	      connection.close();
	      
	      //Close the Connection Object
	      connection.commit();

	      
		} catch (SQLException ex) {
            var lgr = Logger.getLogger(JavaH2Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
			ex.printStackTrace();
		}
    }
    
    //Make a connection to the H2 Database
    private static Connection getDBConnection() {
     
        Connection H2DBConnection = null;
         
        try
        {
            Class.forName(DATABASE_DRIVER);
        } 
        catch (ClassNotFoundException ex) 
        {
            System.out.println(ex.toString());
        }
        
        try
        {
            H2DBConnection = DriverManager.getConnection(DATABASE_CONNECTION, DATABASE_USER, DATABASE_PASSWORD);
             
            return H2DBConnection;
        } 
        catch (SQLException ex) 
        {
            System.out.println(ex.toString());
        }
         
        return H2DBConnection;
    }
    
	//Insert a new record into our SETTLERS table
	public static void insertRecordInTableUsingStatement() 
	{
	    //Create H2 DB Connection Object
	    Connection connection = getDBConnection();
	     
	    try
	    {
	        //Set auto commit to false  
	        connection.setAutoCommit(false);
	         
	        //Create a Statement Object
	        Statement statement = connection.createStatement();
	         
	        //Execute statement to insert record into SETTLERS
	        statement.execute("INSERT INTO SETTLERS (firstname, lastname, hometown, location) "
	                + "                     VALUES ('Barney','Rubble','Elon City','Greenhouse 1')");
	         
	        //Close the statement object
	        statement.close();
	         
	        //Commit the record to the DB table
	        connection.commit();
	    }
	    catch(Exception ex) 
	    {
	        System.out.println(ex.toString());
	    }
	         
	}

	//Insert a new record into our SETTLERS table
	public static void insertRecordInTableUsingPreparedStatement() 
	{
	    //Create H2 DB Connection Object
	    Connection connection = getDBConnection();
	     
	    PreparedStatement prepStatement;
	     
	    try
	    {
	        //Set auto commit to false  
	        connection.setAutoCommit(false);
	         
	        prepStatement = connection.prepareStatement("INSERT INTO SETTLERS (firstname, lastname, hometown, location) VALUES (?, ?, ?, ?)");
	 
	        prepStatement.setString(1, "Freds");
	        prepStatement.setString(2, "Flinstones");
	        prepStatement.setString(3, "Alpha Base");
	        prepStatement.setString(4, "Lander Hab 1");
	        int rc = prepStatement.executeUpdate();
	         
	        System.out.println("Row Count: "+rc);
	         
	        //CLose the PreparedStatement object
	        prepStatement.close();
	         
	        //Close the Connection Object
	        connection.commit();
	         
	    }
	    catch(Exception ex) 
	    {
	        System.out.println(ex.toString());
	    }
	     
	}
	
	public static void getRecordCount() {
	 
		//Create H2 DB Connection Object
	    Connection connection = getDBConnection();
	             
	    PreparedStatement prepStatement;
	             
	    try
	    {
	        prepStatement = connection.prepareStatement("select count(*) as count from SETTLERS");    
	        ResultSet resultSet = prepStatement.executeQuery();
	         
	        while (resultSet.next()) 
	        {
	            System.out.println("Number of Records in SETTLERS: " + resultSet.getInt("count"));
	        }
	         
	        prepStatement.close();
	     
	    }
	    catch(Exception ex) 
	    {
	        System.out.println(ex.toString());
	    }
	}
	 
	private void printRecord() {
        var query = "SELECT * FROM SETTLERS";
        
        try (var con = DriverManager.getConnection(DATABASE_CONNECTION, DATABASE_USER, DATABASE_PASSWORD);
             var st = con.createStatement();
             var rs = st.executeQuery(query)
            ) {

            while (rs.next()) {

                System.out.printf("%d %s %d%n", rs.getInt(1),
                        rs.getString(2), rs.getInt(3));
            }
            
        } catch (SQLException ex) {

            var lgr = Logger.getLogger(JavaH2Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
        }
	}
	
	//Select all records in the SETTLERS table
	public static void selectAllUsingPreparedStatement() {
	    //Create H2 DB Connection Object
	    Connection connection = getDBConnection();
	             
	    PreparedStatement prepStatement;
	             
	    try
	    {
	        prepStatement = connection.prepareStatement("select * from SETTLERS");    
	        ResultSet resultSet = prepStatement.executeQuery();
	         
	        while (resultSet.next()) 
	        {
	            System.out.println("Person ID: "+resultSet.getInt("personid")
	                             +" \nFirst Name: "+resultSet.getString("firstname")
	                             +" \nLast Name: "+resultSet.getString("lastname")
	                             +" \nHometown: "+resultSet.getString("hometown")
	                             +" \nLocation: "+resultSet.getString("location"));
	            System.out.println();
	        }
	        prepStatement.close();
	     
	    }
	    catch(Exception ex) 
	    {
	        System.out.println(ex.toString());
	    }
	 
	}
	
}