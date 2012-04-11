package info.kwarc.sissi.alexproxy;
import info.kwarc.sissi.PropertyLoader;
import info.kwarc.sissi.Util;
import java.util.Properties;

/**
 * @author cdavid
 * 
 */
public class GDocsProxy  {
	private static Proxy proxy = null;
	
	private static void boot() {
		Util.d("GDocsProxy boot");
		int port = 54117;
		try {
			port = Integer.parseInt(Util.getProperty("port"));
		} catch (Exception ex) {
			Util.d("Bad or no port in config file");
			Util.e(ex);
		}
		
		Util.d("Starting webserver...");
		
		try {
			proxy = new Proxy(port);
		} catch (Exception ex) {
			System.err.println("Couldn't start server:\n" + ex);
			System.exit(-1);
		}
		
		Util.d("Listening on port " + port + ". Hit Enter to stop.");
		try {
			System.in.read();
		} catch (Throwable t) {
		}
	}
	
	public static void main(String[] args) {
		Util.d("GDocsProxy main");
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				if (proxy != null) {
					proxy.stop();
				}
			}
		}));
		
		Properties props = null;
		String path = "";
		try {
			path = System.getProperty("configPath");
			if (path == null) {
				path = "proxyrc.properties";							
			}
			props = PropertyLoader.loadProperties(path);
			Util.setProperties(props);
			
		} catch(Exception ex) {
			Util.d("Oups! Something went wrong while reading configuration file " + path);
			Util.d("Please add a file named proxyrc.properties to the CLASSPATH or use -DconfigPath=\"path\"");
			Util.e(ex);
		}		
		
		boot();
	}
}
