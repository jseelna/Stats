package com.seel.stats;

import java.sql.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.FileChannel;

class CollectPlayerImages {
	
	static final String JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";  
	static final String DB_URL = "jdbc:sqlserver://mssql:1433;databaseName=stats";
	
	static final String USER = "statsuser";
	static final String PASS = "statspw";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		java.sql.Connection conn = null;
		Statement stmt = null;
		
		try{
			Class.forName(JDBC_DRIVER);
			
			System.out.println("Connecting to stats database...");
		    conn = DriverManager.getConnection(DB_URL, USER, PASS);
		    System.out.println("Connected to stats database successfully...");
		    
		    System.out.println("Creating statement...");
		    stmt = conn.createStatement();
		    
		    String sql = "SELECT nameFirst, nameLast, playerID FROM stats.dbo.Master WHERE nameLast LIKE'G%'";
		    ResultSet rs = stmt.executeQuery(sql);
		    
		    int howmany = 0;
		    
		    while(rs.next()){
		         //Retrieve by column name
		    	 howmany++;
		         System.out.println(howmany);
		         String firstName = rs.getString("nameFirst");
		         String lastName = rs.getString("nameLast");
//		         System.out.println("Processing " + firstName + " " + lastName);
		         String playerID = rs.getString("playerID");
		         String playerPage = getPlayerURL(playerID);
		         
		         Document doc = Jsoup.connect(playerPage).get();
		         String pageTitle = doc.title();
		         Elements headshots = doc.select("img[src$=.jpg]");
		         
		         String headshot = null;
		         
		         for (Element image : headshots ){
		        	 String imgsrc = image.attr("src");
		        	 if (imgsrc.contains("headshots")){
		        		 headshot = imgsrc;
		        	 }
		         }
		         String imageName = playerID + ".jpg";
		         File defaultImage = new File("H:/defaulths.jpg");
		         
		         if (headshot != null){
		        	 String fileDest = "D:/images/headshots/" + imageName.charAt(0) + "/" + imageName;
		        	 saveImage(headshot,fileDest);
		         }else{
		        	 File imageDest = new File("D:/images/headshots/" + imageName.charAt(0)+ "/" + imageName);
		        	 copyImageFileUsingFileChannels(defaultImage,imageDest);
		         }
		        	    
		               	         		         
		         //Display values
		         System.out.println(playerID + "  " + headshot);
		        
		      }
		      rs.close();

			
		}catch(SQLException se){
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
		}
			
	}
	
	public static String getPlayerURL(String playerID){
		String playerURL = null;
		playerURL = "http://www.baseball-reference.com/players/"+ playerID.charAt(0)+"/"+playerID+".shtml";
		return playerURL;
	}
	
	public static void saveImage(String imageUrl, String destinationFile) throws IOException {
		URL url = new URL(imageUrl);
		InputStream is = url.openStream();
		OutputStream os = new FileOutputStream(destinationFile);

		byte[] b = new byte[2048];
		int length;

		while ((length = is.read(b)) != -1) {
			os.write(b, 0, length);
		}

		is.close();
		os.close();
	}

	private static void copyImageFileUsingFileChannels(File source, File dest) throws IOException {
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		    
		try {
			inputChannel = new FileInputStream(source).getChannel();
	        outputChannel = new FileOutputStream(dest).getChannel();
	        outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
	    } finally {
	        inputChannel.close();
	        outputChannel.close();
	    }
	}

		
		

}

