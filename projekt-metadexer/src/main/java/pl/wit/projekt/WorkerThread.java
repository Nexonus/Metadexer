package pl.wit.projekt;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

/**
 * @author Artur Zakrzewski (Setting up threads and generating new threadpools)
 * @author Mateusz Bura (Running threads and synchronizing)
 * @author Jan Konarski (Thread interrupt WIP)
 */

public class WorkerThread implements Runnable {

private Path path;
private Metadata metadata;
protected boolean interrupted = false;

/**
 * 
 * @param path
 * @param metadata
 */
public WorkerThread(Path path, Metadata metadata) {
this.path = path;
this.metadata = metadata;
    }
@Override
public void run() {
	try {
		if (!interrupted) {
			//System.out.println("Running " + Thread.currentThread().getName() + " thread alive: " + Thread.currentThread().isAlive()); // dbg
			metadata.processImage(path);
		}
		else{
			System.out.println("Thread interrupted: ".concat(Thread.currentThread().getName()));}	// not working atm, leaving this for dbg
}
		catch (IOException io) {
			io.printStackTrace();
		} 
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
}
/***
 * 
 * @return
 */
public boolean getThreadStatus() {
	return Thread.currentThread().isAlive();
}
// Try to force thread interrupt by shutting the window down - JK, doesn't work atm
/**
 * 
 * @param interruptCase
 * @return
 */
protected boolean setInterrupted(boolean interruptCase) {
	return this.interrupted = interruptCase;
}}