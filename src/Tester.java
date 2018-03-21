import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;
import java.util.stream.Stream;

public class Tester {
	static Backlog log;
	static String logFileDirectory;
	

	
	static String attachmentDump;
		
	
	
	public static void main(String[] args) {
		
		Properties p=new Properties();
    	try { p.load(new FileInputStream("app.properties")); }
		catch(Exception e){ System.out.println("Properties failed to load due to : "+e.toString()); }
    	logFileDirectory=p.getProperty("logFileDirectory");
    	attachmentDump=p.getProperty("attachmentDump");
		log=new Backlog(logFileDirectory);
		System.out.println(logFileDirectory);
		
		log.MoveErrorFIleEmail(attachmentDump);
		
		
		//log.MoveFailedEmailSend("D:\\Resource\\java\\workplace\\MailSendWithThreadAndTimeBreak\\TestDir");
	}

}
