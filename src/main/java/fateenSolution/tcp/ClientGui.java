package fateenSolution.tcp;

import java.awt.Dimension;
import java.io.*;
import org.json.*;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

/**
 * The ClientGui class is a GUI frontend that displays an image grid, an input text box,
 * a button, and a text area for status. 
 * 
 * Methods of Interest
 * ----------------------
 * show(boolean modal) - Shows the GUI frame with the current state
 *     -> modal means that it opens the GUI and suspends background processes. Processing 
 *        still happens in the GUI. If it is desired to continue processing in the 
 *        background, set modal to false.
 * newGame(int dimension) - Start a new game with a grid of dimension x dimension size
 * insertImage(String filename, int row, int col) - Inserts an image into the grid
 * appendOutput(String message) - Appends text to the output panel
 * submitClicked() - Button handler for the submit button in the output panel
 * 
 * Notes
 * -----------
 * > Does not show when created. show() must be called to show he GUI.
 * 
 */
public class ClientGui implements OutputPanel.EventHandlers {
	JDialog frame;
	PicturePanel picturePanel;
	OutputPanel outputPanel;
	boolean gameStarted = false;
	Socket sock;
	OutputStream out;
	ObjectOutputStream os;
	BufferedReader bufferedReader;
	//GameServer game = new GameServer();  //TODO: remove this
	ClientTCP gameClient;
	String prevImage;

	public void connect(String serverName, int port){
		gameClient = new ClientTCP(serverName, port);
		try {
			handleInput("newgame");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Construct dialog
	 * @throws IOException 
	 */
	public ClientGui() throws IOException {
		frame = new JDialog();
		frame.setLayout(new GridBagLayout());
		frame.setMinimumSize(new Dimension(1000, 800));
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);


		// setup the top picture frame
		picturePanel = new PicturePanel();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 0.25;
		frame.add(picturePanel, c);

		// setup the input, button, and output area
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 0.75;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		outputPanel = new OutputPanel();
		outputPanel.addEventHandlers(this);
		frame.add(outputPanel, c);

		picturePanel.newGame(1);
		//insertImage("img/hi.png", 0, 0);
		//outputPanel.appendOutput("Hello, please tell me your name.");
		//handleInput("newgame");
	}

	/**
	 * Shows the current state in the GUI
	 * @param makeModal - true to make a modal window, false disables modal behavior
	 */
	public void show(boolean makeModal) {
		frame.pack();
		frame.setModal(makeModal);
		frame.setVisible(true);
	}

	/**
	 * Insert an image into the grid at position (col, row)
	 * 
	 * @param filename - filename relative to the root directory
	 * @param row - the row to insert into
	 * @param col - the column to insert into
	 * @return true if successful, false if an invalid coordinate was provided
	 * @throws IOException An error occured with your image file
	 */
	public boolean insertImage(String filename, int row, int col) throws IOException {
		System.out.println("Image insert");
		String error = "";
		try {
			// insert the image
			if (picturePanel.insertImage(filename, row, col)) {
				// put status in output
				// outputPanel.appendOutput("Inserting " + filename + " in position (" + row + ", " + col + ")"); // you can of course remove this
				return true;
			}
			error = "File(\"" + filename + "\") not found.";
		} catch(PicturePanel.InvalidCoordinateException e) {
			// put error in output
			error = e.toString();
		}
		outputPanel.appendOutput(error);
		return false;
	}


	public boolean insertImage(ImageIcon image, int row, int col) throws IOException {
		System.out.println("Image insert");
		String error = "";
		try {
			// insert the image
			picturePanel.insertImage(image, row, col);
			return true;
		} catch(PicturePanel.InvalidCoordinateException e) {
			// put error in output
			error = e.toString();
		}
		outputPanel.appendOutput(error);
		return false;
	}

	void handleInput(String input) throws IOException{
		String requestMessage = "{'type': 'processInput', 'value' : '"+input+"'}";

		JSONObject request = new JSONObject(requestMessage);
		JSONObject response = gameClient.ProcessMessage(request);

		if(response.has("error")) {
			String errorMsg = response.getString("error");
			System.out.println(errorMsg);
			outputPanel.appendOutput(errorMsg);
		}

		String sAction = response.getString("action");
		String sImage = response.getString("image");
		String sBlanks = response.getString("blanks");
		int sPoints = response.getInt("points");
		String sOutputs = response.getString("outputs");

		outputPanel.setBlanks(sBlanks);
		outputPanel.setPoints(sPoints);
		if(!sOutputs.isEmpty()){
			outputPanel.appendOutput(sOutputs);
		}

		if (sAction.equals("exit")) {
			gameClient.Close();
			System.exit(0);
		} 

		if(!sImage.equals(prevImage)){
			requestMessage = "{'type': 'getImage', 'value' : '"+sImage+"'}";
			request = new JSONObject(requestMessage);
			ImageIcon image = gameClient.GetImage(request);

			if(response.has("error")) {
				String errorMsg = response.getString("error");
				System.out.println(errorMsg);
				outputPanel.appendOutput(errorMsg);
			}	

			try {
				insertImage(image, 0, 0);
				//insertImage(sImage, 0, 0);
			} catch (Exception e){
				System.out.println(e);
			}
			prevImage = sImage;
		}
	}

	/**
	 * Submit button handling
	 * 
	 * TODO: This is where your logic will go or where you will call appropriate methods you write. 
	 * Right now this method opens and closes the connection after every interaction, if you want to keep that or not is up to you. 
	 * @throws IOException
	 */
	@Override
	public void submitClicked() {
		System.out.println("submit clicked ");
	
		// Pulls the input box text
		String input = outputPanel.getInputText();
		outputPanel.appendOutput(input);

		try {
			handleInput(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		outputPanel.setInputText("");
	}


	/**
	 * Key listener for the input text box
	 * 
	 * Change the behavior to whatever you need
	 */
	@Override
	public void inputUpdated(String input) {
		if (input.equals("surprise")) {
			outputPanel.appendOutput("You found me!");
		}
	}


	public static void main(String[] args) throws IOException {

		// create the frame

		try {
			int port = 8000;
			String serverName = "localhost";
			if (args.length == 2) {
				port = Integer.parseInt(args[0]);
				serverName = args[1];
			}

			ClientGui main = new ClientGui();
			main.connect(serverName, port);
			main.show(true);

		} catch (Exception e) {e.printStackTrace();}

	}
}
