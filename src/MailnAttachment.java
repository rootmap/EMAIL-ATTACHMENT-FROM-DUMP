//File Lib Start
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Scanner;
//File Lib End

//Email Lib Start
import com.sun.mail.smtp.SMTPMessage;
import java.util.Properties;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
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
	
	private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';
	
    
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
	
	public static String getCorrectedStr(String str)
	{
		String strAr[] = str.split(" ");
		String retString="";
		if(strAr.length>0)
		{
			retString=strAr[strAr.length-1];
		}
		
		return retString;
	}
	
	public static ArrayList getSourceFileContent(ArrayList list)
	{
		ArrayList listFileName = new ArrayList();
		int count=0;
		while (list.size() > count) {
      
			BufferedReader br = null;
		    String newFile=list.get(count)+"";
		    
				    try {
				        Scanner scanner = new Scanner(new File(newFile));
				        while (scanner.hasNext()) {
				        	ArrayList line = parseLine(scanner.nextLine());
				        	
				        	if(!line.get(0).equals("tx_acc_number"))
				        	{
				        		String NetworkProvider=null;
				        		boolean NetworkProviderStatus=false;
				        		String ContactNo=line.get(4).toString()+"";
					        	String numberPattern = ContactNo.substring(0,2);
					        	
					        	if(numberPattern.equals("18"))
					        	{
					        		NetworkProviderStatus=true;
					        		NetworkProvider="Robi";
					        	}
					        	else if(numberPattern.equals("16"))
					        	{
					        		NetworkProviderStatus=true;
					        		NetworkProvider="Airtel";
					        	}
				        		
					        	
					        	
					        	if(NetworkProviderStatus)
					        	{
					        		String sendAccTypeStr= line.get(0)+"";
						        	String FileName=getCorrectedStr(sendAccTypeStr)+"_"+line.get(1);
						        	String Email=line.get(36)+"";
						        	String InvoiceNo=line.get(51)+"";
						        	String InvoiceFromDate=line.get(48)+"";
						        	String InvoiceToDate=line.get(49)+"";
						        	String contentLine = FileName+"|"+Email+"|"+InvoiceNo+"|"+sendAccTypeStr+"|"+NetworkProvider+"|"+InvoiceFromDate+"|"+InvoiceToDate;
						   //Format For Sending String = FileName  |  Email  |  Invoice No |  Subscriber No   |  Network Provider |  Invoice From Date| Invoice To Date
						        	listFileName.add(contentLine);
					        	}
					        						        	
					        	//System.out.println("Network Provider " + NetworkProvider);
						        //System.exit(1);
				        	}
				        	
				        }
				        scanner.close();
				        //System.out.println("File & Email Array " + listFileName);
				        //System.exit(1);
				        
				        //System.out.println("File & Email Array " + listFileName);
			        
			        } 
				    catch (IOException ioe) 
				    {
				    	   ioe.printStackTrace();
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
	
	/*public static String getFileFromDirectory(String dirStr,String searchStr)
	{
		  File dir = new File(dirStr);
	      FilenameFilter filter = new FilenameFilter() {
	         public boolean accept (File dir, String name) { 
	            return name.startsWith(searchStr);
	         } 
	      }; 
	      String[] children = dir.list(filter);
	      if (children == null) {
	         return "";
	      } else { 
	            String dataret=children[0]+"";    
	            return dataret;
	      } 
	}*/
	
	public static String MailTemplate(String InvoiceNo,String SubscriberNo,String NetworkProvider,String InvoiceFromDate,String InvoiceToDate)
	{
		  String strHtml="";
		       strHtml +="<html>\r\n" + 
			       		"<head>\r\n";
		       strHtml +="<title>Your monthly billing invoice no. "+InvoiceNo+" for subscriber no. "+SubscriberNo+"</title>";
			   strHtml +="</head>\r\n" + 
			       		"<body>";
		       
		       strHtml +="<p><span style=\"font-size:10.5pt;font-family:Georgia, serif\"><b><i>Dear Sir/Madam, </i></b></span></p>\r\n" + 
			       		"\r\n" + 
			       		"<p><span style=\"font-size:10.5pt;font-family:Georgia, serif\"><b><i>Greetings from "+NetworkProvider+".</i></b></span></p>\r\n" + 
			       		"\r\n" + 
			       		"<p><span style=\"font-size:10.5pt;font-family:Georgia, serif\"><b><i>Thank you for using "+NetworkProvider+".\r\n" + 
			       		"    \r\n" + 
			       		"    </i>\r\n" + 
			       		"    </b>\r\n" + 
			       		"    </span>\r\n" + 
			       		"</p>\r\n" + 
			       		"\r\n" + 
			       		"<p><span style=\"font-size:10.5pt;font-family:Georgia, serif\"><b><i>Please find attached your last monthly bill for the period: "+InvoiceFromDate+" to "+InvoiceToDate+"\r\n" + 
			       		"    \r\n" + 
			       		"    </i>\r\n" + 
			       		"    </b>\r\n" + 
			       		"    </span>\r\n" + 
			       		"</p>\r\n" + 
			       		"\r\n" + 
			       		"<p><span style=\"font-size:10.5pt;font-family:Georgia, serif\"><b><i>To enjoy 5% discount on the bill plus uninterrupted service (24/7), please sign upfor auto debit facility, using your VISA, MASTER CARD or AMERICAN EXPRESS card.</i></b></span></p>\r\n" + 
			       		"\r\n" + 
			       		"<p><span style=\"font-size:10.5pt;font-family:Georgia, serif\"><b><i>SD @ 5%, VAT @ 15% and SC @ 1% is included in your tariff plan.</i></b></span></p>\r\n" + 
			       		"\r\n" + 
			       		"<p><span style=\"font-size:10.5pt;font-family:Georgia, serif\"><b><i>We are pleased to inform you that you can know your bill or any product &amp; servicerelated information by dialing *10.53# (free)</i></b></span></p>\r\n" + 
			       		"\r\n" + 
			       		"<p><span style=\"font-size:10.5pt;font-family:Georgia, serif\"><b><i>Now, using "+NetworkProvider+" e-Care, you can manage your "+NetworkProvider+" accounts at your own will, anytimefrom anywhere. Apart from knowing your bill, tariff plan and internet offers,you can also activate "+NetworkProvider+" products and services for your number using thissolution. Register for a free account to use the "+NetworkProvider+" e-Care solution at <a href=\"https://ecare."+NetworkProvider+".com.bd/\">https://ecare."+NetworkProvider+".com.bd/</a>.</i></b></span></p>\r\n" + 
			       		"<p><b><i>&nbsp;</i></b></p>\r\n" + 
			       		"<p><span style=\"font-size:10.5pt;font-family:Georgia, serif\"><b><i>You can also download the "+NetworkProvider+" e-Care App on your phone from Google Play Store orApp store (data charges will apply).</i></b></span></p>\r\n" + 
			       		"\r\n" + 
			       		"<p><span style=\"font-size:10.5pt;font-family:Georgia, serif\"><b><i>We thank you once again for being with us and look forward to serving you incoming days.</i></b></span></p>\r\n" + 
			       		"\r\n" + 
			       		"<p><span style=\"font-size:10.5pt;font-family:Georgia, serif\"><b><i>Sincerely,</i></b></span></p>\r\n" + 
			       		"<p><span style=\"font-size: 10.5pt; font-family: Georgia, serif;\"><b><i>"+NetworkProvider+" Customer Service</i></b></span></p>";
			       
		       strHtml +="</body>\r\n" + 
		       			 "</html>";
		
		return strHtml;
	}
	
	
	
	public static void SendMail(String contentLine)
	{
		
		String[] arrayRowLine = contentLine.split("\\|");
		//System.out.println(arrayRowLine.length);
		if(arrayRowLine.length==7)
		{
					
			String ReceiverEmail=arrayRowLine[1];
			//String ReceiverEmail="fahad@divergenttechbd.com";
			String sendFileNamePDF=arrayRowLine[0];
			
        	String InvoiceNo=arrayRowLine[2];
        	String InvoiceFromDate=arrayRowLine[5];
        	String InvoiceToDate=arrayRowLine[6];
        	String NetworkProvider=arrayRowLine[4];
        	String SubscriberNo=arrayRowLine[3];
			
			String sourceFileName = null;
			String LocatedFileName=sendFileNamePDF+".pdf";
			boolean filegetStatus=false;
			File srcFileCheck = new File(sourceDir,LocatedFileName);
			if(srcFileCheck.exists() && !srcFileCheck.isDirectory())
			{
				sourceFileName=LocatedFileName;
				filegetStatus=true;
			}
			
			if(filegetStatus==true)
			{
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

				         message.setSubject("Your monthly billing invoice no. "+InvoiceNo+" for subscriber no. "+SubscriberNo); // Subject for email

				         String dataTemp=MailTemplate(InvoiceNo,SubscriberNo,NetworkProvider,InvoiceFromDate,InvoiceToDate);
				         
				         // Create the Body message part,
				         BodyPart messageBodyPart = new MimeBodyPart();
				         messageBodyPart.setContent(dataTemp, "text/html");
				         
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
				        //message.setContent(multipart);
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
				       log.WriteReceiver(sourceFileName+" Moved to New Read Directory.");

				    }
				    catch (MessagingException e)
				    { 
				    	System.out.println("Logged in Failed File : "+contentLine);
				    	log.WriteFailedEmailSend(contentLine);
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
		else
		{
			System.out.println("Invalid File Length : "+contentLine);
		}

		
	}
	
	public static ArrayList parseLine(String cvsLine) {
        return parseLine(cvsLine, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    public static ArrayList parseLine(String cvsLine, char separators) {
        return parseLine(cvsLine, separators, DEFAULT_QUOTE);
    }

    public static ArrayList parseLine(String cvsLine, char separators, char customQuote) {

        ArrayList result = new ArrayList();

        //if empty, return!
        if (cvsLine == null && cvsLine.isEmpty()) {
            return result;
        }

        if (customQuote == ' ') {
            customQuote = DEFAULT_QUOTE;
        }

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;

        char[] chars = cvsLine.toCharArray();

        for (char ch : chars) {

            if (inQuotes) {
                startCollectChar = true;
                if (ch == customQuote) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {

                    //Fixed : allow "" in custom quote enclosed
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        curVal.append(ch);
                    }

                }
            } else {
                if (ch == customQuote) {

                    inQuotes = true;

                    //Fixed : allow "" in empty quote enclosed
                    if (chars[0] != '"' && customQuote == '\"') {
                        curVal.append('"');
                    }

                    //double quotes in column will hit this!
                    if (startCollectChar) {
                        curVal.append('"');
                    }

                } else if (ch == separators) {

                    result.add(curVal.toString());

                    curVal = new StringBuffer();
                    startCollectChar = false;

                } else if (ch == '\r') {
                    //ignore LF characters
                    continue;
                } else if (ch == '\n') {
                    //the end, break!
                    break;
                } else {
                    curVal.append(ch);
                }
            }

        }

        result.add(curVal.toString());

        return result;
    }

}
