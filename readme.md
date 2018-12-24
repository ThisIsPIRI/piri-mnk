# PIRI MNK
A platform for MNK games, the most notable of which are tic tac toe and gomoku.
## How to play
Tab on a cell to place a stone. The game ends when either side manages to make a k(=winning streak) stones-long line consisting only of its own stones.

Clicking on the Bluetooth button brings up a Dialog. Choose whether to play first or second and an ID for your connection(it's fine to leave it blank if there aren't anyone doing the same around you). Click 'connect' and wait a bit. If your device requests permission to pair with the opponent, let it.

## Preferences' behavior
### Rules and Graphics: Local
Preferences are changed when you close the corresponding Preference Dialogs and applied to the game when you return to the main screen(in onStart())
### Graphics: Bluetooth
Same as in local mode.
### Rules: Bluetooth
When a connection is established, the first player's rules are forced upon the second player(ORDER_INITIALIZE) and hasChangedRules is set to false. When a player requests a restart,

if the player changes Preference rules while on this connection(hasChangedRules), we take it as a gesture that he wishes to change the applied rules, too. So we send the changed preferenceRules, except when the changed rules are the same as the current rules(!ruleDiffersFromPreference), in which case sending the rules is pointless.

If the player changes his playing order(chooses to play first/second when he's currently playing second/first), we always send a ruleset: the preferenceRules if the player hasChangedRules and the current rules if not.

Otherwise, no rules are sent and the current rules remain the same.

One drawback of this is even if the second player's Preference rules were different from the first player's when the connection was established, there is no way to send those rules to the first player without needlessly changing a value, then changing it back. Maybe hasChangedRules should be tracked in SettingFragment?

## Credits
The AOSP, for AndroidX in Apache License 2.0

Philippe Schnoebelen, for Emacs Gomoku algorithm
