package com.thisispiri.mnk;

/**Represents an abstract manager that can do everything needed for end users. Activities are an example.*/
public interface MnkManager {
	/**Places a stone at the designated coordinate for the current user.
	 * @return {@code true} if it succeeded to place a stone. {@code false} if it failed.*/
	boolean endTurn(int x, int y);
	/**Initializes the game.*/
	void initialize();
	/**Reverts the last move.*/
	void revertLast();
	void setTimeLimit(int limit);
	/**Asks the user if he approves the {@code action}.*/
	void requestToUser(byte action);
	/**Informs the user that his request to another player was rejected.*/
	void informRejection();
	void informIoError();
	void informUser(String that);
	/**Cancels the connection with another player, whatever that might be.*/
	void cancelConnection();
	/**@return The current {@link MnkGame}.*/
	MnkGame getGame();
}
