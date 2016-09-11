package com.seel.stats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class IndexCardSet {
	
	static BufferedReader inputFile = null;
	static BufferedWriter outputFile = null;
	
	static final String SET_YEAR = "1955";
	static final String SET_YEAR_M1 = "1954";
	
	static final String FILENAME = "D:/images/bbcards/Topps/" + SET_YEAR + "ToppsIndex.csv";
	
	static final String JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";  
	static final String DB_URL = "jdbc:sqlserver://mssql:1433;databaseName=stats";
	
	static final String USER = "statsuser";
	static final String PASS = "statspw";
	
	
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
//			inputFile = new BufferedReader(new FileReader(args[0]));
			inputFile = new BufferedReader(new FileReader(FILENAME));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		processFile(inputFile);

	}
	
	public static void processFile(BufferedReader inFile){
		
		String record;
		String [] fields;
		String indexLastName;
		String indexFirstName;
		String shortFirstName;
		
		int unknownCount = 0;
		int lnnfCount =0;
		
		java.sql.Connection conn = null;
		PreparedStatement sqlS1Stmt;
		PreparedStatement sqlS2Stmt;
		PreparedStatement sqlS3Stmt;
		PreparedStatement sqlS4Stmt;
		PreparedStatement sqlS5Stmt;
		PreparedStatement sqlS6Stmt;
		
	
				
		try {
			Class.forName(JDBC_DRIVER);
			
			System.out.println("Connecting to stats database...");
		    conn = DriverManager.getConnection(DB_URL, USER, PASS);
		    System.out.println("Connected to stats database successfully...");
		    
		    
		    String sqlS1 = "SELECT Count(*) FROM dbo.MASTER WHERE nameLast = ? and nameFirst = ?";
		    sqlS1Stmt = conn.prepareStatement(sqlS1);
		    
 
		    String sqlS2 = "SELECT Count(*) FROM dbo.MASTER WHERE nameLast = ?";
		    sqlS2Stmt = conn.prepareStatement(sqlS2);
		    
		    String sqlS3 = "SELECT Count(*) FROM dbo.MASTER, dbo.BATTING  WHERE dbo.MASTER.nameLast = ? and dbo.MASTER.playerId = dbo.BATTING.playerId and dbo.BATTING.yearId = ?";
		    sqlS3Stmt = conn.prepareStatement(sqlS3);
		    
		    String sqlS4 = "SELECT Count(*) FROM dbo.MASTER, dbo.PITCHING  WHERE dbo.MASTER.nameLast = ? and dbo.MASTER.playerId = dbo.PITCHING.playerId and dbo.PITCHING.yearId = ?";
		    sqlS4Stmt = conn.prepareStatement(sqlS4);
		    
		    String sqlS5 = "SELECT Count(*) FROM dbo.MASTER, dbo.BATTING WHERE dbo.MASTER.nameLast = ? and dbo.MASTER.nameFirst = ? and dbo.MASTER.playerId = dbo.BATTING.playerId and dbo.BATTING.yearId = ?";
		    sqlS5Stmt = conn.prepareStatement(sqlS5);
		    
		    String sqlS6 = "SELECT Count(*) FROM dbo.MASTER WHERE nameLast = ? and (nameFirst like ? or nameGiven like ?)";
		    sqlS6Stmt = conn.prepareStatement(sqlS6);
		    
		    
		        
		    
		    		    
			while ((record = inFile.readLine()) != null) {
				fields = record.split(",");
				indexLastName = fields[3];
				indexFirstName = fields[4];
				if (indexFirstName.length()> 2){
					shortFirstName = indexFirstName.substring(0,3);
				}else {
					shortFirstName = indexFirstName;
				}
				
				if (indexFirstName.contains("Special") && indexLastName.contains("Special")){
					System.out.println(indexFirstName + " " + indexLastName + " " + "SPECIAL");
				}else {
					int firstPassCt = twoArgQuery(sqlS1Stmt,indexLastName,indexFirstName);
					if (firstPassCt == 1){
						System.out.println(indexFirstName + " " + indexLastName + " " + firstPassCt);
					}else {
						int secondPassCt = oneArgQuery(sqlS2Stmt,indexLastName);
						if (secondPassCt == 1){
							System.out.println(indexFirstName + " " + indexLastName + " " + secondPassCt);
						}else if (secondPassCt == 0){ 
								System.out.println(indexFirstName + " " + indexLastName + " " + "LAST NAME NOT FOUND");
								lnnfCount++;
								continue;
							} else {
							int thirdPassCt = twoArgQuery(sqlS3Stmt,indexLastName,SET_YEAR);
							if (thirdPassCt == 1){
								System.out.println(indexFirstName + " " + indexLastName + " " + thirdPassCt);
							}else{
								int fourthPassCt = twoArgQuery(sqlS4Stmt,indexLastName,SET_YEAR);
								if (fourthPassCt == 1){
									System.out.println(indexFirstName + " " + indexLastName + " " + fourthPassCt);
								}else{
									int fifthPassCt = threeArgQuery(sqlS5Stmt,indexLastName,indexFirstName,SET_YEAR);
									if (fifthPassCt == 1){
										System.out.println(indexFirstName + " " + indexLastName + " " + fifthPassCt);
									}else {
										int sixthPassCt = threeArgQuery(sqlS6Stmt,indexLastName,shortFirstName+"%",shortFirstName+"%");
										if (sixthPassCt == 1){
											System.out.println(indexFirstName + " " + indexLastName + " " + sixthPassCt);
										}else {
											int seventhPassCt = twoArgQuery(sqlS3Stmt,indexLastName,SET_YEAR_M1);
											if (seventhPassCt == 1){
												System.out.println(indexFirstName + " " + indexLastName + " " + seventhPassCt);
											}else {
												int eighthPassCt = threeArgQuery(sqlS5Stmt,indexLastName,indexFirstName,SET_YEAR_M1);
												if (eighthPassCt == 1){
													System.out.println(indexFirstName + " " + indexLastName + " " + eighthPassCt);
												}else {
													System.out.println(indexFirstName + " " + indexLastName + " " + "UNKNOWN");
													unknownCount++;
												}
												
											}
											
										}
										
									}
									
								}
								
							}
							
						}
					}
				}	
				
			}
			
			System.out.println(" ");
			System.out.println("Unknown Count = " + unknownCount);
			System.out.println("Last Name Not Found Count = " + lnnfCount);
			
		}catch (Exception e){
			e.printStackTrace();
		}finally {
	
			if (inFile != null) {
                try {
					inFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
		}
	}
	
	public static int twoArgQuery(PreparedStatement statement, String arg1, String arg2){

		try {
			statement.setString(1,arg1);
			statement.setString(2,arg2);
			ResultSet rs = statement.executeQuery();
			while (rs.next()){
				return rs.getInt(1);
			}
			
		}catch (Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	
	public static int oneArgQuery(PreparedStatement statement, String arg1){

		try {
			statement.setString(1,arg1);
			ResultSet rs = statement.executeQuery();
			while (rs.next()){
				return rs.getInt(1);
			}
			
		}catch (Exception e){
			e.printStackTrace();
		}
		return 0;
		
	}
	
	public static int threeArgQuery(PreparedStatement statement, String arg1, String arg2, String arg3){

		try {
			statement.setString(1,arg1);
			statement.setString(2,arg2);
			statement.setString(3,arg3);
			ResultSet rs = statement.executeQuery();
			while (rs.next()){
				return rs.getInt(1);
			}
			
		}catch (Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	
	

	
	

}
