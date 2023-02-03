package fateenSolution.udp;

import org.json.*;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientUDP {
	DatagramSocket sock;
	InetAddress address;
	int port;


    public ClientUDP(String serverName, int port) {
        try {
            Connect(serverName, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public void Connect(String serverName, int serverPort) throws UnknownHostException, IOException {
		System.out.println("Client starting connection...");
		address = InetAddress.getByName(serverName);
		port = serverPort; 
		sock = new DatagramSocket();;
	}

	public void Close() throws IOException	{
		System.out.println("Client closing connection...");
		sock.close();
	}

	public JSONObject ProcessMessage(JSONObject requestMessage) {         
		System.out.println("Sending: " + requestMessage.toString());
		NetworkUtils.Tuple responseTuple = null;
		try {
			NetworkUtils.Send(sock, address, port, JsonUtils.toByteArray(requestMessage));
			responseTuple = NetworkUtils.Receive(sock);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JSONObject response = JsonUtils.fromByteArray(responseTuple.Payload);
		System.out.println("Response: " + response.toString());
		if (response.has("error")) {
			System.out.println(response.getString("error"));
		}
		return response;
	}
}
