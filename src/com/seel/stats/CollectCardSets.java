package com.seel.stats;

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
import java.util.*;

public class CollectCardSets {
	
	static final String CARD_SERIES = "Topps";  
	static final String CARD_YEAR = "1952";
	static final String BASE_PATH = "D:/images/bbcards/";
	
	static final String BASE_URL = "http://www.tradingcarddb.com";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try{
			
//			String prevLastName = " ";
//			String prevFirstName = " ";
			
			File seriesDIR = new File(BASE_PATH + CARD_SERIES + "/" + CARD_YEAR + "/");
			if (!seriesDIR.exists()){
				seriesDIR.mkdirs();
		}
			
			String cardYearPage = getCardYearURL(CARD_YEAR);
			Document yearDoc = Jsoup.connect(cardYearPage).timeout(20000).get();
			Elements cardsets = yearDoc.select("a[href]");
			
			String cardSetPage = null;
	         
	         for (Element cardset : cardsets ){
	        	 String link = cardset.attr("href");
	        	 if ((link.contains("ViewSet"))&& (link.endsWith(CARD_YEAR+"-"+CARD_SERIES))){
	        		 cardSetPage = BASE_URL+link;
	        	 }
	         }
	         System.out.println(cardSetPage);
	         
	         Document setDoc = Jsoup.connect(cardSetPage).timeout(20000).get();
	         
	         Elements anchors = setDoc.select("a[href]");
	         
	         String viewAllCardSetPage = null;
	         
	         for (Element anchor : anchors ){
	        	 String link = anchor.attr("href");
	        	 if (link.contains("ViewAllSet")){
	        		 viewAllCardSetPage = BASE_URL+link;
	        	 }
	        	 
	         }
	         
//	         System.out.println(viewAllCardSetPage);
	         List<String> setPages = new ArrayList<String>();
	         setPages.add(viewAllCardSetPage);
	         
	         Document allSetDoc = Jsoup.connect(viewAllCardSetPage).timeout(20000).get();
	         Elements uls = allSetDoc.select("ul[class=pagination]");
	         
	                
	         Element ul = uls.first();
	         List lis = ul.childNodes();
	         Iterator<Node> liIterator = lis.iterator();
	    	 while (liIterator.hasNext()) {
	    		Node currentNode = liIterator.next();
	    		List subNodes = currentNode.childNodes();
	    		Iterator<Node> subNodeIterator = subNodes.iterator();
	    		while (subNodeIterator.hasNext()){
	    			Node subNode = subNodeIterator.next();
	    			
	    			List values = subNode.childNodes();
	    			Iterator<Node> valuesIterator = values.iterator();
	    			while (valuesIterator.hasNext()){
	    				Node value = valuesIterator.next();
	    				if (isInteger(value.toString())){
	    					String subNodeText = subNode.toString();
	    					if (!(subNodeText.contains("#"))){
	    						String href = subNode.attr("href");
//	    						System.out.println(viewAllCardSetPage+href);
	    						setPages.add(viewAllCardSetPage+href);
		    				
	    					}
	    					
	    					
	    				}
	    				
	    			}
	    			
	    		}
	        }
	        
	    	 Iterator<String> setPagesIterator = setPages.iterator();
	         while (setPagesIterator.hasNext()){
	        	 String currentPage = setPagesIterator.next();
	        	 Document currentPageDoc = Jsoup.connect(currentPage).timeout(20000).get();
	        	 Elements currentPageCardLinks = currentPageDoc.select("a[href]");
	        	 
		         
		         String currentCardPage = null;
		         
		         for (Element cardLink : currentPageCardLinks ){
		        	 Elements cardLinkChildren = cardLink.children();
		        	 if (cardLinkChildren.isEmpty()){
		        		 String currentCardLink = cardLink.attr("href");
			        	 if (currentCardLink.contains("ViewCard")){
			        		 currentCardPage = BASE_URL+currentCardLink;
//			        		 System.out.println(currentCardPage);
			        		 String baseImageName = currentCardPage.substring(currentCardPage.lastIndexOf(CARD_YEAR),currentCardPage.lastIndexOf("?"));
//			        		 System.out.println(baseImageName);
			        		 StringTokenizer bINsT = new StringTokenizer(baseImageName,"-");
			        		 List<String> tokens = new ArrayList();
			        		 
			        		 while (bINsT.hasMoreTokens()) {
			        		
			        	         tokens.add(bINsT.nextToken());
			        	         
			        	     }
			        		 
			        		 String cardNumber = tokens.get(2);
			        		 
			        		 String firstName;
			        		 String lastName;
			        		 
			        		 if (tokens.size() > 5){
			        			 firstName = "Special";
			        			 lastName = "Special";
			        		 }else {
			        			 firstName = tokens.get(3);
				        		 lastName = tokens.get(4);
			        		 }
			        		 			        		 
			        			        		 
//			        		 if (!(lastName.equals(prevLastName) && firstName.equals(prevFirstName))){
			        			 
//			        		    prevLastName = lastName.toString();
//			        		    prevFirstName = firstName.toString();
			        		 	System.out.println(CARD_YEAR+","+CARD_SERIES+","+cardNumber+","+lastName+","+firstName+","+baseImageName);
			        		 
			        		 	Document cardDoc = Jsoup.connect(currentCardPage.toString()).timeout(20000).get();
			        		 	Elements cardImages = cardDoc.select("img[class=img-responsive");
			        		 	for (Element cardImage : cardImages){
			        		 		String cardImageURL = BASE_URL + cardImage.attr("src");
			        		 		String cardImageName = cardImageURL.substring(cardImageURL.lastIndexOf("/")+1);
			        		 		String endImageName = cardImageName.substring(cardImageName.lastIndexOf(".")-2);
			        		 		String destBaseImageName = CARD_YEAR + CARD_SERIES + cardNumber;
			        		 		String cardImageDest = BASE_PATH + CARD_SERIES + "/" + CARD_YEAR + "/" + destBaseImageName + "-" + endImageName;
			        		 		saveImage(cardImageURL,cardImageDest);
			        		 	
			        			 
//			        		 	}
			        		 }
			        	 }
		        	 }
		        	 
		        	 
		         }
	        	 
	         }
			

	}catch(Exception e) {
		e.printStackTrace();
	}
		
	}
	
	public static String getCardYearURL(String year){
		String cardYearURL = null;
		cardYearURL = BASE_URL+"/ViewAll.cfm/sp/Baseball/year/"+year;
		return cardYearURL;
	}
	
	public static boolean isInteger(String s) {
	      boolean isValidInteger = false;
	      try
	      {
	         Integer.parseInt(s);
	 
	         // s is a valid integer
	 
	         isValidInteger = true;
	      }
	      catch (NumberFormatException ex)
	      {
	         // s is not an integer
	      }
	 
	      return isValidInteger;
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


}
