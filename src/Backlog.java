import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import javax.swing.text.html.HTMLDocument.HTMLReader.FormAction;

public class Backlog {
	private String logDir;
	private BufferedWriter logFile;
	
	public Backlog(String locationString) //constructor
	{
		logDir=locationString;
		if(!logDir.endsWith("\\") && !logDir.endsWith("/"))
		{
			logDir+="\\";
		}
	}
	
	private synchronized void add(String logType,String msg)
	{
		String logFileName=logDir+logType+"\\"+new SimpleDateFormat("yyyy_MM_dd").format(new Date())+".txt";
		
		String destDir=logDir+logType;
		File directory = new File(String.valueOf(destDir));

	    if (!directory.exists()) {
	        directory.mkdir();
	        //System.out.println("Directory Created : "+logType);
	    }
	    
		try {
			logFile=new BufferedWriter(new FileWriter(logFileName, true));
			logFile.write(new SimpleDateFormat("yyyy_MM_dd").format(new Date())+" | "+msg);
			logFile.newLine();
			logFile.close();
			//System.out.println("Message Written @ : "+logType);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private synchronized void addEmailError(String logType,String msg)
	{
		String logFileName=logDir+logType+"\\"+new SimpleDateFormat("yyyy_MM_dd").format(new Date())+".txt";
		
		String destDir=logDir+logType;
		File directory = new File(String.valueOf(destDir));

	    if (!directory.exists()) {
	        directory.mkdir();
	        //System.out.println("Directory Created : "+logType);
	    }
	    
		try {
			logFile=new BufferedWriter(new FileWriter(logFileName, true));
			logFile.write(msg);
			logFile.newLine();
			logFile.close();
			//System.out.println("Message Written @ : "+logType);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	private synchronized void MoveEmailError(String logType,String newLocation)
	{
		String destDir=logDir+"\\"+logType+"\\";
		//System.out.println(destDir);
		//System.exit(1);
		ArrayList list = new ArrayList();
		try (Stream<Path> filePathStream=Files.walk(Paths.get(destDir))) {
		    filePathStream.forEach(filePath -> {
		        if (Files.isRegularFile(filePath)) {
		            list.add(filePath);  
		            //System.out.println(filePath);
		        }
		    });
		    
		    //System.out.println(list);
		    
		    if(list.size()>0)
		    {
		    	int ErrorMove=0;
		    	while(list.size()>ErrorMove)
		    	{
		    		String SourceDir=list.get(ErrorMove)+"";
		    		//File Move Start 
				     File fileM = new File(SourceDir);
		    	     String simpleFileNameDump = fileM.getName();
		    	     File srcFile = new File(SourceDir);
			         srcFile.renameTo(new File(newLocation,simpleFileNameDump));
			         WriteInfo(simpleFileNameDump+" Error File Moved to Dump Read Directory.");
			         System.out.println(simpleFileNameDump+" Error File Moved to Dump Read Directory.");
			        //File Move FInal Nofification 
		    		ErrorMove++;
		    	}
		    }
		    
		    
		}
		catch (IOException ioe) 
	    {
			//if(developmentMode==0)
				System.out.println(ioe);
	    }
	}
	
	public void WriteInfo(String infoMsg)
	{
		add("Info",infoMsg);
	}
	
	public void WriteError(String infoMsg)
	{
		add("Error",infoMsg);
	}
	
	public void WriteSend(String infoMsg)
	{
		add("Send",infoMsg);
	}
	

	public void WriteReceiver(String infoMsg)
	{
		add("Receiver",infoMsg);
	}
	
	public void WriteFailedEmailSend(String infoMsg)
	{
		addEmailError("EmailSendFailed",infoMsg);
	}
	
	public void MoveErrorFIleEmail(String desLocation)
	{
		MoveEmailError("EmailSendFailed",desLocation);
	}
}
