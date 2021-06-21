# Lurchium

Simple timer mod for fabric

Created on basis of Fabric Example Mod ([fabric wiki page](https://fabricmc.net/wiki/tutorial:setup))

Requires fabric launcher and fabric api.

## Usage

Adds the following commands:

- `lurchium give lurchys_clock` Adds a special clock to the player's inventory. If it is selected by the player, it will show the timer on their hud.
- `lurchium set_chest` If the player looks at a chest, it will become the lurchium chest. If the timer runs and an instance of lurchys_clock is put into the chest it consumes the clock and adds the owner of the clock - if he isn't already on it - to the leaderboard.
- `lurchium unset_chest` The lurchium chest becomes a normal chest again.
- `lurchium start_timer` Starts the timer
- `lurchium reset_timer` Resets the timer
- `lurchium finish [player]` Puts the player on the leaderboard and consumes their clock.
- `lurchium leaderboard set_display` If the player looks at a sign, updates to the leaderboard will be printed on this sign and consecutive signs. The first entry of the leaderboard goes on this sign. Following entries are printed on signs right to this one, if existent. The following signs have to be placed in a row going away from the initial sign.
- `lurchium leaderboard unset_display` Updates to the leaderboard won't be printed on the sign anymore.
- `lurchium leaderboard broadcast` Sends the leaderboard as a chat message to all players. 
- `lurchium leaderboard print` Sends the leaderboard as a chat message to the player.
- `lurchium leaderboard reset` Removes all entries from the leaderboard.
- `lurchium add_timer_display` Adds the sign the player is pointing at to the timer displays.
- `lurchium reset_timer_displays` Removes all timer displays
- `lurchium print_timer_display_positions` Sends a list of coordinates of all timer displays.