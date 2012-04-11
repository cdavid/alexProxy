/**
 * 
 */
package info.kwarc.sissi.alexproxy;

import info.kwarc.sissi.Message;
import info.kwarc.sissi.Util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * @author cdavid
 *
 */
public class Proxy extends NanoHTTPD {
	private static Map<Integer, Connection> cnx = new HashMap<Integer,Connection>();
	private static int maxID = 0;
	
	/**
	 * @param port
	 * @param wwwroot
	 * @throws IOException
	 */
	public Proxy(int port) throws IOException {
		super(port, new File(""));
	}

	public Response serve(String uri, String method, Properties header, Properties parms, Properties Files) {
		Util.d("Proxy serve");
		System.out.println(method + " '" + uri + "' ");
		String msg = "";
		
		if (uri.equalsIgnoreCase("/connect")) {
			//here we make a new Socket connection to the server
			//and give this connection an ID
			//we return this ID in the message
			
			int id = connect();
			msg = "{\"id\": \"" + id + "\"}";  
			
		} else if (uri.equalsIgnoreCase("/update")) {
			//required parameter: id
			//based on this id, we look up any messages in the queue for this socket connection
			update(Integer.parseInt(parms.getProperty("id")));
		} else if (method.equalsIgnoreCase("POST")){
			if (uri.equalsIgnoreCase("/whoami") || uri.equalsIgnoreCase("/alex.imap") || uri.equalsIgnoreCase("/alex.click")){
				// here we transform any other requests to requests addressed to the server
				// type: whoami (POST) | alex.imap (POST) | alex.click (POST)
			}			
		} else {
			// some other request?
		}
		
		return new NanoHTTPD.Response(HTTP_OK, "application/json", msg);
	}
	
	private static int connect() {
		Connection c = new Connection();		
		int id;
		if (c.error != 0)
			return -1;
		synchronized(cnx) {
			cnx.put(++maxID, c);						
			c.id = maxID;
			id = maxID;
		}
		return id;
	}
	
	private static String update(int id) {
		Connection c = cnx.get(id);
		String response = "[";
		synchronized(c.queue) {
			Iterator<Message> it = c.queue.iterator();
			while(it.hasNext()) {
				Message m = it.next();
				response += Util.messageToJSON(m) + ",";
				it.remove();
			}
		}
		response += "]";
		return response;
	}
}
