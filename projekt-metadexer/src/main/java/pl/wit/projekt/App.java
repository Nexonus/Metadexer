package pl.wit.projekt;

import org.apache.logging.log4j.*;

public class App {
	/** Create Log **/
	private static Logger Log = LogManager.getLogger(App.class);
	
	public String test() {
		StringBuilder sb = new StringBuilder();
		sb.append("Hello World!");
		
		Log.debug(sb);
		return sb.toString();
	}
}
