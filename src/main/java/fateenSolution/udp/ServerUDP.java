package fateenSolution.udp;

import org.json.*;
import java.io.IOException;
import java.net.DatagramSocket;

public class ServerUDP
{
	public static void main(String[] args) throws IOException {

		int port = 8000;
		if (args.length == 1) {
			port = Integer.parseInt(args[0]);
		}

		System.out.println("Starting UDP Server...");
		StartServer(port);
		System.out.println("Shutting Down USP Server...");
	}

	public static void StartServer(int port) throws IOException {
		GameServer game = new GameServer();
		DatagramSocket sock = null;
		try {
		  sock = new DatagramSocket(port);
		  System.out.println("Waiting for first connection...");
		  // NOTE: SINGLE-THREADED, only one connection at a time
		  while (true) {
			try {
			  while (true) {
				NetworkUtils.Tuple messageTuple = NetworkUtils.Receive(sock);
				JSONObject requestMessage = JsonUtils.fromByteArray(messageTuple.Payload);

				System.out.println("Recived: " + requestMessage.toString());
				JSONObject returnMessage = game.ProcessMessage(requestMessage);
				System.out.println("Returned: " + returnMessage.toString().length() + " bytes");
				byte[] output = JsonUtils.toByteArray(returnMessage);
				NetworkUtils.Send(sock, messageTuple.Address, messageTuple.Port, output);
			  }
			} catch (IOException e) {
			  e.printStackTrace();
			}
		  }
		} catch (IOException e) {
		  e.printStackTrace();
		} finally {
		  if (sock != null) {
			sock.close();
		  }
		}	
	}
}
