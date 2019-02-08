package com.bermudalocket.lastseen;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.Calendar;

// ------------------------------------------------------------------------
/**
 * The LastSeen plugin in its entirety, including event handlers and command
 * execution.
 */
public class LastSeen extends JavaPlugin implements Listener, TabExecutor {

    // ------------------------------------------------------------------------
    /**
     * @see JavaPlugin#onEnable().
     */
    @Override
    public void onEnable() {
        saveDefaultConfig();
        _debug = getConfig().getBoolean("debug", false);
        _storage = new DataStorage<>("last-seen", getDataFolder().getPath());
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    // ------------------------------------------------------------------------
    /**
     * Records the timestamp when a player logs in.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (player != null) {
            String name = player.getName();
            _storage.setData(getKey(name), System.currentTimeMillis());
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Records the timestamp when a player logs out.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (player != null) {
            String name = player.getName();
            _storage.setData(getKey(name), System.currentTimeMillis());
        }
    }

    // ------------------------------------------------------------------------
    /**
     * @see TabExecutor#onCommand(CommandSender, Command, String, String[]).
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("seen")) {
            if (args.length == 1) {
                String playerName = args[0];
                Player player = Bukkit.getPlayer(playerName);
                if (player != null && player.isOnline()) {
                    msg(sender, player.getName() + " is online now!");
                } else {
                    tellLastSeen(sender, playerName);
                }
            } else {
                msg(sender, "Usage: /seen <player-name>");
            }
        } else if (command.getName().equalsIgnoreCase("firstseen")) {
            if (args.length == 1) {
                String playerName = args[0];
                Player player = Bukkit.getPlayer(playerName);
                if (player != null && player.isOnline()) {
                    msg(sender, player.getName() + " first played on " + longToDate(player.getFirstPlayed()));
                } else {
                    msg(sender, "The player must be online for us to figure that out.");
                }
            } else {
                msg(sender, "Usage: /firstseen <player-name>");
            }
        }
        return true;
    }

    // ------------------------------------------------------------------------
    /**
     * Tells the given CommandSender the last time the given OfflinePlayer was
     * seen on the server.
     *
     * @param tellTo the CommandSender to send the result to.
     * @param queriedPlayer the name of the player being queried.
     */
    private void tellLastSeen(CommandSender tellTo, String queriedPlayer) {
        _storage.getData(getKey(queriedPlayer), _debug)
                .thenAccept(timestamp -> {
                    if (timestamp == null || timestamp == 0) {
                        msg(tellTo, "Either that player doesn't exist or they haven't been online in a while.");
                    } else {
                        msg(tellTo, queriedPlayer + " was last seen on " + longToDate(timestamp));
                    }
                });
    }

    // ------------------------------------------------------------------------
    /**
     * Turns the given timestamp into a string following the form described by
     * DATE_FORMAT.
     *
     * @param time the timestamp.
     * @return a string following the form described by DATE_FORMAT.
     */
    private static String longToDate(Long time) {
        CALENDAR.setTimeInMillis(time);
        return DATE_FORMAT.format(CALENDAR.getTime());
    }

    // ------------------------------------------------------------------------
    /**
     * Returns the YAML key for the specified player.
     *
     * @param playerName the player's name.
     * @return the YAML key.
     */
    private static String getKey(String playerName) {
        return "players." + playerName + ".last-seen";
    }

    // ------------------------------------------------------------------------
    /**
     * Sends a colorized message to the given CommandSender.
     *
     * @param sender the recipient.
     * @param msg the message.
     */
    private static void msg(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.GOLD + msg);
    }

    /**
     * Calendar object used for converting timestamps.
     */
    private static final Calendar CALENDAR = Calendar.getInstance();

    /**
     * Date formatting object used for converting timestamps.
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("E MMM d y hh:mm:ss a");

    /**
     * Persistent YAML storage.
     */
    private DataStorage<Long> _storage;

    private boolean _debug;

}
