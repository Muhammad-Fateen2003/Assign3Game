package fateenSolution.udp;

import org.json.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

import javax.imageio.ImageIO;

class GameServer {
	int points = 10;
	String currentPrompt = "newgame";
	String playerName;
	StringBuffer currentProgress;
	String imageToGuess;
	String wordToGuess;
	JSONObject leaderBoard;

    String sImage;
    String sBlanks = "";
    int sPoints;
    String sOutputs;

    public JSONObject ProcessMessage(JSONObject request){         
		String type = request.getString("type");
		String input = request.getString("value");
		JSONObject response = null;

        switch (type) {
            case "processInput":
                ProcessInput(input);
				String reponseMessage = "{'type': 'response', 'action': '"+currentPrompt+"', 'image': '"+sImage+"', 'blanks' : '"+sBlanks+"', 'points' : '"+sPoints+"', 'outputs' : '"+sOutputs+"'}";                    
				response = new JSONObject(reponseMessage);
                break;
			case "getImage":
				response = GetImage(input);
				break;
            default:
				System.out.println("Ignoring type: " + type);
				break;
        }

        return response;
    }

	private JSONObject GetImage(String input) {
		JSONObject json = new JSONObject();
		json.put("type", "image");
	
		File file = new File(input);
		if (!file.exists()) {
		  System.err.println("Cannot find file: " + file.getAbsolutePath());
		}
		// Read in image
		BufferedImage img = null;
		try {
			img = ImageIO.read(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] bytes = null;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
		  ImageIO.write(img, "png", out);
		  bytes = out.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (bytes != null) {
		  Base64.Encoder encoder = Base64.getEncoder();
		  json.put("data", encoder.encodeToString(bytes));
		  return json;
		}
		return error("Unable to save image to byte array");
	}

	public void ProcessInput(String input){
        sOutputs = "";

		switch (input) {
			case "quit":
				currentPrompt = "exit";
				break;
			
			case "newgame":
				currentPrompt = "newgame";
				break;
		
			default:
				break;
		}

		switch (currentPrompt) {
			case "newgame":
				HandleNewGame();
				break;
			case "name":
				HandleName(input);
				break;
			case "type":
				HandleType(input);
				break;
			case "again":
				HandleAgain(input);
				break;
			case "city":
				HandleGuess(input);
				break;
			case "country":
				HandleGuess(input);
				break;
			case "exit":
				HandleExit();
				break;
            default:
                break;
		}
	}

    /**
     * Set points in label box
     * @param points current points in round
     */
    void setBlanks(String blanks) {
        sBlanks = blanks;
    }

    /**
     * Set points in label box
     * @param points current points in round
     */
    void setPoints(int points) {
        sPoints = points;
    }

    /**
     * Append a message to the output panel
     * @param message - the message to print
     */
    void appendOutput(String message) {
        if(!sOutputs.isEmpty())
        {
            sOutputs += "\\n";
        }
        sOutputs += message;
    }

	boolean insertImage(String filename, int row, int col) throws IOException {
        sImage = filename;
        return true;
    }

	void ReadJSONFile(){
		try {
			FileReader lbReader = new FileReader("json/leaderboard.json");
			leaderBoard = new JSONObject(new JSONTokener(lbReader));
		} catch (Exception e) {
			leaderBoard = new JSONObject();
		} 
	}

	FilenameFilter imgFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			String lowercaseName = name.toLowerCase();
			if (lowercaseName.endsWith(".png") || lowercaseName.endsWith(".jpg") || lowercaseName.endsWith(".jpeg")) {
				return true;
			} else {
				return false;
			}
		}
	};

	String GetBlanks(int count){
		return new String(new char[count]).replace("\0", "_ ");
	}

	void RandomCity()
	{
		File cityFile = new File("img/city");
		File[] cities = cityFile.listFiles(imgFilter);
		Random random = new Random();
		int randomIndexCi = random.nextInt(cities.length);
		File randomCity = cities[randomIndexCi];
		imageToGuess = randomCity.getName();
		wordToGuess = getNameWithoutExtension(randomCity);
		// currentProgress = new StringBuffer("_ ".repeat(wordToGuess.length()));
		currentProgress = new StringBuffer(GetBlanks(wordToGuess.length()));
	}

	void RandomCountry()
	{

		File countryFile = new File("img/country");
		File[] countries = countryFile.listFiles(imgFilter);
		Random random = new Random();
		int randomIndexCo = random.nextInt(countries.length);
		File randomCountry = countries[randomIndexCo];
		imageToGuess = randomCountry.getName();
		wordToGuess = getNameWithoutExtension(randomCountry);
		// currentProgress = new StringBuffer("_ ".repeat(wordToGuess.length()));
		currentProgress = new StringBuffer(GetBlanks(wordToGuess.length()));
	}

	boolean IsCorrect()
	{
		return !currentProgress.toString().contains("_");
	}

	boolean SaveLeader() {
		//playername, points
		if(leaderBoard.has(playerName)){
			int prevPoints = leaderBoard.getInt(playerName);
			leaderBoard.put(playerName, points + prevPoints);
		} else {
			leaderBoard.put(playerName, points); 
		}
		try {
			FileWriter lbWriter = new FileWriter("json/leaderboard.json");
			lbWriter.write(leaderBoard.toString());
			lbWriter.flush();
			lbWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

	void HandleNewGame(){
		try {
			insertImage("img/hi.png", 0, 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		appendOutput("Hello, please tell me your name.");	
		points = 0;
		setPoints(points);
		currentProgress = new StringBuffer();
		setBlanks(currentProgress.toString());		
		currentPrompt = "name";	
	}

	void HandleExit(){
		appendOutput("Goodbye");
	}

	void HandleName(String input){
		ReadJSONFile();
		playerName = input;
		appendOutput("Hello: " + playerName);
		try {
			insertImage("img/questions.jpg", 0, 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		appendOutput("Would you like to guess a city (ci) or a country (co) or see the leaderboard (leader)");
		points = 10;
		setPoints(points);
		currentPrompt = "type";
	}
	
	void HandleType(String input){
		try {	
			switch (input) {
				case "ci":
					appendOutput("Thank you " + playerName + ", I will show you a picture of a city and you have to guess which one it is");
					currentPrompt = "city";
					RandomCity();
					setBlanks(currentProgress.toString());
					insertImage("img/city/" + imageToGuess, 0, 0);
					break;
				case "co":
					appendOutput("Thank you " + playerName + ", I will show you a picture of a country and you have to guess which one it is");
					currentPrompt = "country";
					RandomCountry();
					setBlanks(currentProgress.toString());
					insertImage("img/country/" + imageToGuess, 0, 0);
					break;
				case "leader":
					appendOutput("Thank you " + playerName + ", here is the leaderboard!");
					appendOutput(leaderBoard.toString());
					appendOutput("Would you like to play again (y/n)?");
					currentPrompt = "again";
					break;
				default:
					break;
			}
		} catch (Exception e){
			System.out.println(e);
		}		
	}

	void HandleAgain(String input){
		switch (input) {
			case "y":
				try {
					insertImage("img/hi.png", 0, 0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				appendOutput("Hello, please tell me your name.");
				currentPrompt = "name";
				setPoints(0);
				break;
			default:
				currentPrompt = "exit";
				HandleExit();
				break;
		}
	}

	void HandleGuess(String input){
		if (input.length() != 1)
		{
			if (input.equals(wordToGuess)) {
				points += 5;
				setPoints(points);
				for(int n = 0; n < input.length(); n++)						{
					currentProgress.setCharAt(n * 2, input.charAt(n));
				}
				setBlanks(currentProgress.toString());
			}
			else {
				points -= 5;
				setPoints(points);
			}
		} else {
			if(currentProgress.toString().contains(input)) {
				// ignore repeat guesses
			}
			else if(wordToGuess.contains(input)){
				int index = wordToGuess.indexOf(input);
				points ++;
				setPoints(points);
				while (index >= 0) {
					currentProgress.setCharAt(index * 2, input.charAt(0));
					index = wordToGuess.indexOf(input, index + 1);
				}
				setBlanks(currentProgress.toString());
				
			}
			else {
				points --;
				setPoints(points);
			}
		}

		if (points <= 0) {
			try {
				insertImage("img/lose.jpg", 0, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			appendOutput("You ran out of guesses. The word was " + wordToGuess);
			appendOutput("Would you like to play again (y/n)?");
			currentPrompt = "again";
		}

		if (IsCorrect()) {
			SaveLeader();
			try {
				insertImage("img/win.jpg", 0, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			appendOutput("Would you like to play again (y/n)?");
			currentPrompt = "again";
		}
	}

	/**
	 * Returns the name of a file without its extension.
	 *
	 * @param file the File object that represents the file whose name without extension is to be returned
	 * @return the name of the file without its extension
	 */
	String getNameWithoutExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

	JSONObject error(String err) {
		System.out.println("Error: " + err);
		JSONObject json = new JSONObject();
		json.put("type", "error");
		json.put("error", err);
		return json;
	}
}
