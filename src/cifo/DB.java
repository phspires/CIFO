package cifo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DB {
	protected static Connection db_conection;
	protected static String inserts_results = "";
	protected Writer outFile;
	public static void main() {
			      
			    
	}//end main
	
	public void createDBconection(){
		  try {
		         Class.forName("org.postgresql.Driver");
		         db_conection = DriverManager
		            .getConnection("jdbc:postgresql://45.55.11.88:64323/CIFODB",
		            "CIFO", "CIFO");
		      } catch (Exception e) {
		         e.printStackTrace();
		         System.err.println(e.getClass().getName()+": "+e.getMessage());
		         
		      }
		      System.out.println("Opened database successfully");
		
	}
	
	
	public void closeConection(){
		  try { 
		         db_conection.close();
		      } catch (Exception e) {
		         e.printStackTrace();
		         System.err.println(e.getClass().getName()+": "+e.getMessage());
		         
		      }
		      System.out.println("Database conection closed.");
	}
	
	public Double[] selectNextRun() {
	    Statement stmt = null;
	    Double[] resp = new Double[50];
	    try {
		stmt = db_conection.createStatement();
        ResultSet rs = stmt.executeQuery( "SELECT * FROM cifo_test WHERE state = 'new' ORDER BY id LIMIT 1;" );
        if (!rs.isBeforeFirst() ) {    
            System.out.println("No data");
            return null;
        } else {
	        while ( rs.next() ) {
	        	resp[0] = (double)rs.getInt("NUMBER_OF_RUNS");
	        	resp[1] = (double) rs.getInt("NUMBER_OF_GENERATIONS");
	        	resp[2] = (double) rs.getInt("POPULATION_SIZE");
	        	resp[3] = rs.getDouble("MUTATION_PROBABILITY");
	        	resp[4] = (double)rs.getInt("TOURNAMENT_SIZE");
	        	if(rs.getBoolean("smooth")) {
	        		resp[5] = (double) 1;
	        	} else {
	        		resp[5] = (double) 0;
	        	}
	        	if(rs.getBoolean("bestPar")) {
	        		resp[6] = (double) 1;
	        	} else {
	        		resp[6] = (double) 0;
	        	}
	        	if(rs.getBoolean("xOver2")) {
	        		resp[7] = (double) 1;
	        	} else {
	        		resp[7] = (double) 0;
	        	}
	        	if(rs.getBoolean("windOpen")) {
	        		resp[8] = (double) 1;
	        	} else {
	        		resp[8] = (double) 0;
	        	}
	        	resp[9] = (double) rs.getDouble("NUMBER_OF_TRIANGLES");
	        	resp[10] = (double) rs.getDouble("id");
	        }
        }
        rs.close();
        stmt.close();
	    } catch ( Exception e ) {
	         System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	         return null;
	       }
		
		return resp;
	}
	
	public void updateState(int id_database) {
	    Statement stmt = null;
	    try {
		stmt = db_conection.createStatement();
        ResultSet rs = stmt.executeQuery( "UPDATE cifo_test SET state='in_process', datetime_start = 'now()'  WHERE id = " + id_database);
        rs.close();
        stmt.close();
	    } catch ( Exception e ) {
	         System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	         
	       }
	}
	
	public void insertResult(String update_params) {
	    String query = "";
		query = "INSERT INTO cifo_results "
        		+ "(order_id,name,\"currentRun\",\"currentGeneration\",fitness,\"populationSize\",\"tournamentSize\","
        		+ "\"mutationProbability\",\"numberOfTriangles\",c2p,sm,bp) "
        		+ "VALUES (" + Main.id_database + "," + update_params + ");";
		inserts_results += query;
	}
	
	
	public void insertResultsDB(String update_query) {
	    Statement stmt = null;
	    String query = "";
	    try {
		stmt = db_conection.createStatement();
		query =update_query;
        ResultSet rs = stmt.executeQuery(query);
        rs.close();
        stmt.close();
        
	    } catch ( Exception e ) {
	         System.err.println( e.getClass().getName()+": "+ e.getMessage());
	       }
	}
	public void updateFinalResult(String update_params) {
		
		try {
			
			insertResultsDB(inserts_results);
			inserts_results = "";
		} catch ( Exception e ) {
			outFile = new Writer();
			String nameFile = "Test_" + Main.id_database + "_run_"+ Main.currentRun + "_insertResults";	
			outFile.setFileName(nameFile);
			outFile.printLineToFile(inserts_results);
	        System.err.println( e.getClass().getName()+": "+ e.getMessage());
	    }
		
	    Statement stmt = null;
	    String query = "";
	    try {
		stmt = db_conection.createStatement();
		query = "UPDATE cifo_test SET " + update_params + " WHERE id = " +  Main.id_database;
        ResultSet rs = stmt.executeQuery(query);
        rs.close();
        stmt.close();
	    } catch ( Exception e ) {
			outFile = new Writer();
			String nameFile = "Test_" + Main.id_database + "_updateResults";	
			outFile.setFileName(nameFile);
			outFile.printLineToFile(inserts_results);
	         System.err.println( e.getClass().getName()+": "+ e.getMessage() + query);
	       }
	}
	
}//end JDBCExample

