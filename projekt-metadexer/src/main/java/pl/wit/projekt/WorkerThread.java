package pl.wit.projekt;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

public class WorkerThread implements Runnable {

private Path path;
private Metadata metadata;
 
//private static AtomicInteger threadCount = new AtomicInteger(0);
 
public WorkerThread(Path path, Metadata metadata) {
this.path = path;
this.metadata = metadata;
    }
    
@Override
public void run() {
	synchronized(metadata) {
		try {
			System.out.println("Running " + Thread.currentThread().getName() + " thread alive: " + Thread.currentThread().isAlive());
			metadata.processImage(path);
		}
		catch (IOException io) {
			io.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}	
	}
    /*
	try {
    	
		int threadNumber = threadCount.incrementAndGet();
		Thread.currentThread().setName("Wątek " + threadNumber);
		System.out.println("Uruchomiono " + Thread.currentThread().getName() + " dla obrazu " + path.toString());
	
        metadata.processImage(path);
        
        System.out.println("Zakończono WĄTEK: " + Thread.currentThread().getName() + " dla obrazu " + path.toString());
        } 
    	catch (IOException e) {
    		e.printStackTrace();
        }*/ 

}}