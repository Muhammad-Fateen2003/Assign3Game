package fateenSolution.tcp;

import org.json.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Random;

class GameServer {
	int points = 10;
	String currentPrompt = "name";
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

        switch (type) {
            case "processInput":
                String input = request.getString("value");
                ProcessInput(input);                
                break;
        
            default:
                break;
        }

        String reponseMessage = "{'image': '"+sImage+"', 'blanks' : '"+sBlanks+"', 'points' : '"+sPoints+"', 'outputs' : '"+sOutputs+"'}";    

        return new JSONObject(reponseMessage);
    }

	public void ProcessInput(String input){
        sOutputs = "";

		switch (currentPrompt) {
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
		switch (input) {
			case "ci":
				appendOutput("Thank you " + playerName + ", I will show you a picture of a city and you have to guess which one it is");
				currentPrompt = "city";
				RandomCity();
				setBlanks(currentProgress.toString());
				try {
					insertImage("img/city/" + imageToGuess, 0, 0);
				} catch (Exception e){
					System.out.println(e);
				}		
				break;
			case "co":
				appendOutput("Thank you " + playerName + ", I will show you a picture of a country and you have to guess which one it is");
				currentPrompt = "country";
				RandomCountry();
				setBlanks(currentProgress.toString());
				try {
					insertImage("img/country/" + imageToGuess, 0, 0);
				} catch (Exception e){
					System.out.println(e);
				}
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
				appendOutput("Goodbye 😊");
				currentPrompt = "exit";
				System.exit(0);
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
				points ++;
				setPoints(points);
				currentProgress.setCharAt(wordToGuess.indexOf(input) * 2, input.charAt(0));
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
	static String getNameWithoutExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

}
