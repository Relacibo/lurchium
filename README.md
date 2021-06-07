# Lurchium

Simple timer mod for fabric

Created on basis of Fabric Example Mod ([fabric wiki page](https://fabricmc.net/wiki/tutorial:setup))

Requires fabric launcher and fabric api.

## Usage

Adds the following commands:

- `lurchium give lurchys_clock` Adds a special clock to the player's inventory. If it is selected by the player, it will show the timer on their hud.
- `lurchium set_chest` If the player looks at a chest, it will become the lurchium chest. If the timer runs and an instance of lurchys_clock is put into the chest it consumes the clock and adds the owner of the clock - if he isn't already on it - to the leaderboard.
- `lurchium unset_chest` The lurchium chest becomes a normal chest again.
- `lurchium set_display` If the player looks at a sign, updates to the leaderboard will be printed on this sign and optional signs on the right to this sign.
- `lurchium unset_display` Updates to the leaderboard won't be printed on the sign anymore.
- `lurchium start_timer` Starts the timer
- `lurchium reset_timer` Resets the timer
- `lurchium finish [player]` Puts the player on the leaderboard and consumes his clock.
- `lurchium broadcast_leaderboard` Sends the leaderboard as a chat message to all players. 
