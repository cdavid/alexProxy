/**
 * 
 */
package info.kwarc.sissi.alexproxy;

import info.kwarc.sissi.Util;
import info.kwarc.sissi.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cdavid
 *
 */
public class Connection implements Runnable {
	public int id;
	private Socket sock = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	public List<Message> queue = new ArrayList<Message>();
	public int error = -1;
	
	public Connection() {
		Util.d("Connection constructor");
		
		try {
			String host = Util.getProperty("sally_host");
			int port = Integer.parseInt(Util.getProperty("sally_port"));			
			sock = new Socket(host, port);
			out = new PrintWriter(sock.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			new Thread(this).start();
			error = 0;
		} catch (NumberFormatException e) {
			Util.e(e);
		} catch (UnknownHostException e) {
			Util.e(e);
		} catch (IOException e) {
			Util.e(e);
		}	
	}
	
	public void sendMessage(String str) {
		Util.d("Connection sendMessage " + str );
		out.println(str);
	}
	
	public void close() {
		Util.d("Connection close");
		try {
			out.close();
			in.close();
			sock.close();
		} catch (IOException e) {
			Util.e(e);
		}
	}
	
	/**
	 * implements Runnable
	 */
	public void run() {
		Util.d("Connection run");
		String read = "";
		try {
			while( (read = in.readLine()) != null) {
				synchronized(queue) {
					queue.add(Util.messageFromJSON(read));
				}
			}
		} catch (IOException e) {
			Util.e(e);
		}
	}
}
