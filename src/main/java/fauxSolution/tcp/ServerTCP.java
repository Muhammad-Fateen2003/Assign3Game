package fauxSolution.tcp;

import org.json.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTCP
{
	public static void main(String[] args) throws IOException {
		StartServer(9000);
	}

	public static void StartServer(int port) throws IOException {
		GameServer game = new GameServer();
		ServerSocket serv = null;
		try {
		  serv = new ServerSocket(9000);
		  // NOTE: SINGLE-THREADED, only one connection at a time
		  while (true) {
			Socket sock = null;
			try {
			  sock = serv.accept(); // blocking wait
			  OutputStream out = sock.getOutputStream();
			  InputStream in = sock.getInputStream();
			  while (true) {
				byte[] messageBytes = NetworkUtils.Receive(in);
				JSONObject requestMessage = JsonUtils.fromByteArray(messageBytes);

				JSONObject returnMessage = game.ProcessMessage(requestMessage);
	
				// we are converting the JSON object we have to a byte[]
				byte[] output = JsonUtils.toByteArray(returnMessage);
				NetworkUtils.Send(out, output);
			  }
			} catch (Exception e) {
			  System.out.println("Client disconnect");
			} finally {
			  if (sock != null) {
				sock.close();
			  }
			}
		  }
		} catch (IOException e) {
		  e.printStackTrace();
		} finally {
		  if (serv != null) {
			serv.close();
		  }
		}
	}
}
