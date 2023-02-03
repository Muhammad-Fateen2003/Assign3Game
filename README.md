# Assignment 3 Client Sever Hangman Game
This project show a TCP and the same code with UDP. 

Created by David Clements so all kudos go to him! A [video](https://youtu.be/QNGj_EvOHlw "Hello my name is Muhammad") he made about this to help understanding hat is going on in the code. 

## Description

In the example we are actually converting all the data to a byte[] and not just sending over the String and letting Java do the rest

The Client connects to server and opens up a Gui. Client can send num 1-5 over to client
1 - server will send a joke
2 - server will send a quote
3 - server will send an image
4 - server will send either of the above

For more details see code and/or video

## Requirements

- [X] (3 points) When the user starts up it should connect to the server. The server will
reply by asking for the name of the player.
- [X] (3 points) The user should send their name and the server should receive it and greet
the user by name.
- [X] (4 points) The user should be presented a choice between seeing a leader board or
being allowed to guess a city or country (make the interface easy so a user will know
what to do).
- [X] (4 points) The leader board will show all players that have played since the first
start of the server with their name and points. The server will maintain the leader
board and send it to the client when requested. You can assume that the same name
is the same player.
- [X] (2 points) Add on: The leader board is persistent even when the server is restarted
(we did not cover this in class yet though) – so this is extra credit.
- [X] (6 points) If the user chooses city or county the server will send over an image and
the blanks for the word we are searching for.
- [X] (5 points) The image should be chosen randomly from the folder, it is ok if you show
the same image a couple times, you do not have to keep track of which you already
used – you need to print the intended answer in the server terminal to simplify
grading for us (this will be worth some points)
- [X] (5 points) The user can then do one of three things; enter a letter guess (a one letter
response), enter a word (guessing the word), type "quit" to end the game.
- [X] (2 points) The user enters a guess (word or letter) and the server must check the
guess and respond accordingly.
- [X] (2 points) If it is a letter and it is correct +1 point and the letter needs to be
displayed in the UI at the correct place.
- [X] (2 points) If it is a letter and it is wrong -1 point and the points need to be updated
in the UI, the game continues if the user has still more than 0 points.
- [X] (2 points) If it is a word and it is correct +5 point and the user is informed and the
game ends.
- [X] (2 points) If it is a word and it is wrong -5 point and the user needs to be informed,
the game continues if the user has still more than 0 points.
- [X] (3 points) If the user types "quit" the client disconnects gracefully even during the
game.
- [X] (4 points) The current points the user has, have to be displayed in the UI. As soon
as a user has 0 points, they lose. As long as they have more than 0 points they are
still in the game.
- [X] (3 points) If the player won add their new points to their old points on the leader
board (or choose the highest value either is ok). You can assume that their name
always identifies them.17. Evaluations of the input needs to happen on the server side; the client will not know
the images, the corresponding answers, the points, or the leader board. The correct
answers should not be sent to the client. No real points for this since if this is
not done then you do not really use the client/server correctly. So this will lead to
deductions above.
- [X] (4 points) Your protocol must be robust. If a command that is not understood
is sent to the server or an incorrect parameterization of the command, then the
protocol should define how these are indicated. Your protocol must have headers
and optionally payloads. This means in the context of one logical message the
receiver of the message has to be able to read a header, understand the metadata
it provides, and use it to assist with processing a payload (if one is even present).
This protocol needs to be described in detail in the README.md.
- [X] (7 points) Your programs must be robust. If errors occur on either the client or server
or if there is a network problem, you have to consider how these should be handled
in the most recoverable and informative way possible. Implement good general error
handling and output. Your client/server should not crash even when invalid inputs
are provided by the user.
- [X] (3 points) After the player wins/loses they they get back to the main menu (this
is different than my video demenstartion so please do as this assignment says. The
user can now quit, see the leader board or play another game.
- [X] (3 points) - advice: skip this until you have everything else then get back to this:
If a game is in progress and a second user tries to connect, they should receive a
message that a game is already in progress and they cannot connect. How exactly
you handle this, whether you let the user wait or just do not let them do anything
anymore, is up to you. DO NOT use threads, yes I know I am mean.

# TCP

## Running the example

`gradle TCPServer`

`gradle TCPClient`

## UML
![Assigment 3 UML](https://github.com/Muhammad-Fateen2003/Assign3Game/blob/main/Sequence%20Diagram1.png)

### Simple protocol

Client only sends what they want, could consider adding more information like a clientID or optional data. 

```
{ 
	"selected": <int: 1=joke, 2=quote, 3=image, 4=random>
}
```
   
Server sends the data type of "data" and if it is a joke, quote or image and also of course the data response: 
   
```
{
   "datatype": <int: 1-string, 2-byte array>, 
   "type": <"joke", "quote", "image">,
   "data": <thing to return> 
}
```
   
Server sends error if something goes wrong

```
{
	"error": <error string> 
}
```
   
   
## Issues in the code that were not included on purpose
The code is basically to show you how you can use a TCP connection to send over different data and interpret it on either side. It focuses on this alone and not on error handling and some nicer features.
It is suggested that you play with this and try to include some of the below for your own practice. 

- Not very robust, e.g. user enters String
- Second client can connect to socket but will not be informed that there is already a connection from other client thus the server will not response
	- More than one thread can solve this
	- can consider that client always connects with each new request
		- drawback if server is working with client A then client B still cannot connect, not very robust
- Protocol is very simple no header and payload, here we just used data and type to simplify things
- Error handling is very basic and not complete
- Always send the same joke, quote and picture. Having more of each and randomly selecting with also making sure to not duplicate things would improve things



# UDP

The main differences can be seen in NetworkUtils.java. In there the sending and reading of messages happen. For UDP the max buffer length is assumed to be 1024 bytes. So if the package is bigger it is split up into multiple packages. Ever package holds the information about the following data
     *   totalPackets(4-byte int),  -- number of total packages
     *   currentPacket#(4-byte int),  -- number of current package
     *   payloadLength(4-byte int), -- length of the payload for this package
     *   payload(byte[]) -- payload

Client and server are very similar to the TCP example just the connection of course is UDP instead of TCP. The UDP version has the same issues as the TCP example and that is again on purpose. 

