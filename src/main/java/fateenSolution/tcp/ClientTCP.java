package fateenSolution.tcp;

import org.json.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientTCP {
	Socket sock;
	OutputStream out;
	InputStream in;

    public ClientTCP(String serverName, int port) {
        try {
            Connect(serverName, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public void Connect(String serverName, int port) throws UnknownHostException, IOException {
		System.out.println("Client starting connection...");
		sock = new Socket(serverName, port);
		out = sock.getOutputStream();
		in = sock.getInputStream();
	}

	public void Close() throws IOException	{
		System.out.println("Client closing connection...");
		sock.close();
		out.close();
		in.close();		
	}

	public JSONObject ProcessMessage(JSONObject requestMessage) {         
		byte[] responseBytes = null;
		try {
			System.out.println("Sending: " + requestMessage.toString());
			NetworkUtils.Send(out, JsonUtils.toByteArray(requestMessage));
			responseBytes = NetworkUtils.Receive(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JSONObject response = JsonUtils.fromByteArray(responseBytes);
		System.out.println("Response: " + response.toString());
		return response;
	}
}
