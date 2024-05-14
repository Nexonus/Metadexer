package pl.wit.projekt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicInteger;

/***
 * 
 * @author Artur Zakrzewski, Mateusz Bura
 *
 */

public class WorkerThread implements Runnable {

private Path path;
private Metadata metadata;
protected boolean interrupted = false;

public WorkerThread(Path path, Metadata metadata) {
this.path = path;
this.metadata = metadata;
    }
    
@Override
public void run() {
	try {
		if (!interrupted) {
			System.out.println("Running " + Thread.currentThread().getName() + " thread alive: " + Thread.currentThread().isAlive());
			metadata.processImage(path);
		}
		else{
			System.out.println("Thread interrupted: ".concat(Thread.currentThread().getName()));}
}
		catch (IOException io) {
			io.printStackTrace();
		} 
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
}
protected boolean setInterrupted(boolean interruptCase) {
	return this.interrupted = interruptCase;
}}
/*
public class WorkerThread implements Runnable {
	/// To-do, create Worker Threads
	 private Path path;
	 private Metadata metadata;
	 private static AtomicInteger threadCount = new AtomicInteger(0);
	 
	    public WorkerThread(Path path, Metadata metadata) {
	        this.path = path;
	        this.metadata = metadata;
	    }
	    
	    @Override
	    public void run() {
	        try {
	int threadNumber = threadCount.incrementAndGet();
	Thread.currentThread().setName("Wątek " + threadNumber);
	System.out.println("Uruchomiono " + Thread.currentThread().getName() + " dla obrazu " + path.toString());

	            metadata.processImage(path);
	System.out.println("Zakończono WĄTEK: " + Thread.currentThread().getName() + " dla obrazu " + path.toString());
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
	    }

	}*/