import java.net.Socket;

public class ConnectionThread extends Thread {
	
	Socket connection;
	
	ConnectionThread(Socket connection){
		this.connection=connection;
	}
	
	public void run(){
		ReadRequest.myHandler(connection);
	}
}
