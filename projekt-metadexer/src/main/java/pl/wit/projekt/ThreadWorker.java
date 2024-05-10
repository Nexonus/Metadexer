package pl.wit.projekt;


public class ThreadWorker extends Thread{
	
	public ThreadWorker(String filePath) {
		super(filePath);
	}
	
	private String outputImagePath;
	public void run() {
		System.out.println("Thread Worker is running: "+getName());
		//System.out.println(getOutputImagePath());
	}

	public String getOutputImagePath() {
		return outputImagePath;
	}

	public void setOutputImagePath(String outputImagePath) {
		this.outputImagePath = outputImagePath;
	}
}
/*
import java.io.IOException;
import org.apache.commons.imaging.ImagingException;

public class ThreadWorker extends Thread {
	
	private String imagePath;
	static Metadata md = new Metadata();
	
	ThreadWorker(String threadID, String filePath){
		super(threadID);
		this.imagePath = filePath;}
	
	public void run() {
		try {
			//Thread.sleep(1500);
			System.out.println("Thread "+getName()+" is working");
			System.out.println(imagePath);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(getName()+" finished their work.");
	}
}
*/