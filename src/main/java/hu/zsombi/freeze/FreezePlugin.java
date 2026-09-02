package hu.codex.freeze;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class FreezePlugin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    private final Set<UUID> frozenPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        if (getCommand("freeze") != null) {
            getCommand("freeze").setExecutor(this);
            getCommand("freeze").setTabCompleter(this);
        }
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (UUID uuid : frozenPlayers) {
                Player player = getServer().getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.sendTitle(ChatColor.RED + "Le lettél fagyasztva!", ChatColor.GRAY + "Csatlakozz a Discord voice-ba!", 0, 45, 0);
                }
            }
        }, 0L, 40L);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(message("usage"));
            return true;
        }

        Player target = getServer().getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(message("player-not-found"));
            return true;
        }

        if (frozenPlayers.remove(target.getUniqueId())) {
            target.removePotionEffect(PotionEffectType.BLINDNESS);
            target.removePotionEffect(PotionEffectType.MINING_FATIGUE);
            target.sendMessage(message("unfrozen"));
            sender.sendMessage(message("staff-unfrozen").replace("%player%", target.getName()));
        } else {
            frozenPlayers.add(target.getUniqueId());
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0, false, false, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, Integer.MAX_VALUE, 254, false, false, false));
            sendFreezeMessage(target);
            sender.sendMessage(message("staff-frozen").replace("%player%", target.getName()));
        }
        return true;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!frozenPlayers.contains(event.getPlayer().getUniqueId())) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to != null && (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ())) {
            event.setTo(from);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && frozenPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    private void sendFreezeMessage(Player player) {
        String prefix = message("frozen");
        String discordUrl = getConfig().getString("discord-url", "https://discord.gg/szerver");
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(prefix));
        TextComponent link = new TextComponent(TextComponent.fromLegacyText(ChatColor.AQUA + discordUrl));
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, discordUrl));
        component.addExtra(link);
        player.spigot().sendMessage(component);
    }

    private String message(String key) {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages." + key, ""));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) return List.of();
        String input = args[0].toLowerCase();
        List<String> matches = new ArrayList<>();
        for (Player player : getServer().getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(input)) matches.add(player.getName());
        }
        return matches;
    }
}
