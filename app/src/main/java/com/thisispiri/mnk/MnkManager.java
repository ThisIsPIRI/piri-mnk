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
	/**Asks the user if he approves the {@code action} with {@code details}.*/
	<T> void requestToUser(byte action, T details);
	/**Informs the user of the {@link Info}*/
	void informUser(Info of);
	//void informUser(String that);
	/**Cancels the connection with another player, whatever that might be.*/
	void cancelConnection();
	/**Returns the current rules.
	 * @see MnkManager#setRulesFrom for the format of the returned array.*/
	int[] getRules();
	/**Sets the rules as described in the array. May also {@link MnkManager#initialize} the game.
	 * The array must contain {horSize, verSize, winStreak, timeLimit, myIndex} in order.
	 * myIndex is this player's index in the list of players by playing order, starting from 0.
	 * For example, the first player's myIndex is 0, while that of the second would be 1.
	 * It is typically only useful when 2 or more devices are used.*/
	void setRulesFrom(int[] array);
	enum Info {
		REJECTION, INVALID_MOVE, READ_FAIL, WRITE_FAIL;
	}
}
