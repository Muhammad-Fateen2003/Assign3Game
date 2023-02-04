package fateenSolution.tcp;

import org.json.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ClientTCP {
	Socket sock;
	OutputStream out;
	InputStream in;

    public ClientTCP(String serverName, int port) {
        try {
            Connect(serverName, port);
        } catch (UnknownHostException e) {
            System.out.println(e.toString());
			System.exit(-1);
        } catch (IOException e) {
            System.out.println(e.toString());
			System.exit(-1);
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
			System.out.println("Request: " + requestMessage.toString());
			NetworkUtils.Send(out, JsonUtils.toByteArray(requestMessage));
			responseBytes = NetworkUtils.Receive(in);
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		JSONObject response = JsonUtils.fromByteArray(responseBytes);
		System.out.println("Response size: " + response.toString().length() + " type: " + response.getString("type"));
		if(response.has("action")) {
			System.out.println("Response content: " + response.toString());
		}
		return response;
	}

	public ImageIcon GetImage(JSONObject requestMessage) {
		JSONObject response = ProcessMessage(requestMessage);
		// convert response to Image Icon using lines 104-109 in clinet.Java
		Base64.Decoder decoder = Base64.getDecoder();
		byte[] bytes = decoder.decode(response.getString("data"));
		ImageIcon icon = null;
		try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
		  BufferedImage image = ImageIO.read(bais);
		  icon = new ImageIcon(image);
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		return icon;
	}

}
