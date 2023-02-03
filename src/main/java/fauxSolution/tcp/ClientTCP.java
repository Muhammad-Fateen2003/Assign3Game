package fauxSolution.tcp;

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

    public ClientTCP() {
        try {
            Connect("localhost", 9000);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public void Connect(String serverName, int port) throws UnknownHostException, IOException {
		sock = new Socket(serverName, port);
		out = sock.getOutputStream();
		in = sock.getInputStream();
	}

	public void Close() throws IOException	{
		sock.close();
		out.close();
		in.close();		
	}

	public JSONObject ProcessMessage(JSONObject requestMessage) throws IOException {         
		JSONObject request = new JSONObject(requestMessage);
		NetworkUtils.Send(out, JsonUtils.toByteArray(request));
		byte[] responseBytes = NetworkUtils.Receive(in);
		JSONObject response = JsonUtils.fromByteArray(responseBytes);
		return response;
	}
}
