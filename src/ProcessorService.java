import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ProcessorService {
	Backlog log;
	Timer logTimer;
	int ThreadInterval=100;
	int ThreadCount=5;
	String logFileDirectory;
	String attachmentDump;
	boolean DataDB=false;
	
	public ProcessorService()
	{
		Properties p=new Properties();
		try { p.load(new FileInputStream("app.properties")); }
		catch(Exception e){ System.out.println("Properties failed to load due to : "+e.getMessage()); }
		
		logFileDirectory=p.getProperty("logFileDirectory");
		log=new Backlog(logFileDirectory);
		attachmentDump=p.getProperty("attachmentDump");
		ThreadInterval=Integer.parseInt(p.getProperty("ThreadInterval"));
		ThreadCount=Integer.parseInt(p.getProperty("ThreadCount"));		
	}
	
	public void ProcessSchduler()
	{
		log.WriteInfo("Starting Process Schduler.");
		
		logTimer =new Timer();
		
		logTimer.scheduleAtFixedRate(new java.util.TimerTask() {
			
			@Override
			public void run() {
				
				try {
					//System.out.println("Schdule Running 1");
					log.WriteInfo("Timer Scheduler Start Running.");
					ExcuteProcess();
					log.WriteInfo("Timer Scheduler Complete Running.");
					log.MoveErrorFIleEmail(attachmentDump);
				}
				catch (Exception e) {
					//System.out.println("Schdule Running Failed : "+e.getMessage());
					log.WriteError("Schdule Running Failed : "+e.getMessage().toString());
				}
				
			}
			
		}, 0, ThreadInterval);
	}
	
	private Runnable ProcessEmailNDAttachment(String sourceFileName)
	{
		return new Runnable()
		{

			@Override
			public void run() {
				log.WriteInfo("Mail Sending Starting "+sourceFileName);
				MailnAttachment mna=new MailnAttachment();
				mna.SendMail(sourceFileName);
				log.WriteInfo("Mail Sending Complete "+sourceFileName);
			}
			
		};
	}
	
	public synchronized void ExcuteProcess()
	{
		try
		{
			
			MailnAttachment mna=new MailnAttachment();
			
			ArrayList sourceFiles;
			ArrayList sourceFilesNames;
			//System.out.println("Process Excute Process In Try With MNA");
			sourceFiles=mna.getSource();
			
			if(sourceFiles.size()>0)
			{
				sourceFilesNames=mna.getSourceFileContent(sourceFiles);
				//System.out.println("Source Files "+sourceFilesNames);
				//System.exit(1);
				if(sourceFilesNames.size()>0)
				{
					//System.out.println("Process Excute Process In sourceFiles Nnew Ext");
					//System.exit(1);
					//mna.SendMail(sourceFilesNames);
					//ProcessEmailNDAttachment(sourceFilesNames);
					
					BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(100, true);
					ThreadPoolExecutor executor = new ThreadPoolExecutor(
							ThreadCount, // core size
							ThreadCount+5, // max size
							1, // keep alive time
							TimeUnit.MINUTES, // keep alive time units
							queue // the queue to use
					);
					
					//System.out.println(sourceFilesNames);
					//System.exit(1);
					int fileQue=0;
					while(sourceFilesNames.size() > fileQue)
					{
						log.WriteInfo("Mail Looping Queue Starting : "+fileQue);
						String sourceFileName = sourceFilesNames.get(fileQue)+"";
						
						//System.exit(1);
						executor.execute(ProcessEmailNDAttachment(sourceFileName));
						fileQue++;
					}
					
					try
					{
						executor.shutdown();
						try {
							executor.awaitTermination(5, TimeUnit.MINUTES);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					catch(Exception ex)
					{
						log.WriteError("Error while preparing & processing: " + ex.getMessage() + "|" + ex.getStackTrace()[0]); 
					}
					
				}
				else
				{
					log.WriteInfo("Source content is empty.");
					System.out.println("Source content is empty.");
				}
			}
			else
			{
				//log.WriteInfo("No Dump File Found.");
				System.out.println("No Dump File Found.");
			}
			
			
		}
		catch(Exception e)
		{
			log.WriteInfo("Excution Failed TO Process.");
			System.out.println("Excution Failed TO Process.");
		}
	}
	
	
}
