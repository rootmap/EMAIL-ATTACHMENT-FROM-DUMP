//File Lib Start
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;
//File Lib End

//Email Lib Start
import com.sun.mail.smtp.SMTPMessage;
import java.util.Properties;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
//Email Lib End


public class MailnAttachment {
	static Backlog log;
	static String logFileDirectory;
	static String attachmentDump;
	static String attachmentDumpRead="";
	static int developmentMode=0;
	static String destinationDir="DemoDestination";
	static String sourceDir="DemoSource";
	
	static String SMTP_HOST="smtp.gmail.com";
	static int SMTP_SOCKET_PORT=465;
	static String SMTP_SOCKET_FACTORY_CLASS="javax.net.ssl.SSLSocketFactory";
	static boolean SMTP_AUTH=true;
	static int SMTP_PORT=587;
	static String SMTP_EMAIL="f.fahad.server@gmail.com";
	static String SMTP_PASSWORD="@sdQwe123";
	static String SET_MAIL_FROM_EMAIL="f.fahad.server@gmail.com";
	
    
    public MailnAttachment()
    {
    	Properties p=new Properties();
    	try { p.load(new FileInputStream("app.properties")); }
		catch(Exception e){ System.out.println("Properties failed to load due to : "+e.toString()); }
    	logFileDirectory=p.getProperty("logFileDirectory");
		log=new Backlog(logFileDirectory);
		attachmentDump=p.getProperty("attachmentDump");
		attachmentDumpRead=p.getProperty("attachmentDumpRead");
	    developmentMode =Integer.parseInt(p.getProperty("developmentMode"));
	    destinationDir =p.getProperty("destinationDir");
	    sourceDir =p.getProperty("sourceDir");
	    
		SMTP_HOST=p.getProperty("SMTP_HOST");
		SMTP_SOCKET_PORT=Integer.parseInt(p.getProperty("SMTP_SOCKET_PORT"));
		SMTP_SOCKET_FACTORY_CLASS=p.getProperty("SMTP_SOCKET_FACTORY_CLASS");
		SMTP_AUTH=Boolean.parseBoolean(p.getProperty("SMTP_AUTH"));
		SMTP_PORT=Integer.parseInt(p.getProperty("SMTP_PORT"));
		SMTP_EMAIL=p.getProperty("SMTP_EMAIL");
		SMTP_PASSWORD=p.getProperty("SMTP_PASSWORD");
		SET_MAIL_FROM_EMAIL=p.getProperty("SET_MAIL_FROM_EMAIL");
    }
	
	public static ArrayList getSource()
	{
		ArrayList list = new ArrayList();
		try (Stream<Path> filePathStream=Files.walk(Paths.get(attachmentDump))) {
		    filePathStream.forEach(filePath -> {
		        if (Files.isRegularFile(filePath)) {
		            list.add(filePath);  
		            //System.out.println(filePath);
		        }
		    });
		}
		catch (IOException ioe) 
	    {
			if(developmentMode==0)
				System.out.println(ioe);
	    }
		
		return list;
	}
	
	public static ArrayList getSourceFileContent(ArrayList list)
	{
		ArrayList listFileName = new ArrayList();
		int count=0;
		while (list.size() > count) {
      
			BufferedReader br = null;
		    String newFile=list.get(count)+"";
		       try{	
		    	   
		           br = new BufferedReader(new FileReader(newFile));		
		           String contentLine = br.readLine();
				   while (contentLine != null) {
				      //String[] arrayRowLine = contentLine.split("\\|");			      
				      //int k=0;
				      //while (arrayRowLine.length > k)
				      //{
					   		listFileName.add(contentLine);
				    	  //listFileName.add(arrayRowLine[k]);
				    	//  k++;
				     // }
				      contentLine = br.readLine();				      
				   }
				   
				
				   
		    } 
		    catch (IOException ioe) 
		    {
		    	if(developmentMode==0)
		    	   ioe.printStackTrace();
		    } 
		    
		       finally 
			   {
					   try {
					      if (br != null)
					    	  br.close();
					   } 
					   catch (IOException ioe) 
				       {
						   if(developmentMode==0)
							   System.out.println("Error in closing the BufferedReader");
					   }
			   }
		       
		       //File Move Start 
			   File fileM = new File(newFile);
	    	   String simpleFileNameDump = fileM.getName();
	    	   File srcFile = new File(newFile);
		       srcFile.renameTo(new File(attachmentDumpRead,simpleFileNameDump));
		       log.WriteReceiver(simpleFileNameDump+" Dump File Moved to New Read Directory.");
		       System.out.println(simpleFileNameDump+" Dump File Moved to New Read Directory.");
		       //File Move FInal Nofification 
			
			count++;
	    }
		
		//System.out.println(listFileName);
		log.WriteInfo("List of file dump : "+listFileName.toString());
		return listFileName;
	}
	
	public static void SendMail(String contentLine)
	{
		
		String[] arrayRowLine = contentLine.split("\\|");

		if(arrayRowLine.length==2)
		{
			
			//System.out.println(arrayRowLine.length);
			//String listFileName="";
			//System.exit(1);
			
			String ReceiverEmail=arrayRowLine[0];
			String sourceFileName=arrayRowLine[1];
			
			File srcFileCheck = new File(sourceDir,sourceFileName);
			if(srcFileCheck.exists() && !srcFileCheck.isDirectory())
			{
					 //file name need to move in one to another
					//Email Script Start
					Properties props = new Properties();
				    props.put("mail.smtp.host", SMTP_HOST);
				    props.put("mail.smtp.socketFactory.port", SMTP_SOCKET_PORT);
				    props.put("mail.smtp.socketFactory.class",SMTP_SOCKET_FACTORY_CLASS);
				    props.put("mail.smtp.auth", SMTP_AUTH);
				    props.put("mail.smtp.port", SMTP_PORT);

				    Session session = Session.getDefaultInstance(props,new javax.mail.Authenticator() {
				        @Override
				        protected PasswordAuthentication getPasswordAuthentication() {
				                return new PasswordAuthentication(SMTP_EMAIL,SMTP_PASSWORD);
				        }
				    });

				    try 
				    {
				    	
				        SMTPMessage message = new SMTPMessage(session);
				        message.setFrom(new InternetAddress(SET_MAIL_FROM_EMAIL));
				        message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(ReceiverEmail));

				         message.setSubject("Testing Subject With Thread"); // Subject for email
				        
				         // Create the Body message part
				         BodyPart messageBodyPart = new MimeBodyPart();
				         messageBodyPart.setText("You using java email servce :D");
				         Multipart multipart = new MimeMultipart();
				         multipart.addBodyPart(messageBodyPart);
				         
				        //File Part with Multi part 
				         messageBodyPart = new MimeBodyPart();
				         String filename = sourceDir+"\\"+sourceFileName;
				         DataSource source = new FileDataSource(filename);
				         messageBodyPart.setDataHandler(new DataHandler(source));
				         messageBodyPart.setFileName(filename);
				         multipart.addBodyPart(messageBodyPart);

				        //Marging Both Part
				        message.setContent(multipart);
				        message.setNotifyOptions(SMTPMessage.NOTIFY_SUCCESS);
				        int returnOption = message.getReturnOption();
				        System.out.println(returnOption);        
				        Transport.send(message);
				        System.out.println("sent");
				        log.WriteSend(filename+" Send Successfully.");
				        
				       //File Move Start 
				       File srcFile = new File(sourceDir,sourceFileName);
				       srcFile.renameTo(new File(destinationDir,sourceFileName));
				       //File Move Ends here
				       //System.out.println(sourceFileName+".pdf Moved to New Read Directory.");	
				       log.WriteReceiver(sourceFileName+" Moved to New Read Directory.");
				       //File Move FInal Nofification 

				    }
				    catch (MessagingException e)
				    { 
				    	System.out.println("Logged in Failed File : "+contentLine);
				    	log.WriteFailedEmailSend(contentLine);
				    	//System.exit(1);
				    	throw new RuntimeException(e); 
				    }
					
					//Email Script End
					
					
					
			}
			else
			{
				System.out.println(contentLine+" File Already Moved/Send.");
				log.WriteError(contentLine+" File Already Moved/Send.");
				//System.out.println(sourceFileName+".pdf Moved to New Read Directory.");
			}
			
		}

		
	}

}
