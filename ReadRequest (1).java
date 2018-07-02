import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * The main() program in this class is designed to read requests from
 * a Web browser and display the requests on standard output.  The
 * program sets up a listener on port 50505.  It can be contacted
 * by a Web browser running on the same machine using a URL of the
 * form  http://localhost:505050/path/to/resource.html  This method
 * does not return any data to the web browser.  It simply reads the
 * request, writes it to standard output, and then closes the connection.
 * The program continues to run, and the server continues to listen
 * for new connections, until the program is terminated (by clicking the
 * red "stop" square in Eclipse or by Control-C on the command line).
 */
public class ReadRequest {
	
	/**
	 * The server listens on this port.  Note that the port number must
	 * be greater than 1024 and lest than 65535.
	 */
	private final static int LISTENING_PORT = 50505;
	private final static String rootDirectory = "myFiles"; 
	
	
	/**
	 * Main program opens a server socket and listens for connection
	 * requests.  It calls the handleConnection() method to respond
	 * to connection requests.  The program runs in an infinite loop,
	 * unless an error occurs.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(LISTENING_PORT);
		}
		catch (Exception e) {
			System.out.println("Failed to create listening socket.");
			return;
		}
		System.out.println("Listening on port " + LISTENING_PORT);
		try {
			while (true) {
				Socket connection = serverSocket.accept();
				System.out.println("\nConnection from " 
						+ connection.getRemoteSocketAddress());
				//myHandler(connection);
				ConnectionThread myThread = new ConnectionThread(connection);
				myThread.start();
			}
		}
		catch (Exception e) {
			System.out.println("Server socket shut down unexpectedly!");
			System.out.println("Error: " + e);
			System.out.println("Exiting.");
		}
	}

	/**
	 * Handle commuincation with one client connection.  This method reads
	 * lines of text from the client and prints them to standard output.
	 * It continues to read until the client closes the connection or
	 * until an error occurs or until a blank line is read.  In a connection
	 * from a Web browser, the first blank line marks the end of the request.
	 * This method can run indefinitely,  waiting for the client to send a
	 * blank line.
	 * NOTE:  This method does not throw any exceptions.  Exceptions are
	 * caught and handled in the method, so that they will not shut down
	 * the server.
	 * @param connection the connected socket that will be used to
	 *    communicate with the client.
	 */
	private static void handleConnection(Socket connection) {
		try {
			
			Scanner in = new Scanner(connection.getInputStream());
			while (true) {
				if ( ! in.hasNextLine() )
					break;
				String line = in.nextLine();
				if (line.trim().length() == 0)
					break;
				System.out.println("   " + line);
			}
		}
		catch (Exception e) {
			System.out.println("Error while communicating with client: " + e);
		}
		finally {  // make SURE connection is closed before returning!
			try {
				connection.close();
			}
			catch (Exception e) {
			}
			System.out.println("Connection closed.");
		}
	}
	
	public static void myHandler (Socket connection){
		Scanner in;
		OutputStream out = null;
		System.out.println("My handler received data");
		try {
			out = connection.getOutputStream();
			in = new Scanner(connection.getInputStream());
			if (in.hasNext()){
				String line = in.nextLine();
				String[] parts = line.split(" ");
				if (parts.length != 3){
					errorHandle(1,out);
					connection.close();
					return;
				}
				String part1 = parts[0];
				String part2 = parts[1];
				String part3 = parts[2];
				if (! part1.equals( "GET" )){
					errorHandle(2,out);
					connection.close();
					return;				}
				
				File file = new File(rootDirectory + part2);
				if (!file.exists()){
					errorHandle(3,out);
					connection.close();
					return;
					//code for error
				}
				if (!file.canRead()){
					errorHandle(4,out);
					connection.close();
					return;					
				}
				long fileLength = file.length();
				Scanner in2 = new Scanner (file);
				String fileContent = new String();
				while (in2.hasNextLine()){
					fileContent += in2.nextLine();
				}
				in2.close();
				//
				PrintWriter myPrintWriter = new PrintWriter(out);
				myPrintWriter.print("HTTP/1.1 200 OK\r\nConnection: close\r\nContent-Type: text/html\r\nContent-Length:"+fileContent.length()+"\r\n\r\n"+fileContent);
				myPrintWriter.flush();
				myPrintWriter.close();
				in.close();
				connection.close();
				return;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				errorHandle(5,out);
				connection.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return;
			
		}
		
	}

	private static void errorHandle(int i, OutputStream out) {
		PrintWriter myPrintWriter = new PrintWriter(out);
		int errorCode=0;
		if (i==1){
			myPrintWriter.print("HTTP/1.1 400 Bad Request\r\n");
			errorCode=400;
		}
		if (i==2){
			myPrintWriter.print("HTTP/1.1 501 Not Implemented\r\n");
			errorCode=501;
		}
		if (i==3){
			myPrintWriter.print("HTTP/1.1 404 Not Found\r\n");
			errorCode=404;
		}
		if (i==4){
			myPrintWriter.print("HTTP/1.1 403 Forbidden\r\n");
			errorCode=403;
		}
		if (i==5){
			myPrintWriter.print("HTTP/1.1 500 Internal Server Error\r\n");
			errorCode=500;
		}
		myPrintWriter.print("Connection: close\r\n");
		myPrintWriter.print("Connection-Type: text/html\r\n\r\n");
		myPrintWriter.println("<html><h1>Error:"+errorCode+"</h1></html>");
		myPrintWriter.flush();
		myPrintWriter.close();
		
	}

}
