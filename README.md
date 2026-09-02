# FreezePlugin

A Paper/Spigot plugin with a Hungarian **/freeze <player>** command.

The first use of the command freezes the player; the next use unfreezes them.
When frozen, the player cannot move, but can still look around.
A clickable Discord link is displayed in chat when a player is frozen.
Server operators can use the command by default. Give others the **freezeplugin.freeze** permission.
Setup

Put the compiled JAR into the server's **plugins** folder, then restart the server. After that, open **plugins/FreezePlugin/config.yml** and replace the **discord-url** value with your own Discord invite link.
