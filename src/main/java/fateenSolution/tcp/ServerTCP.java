package fateenSolution.tcp;

import org.json.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTCP
{
	public static void main(String[] args) {
		int port = 8000;
		if (args.length == 1) {
			port = Integer.parseInt(args[0]);
		}
		
		System.out.println("Starting TCP Server...");
		StartServer(port);
		System.out.println("Shutting Down TCP Server...");
	}

	public static void StartServer(int port) {
		GameServer game = new GameServer();
		ServerSocket serv = null;
		try {
		  serv = new ServerSocket(port);
		  System.out.println("Waiting for first connection...");		  
		  // NOTE: SINGLE-THREADED, only one connection at a time
		  while (true) {
			Socket sock = null;
			try {
			  sock = serv.accept(); // blocking wait
			  System.out.println("Client connect");
			  OutputStream out = sock.getOutputStream();
			  InputStream in = sock.getInputStream();
			  while (true) {
				byte[] messageBytes = NetworkUtils.Receive(in);
				JSONObject requestMessage = JsonUtils.fromByteArray(messageBytes);
				System.out.println("Recived: " + requestMessage.toString());

				JSONObject returnMessage = game.ProcessMessage(requestMessage);
	
				// we are converting the JSON object we have to a byte[]
				byte[] output = JsonUtils.toByteArray(returnMessage);
				NetworkUtils.Send(out, output);
				System.out.println("Returned: " + returnMessage.toString().length() + " bytes");

				if (returnMessage.has("action") && returnMessage.getString("action").equals("exit"))
				{
					break; // exit while loop to close socket
				}
			  }
			} catch (Exception e) {
			  System.out.println("Client disconnect");
			} finally {
			  if (sock != null) {
				sock.close();
				System.out.println("Socket closed");
			  }
			}
		  }
		} catch (IOException e) {
			System.out.println(e.toString());
		} finally {
		  if (serv != null) {
			try {
				serv.close();
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		  }
		}
	}
}
