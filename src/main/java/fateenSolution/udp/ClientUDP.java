package fateenSolution.udp;

import org.json.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Base64;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ClientUDP {
	DatagramSocket sock;
	InetAddress address;
	int port;


    public ClientUDP(String serverName, int port) {
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
		System.out.println("Response size: " + response.toString().length() + " type: " + response.getString("type"));
		if(response.has("action")) {
			System.out.println("Response content: " + response.toString());
		}
		if (response.has("error")) {
			System.out.println(response.getString("error"));
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
