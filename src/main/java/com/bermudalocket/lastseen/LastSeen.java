package com.bermudalocket.lastseen;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.ocpsoft.prettytime.Duration;
import org.ocpsoft.prettytime.PrettyTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
        String commandName = command.getName().toLowerCase();

        if (args.length != 1) {
            msg(sender, "Usage: /" + commandName + " <player-name>");
            return true;
        }

        String playerName = args[0];
        OfflinePlayer player = (OfflinePlayer) Bukkit.getPlayer(playerName);
        if (player == null) {
            player = getOfflinePlayerByName(playerName);
        }
        if (player == null) {
            msg(sender, playerName + " has never been seen before.");
            return true;
        }

        if (commandName.equals("seen")) {
            if (player.isOnline()) {
                msg(sender, player.getName() + " is online now!");
            } else {
                tellLastSeen(sender, playerName);
            }
        } else if (commandName.equals("firstseen")) {
            msg(sender, player.getName() + " first played on " + longToDate(player.getFirstPlayed()));
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
                        msg(tellTo, queriedPlayer + " was last seen on " + 
                            longToDate(timestamp) + " (" +
                            longToRelativeDate(timestamp) + ")");
                    }
                });
    }

    // ------------------------------------------------------------------------
    /**
     * Returns an instance of OfflinePlayer if one can be found in the Bukkit
     * list of offline players which matches the given playerName String.
     * Otherwise, returns null.
     *
     * @see https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Bukkit.html#getOfflinePlayer-java.lang.String-
     *
     * @param playerName the name of the player being queried.
     */
    private OfflinePlayer getOfflinePlayerByName(String playerName) {
        Optional<OfflinePlayer> maybePlayer = Stream.of(Bukkit.getOfflinePlayers​())
                                                    .filter(p -> p.getName​().equals(playerName))
                                                    .findFirst();
        return maybePlayer.isPresent() ? maybePlayer.get() : null;
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
     * Turns the given timestamp into a string describing the relative date
     * in English to the current time now.
     *
     * @param time the timestamp.
     * @return a string describing the relative date.
     */
    private static String longToRelativeDate(Long time) {
        CALENDAR.setTimeInMillis(time);
        List<Duration> durations = PRETTY_TIME_FORMAT.calculatePreciseDuration(CALENDAR.getTime());
        return PRETTY_TIME_FORMAT.format(durations);
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
     * SimpleDateFormat instance used to convert a timestamp to a date string.
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("E MMM d y hh:mm:ss a");

    /**
     * PrettyTime instance used to convert a timestamp to a relative date string.
     */
    private static final PrettyTime PRETTY_TIME_FORMAT = new PrettyTime();

    /**
     * Persistent YAML storage.
     */
    private DataStorage<Long> _storage;

    private boolean _debug;

}
