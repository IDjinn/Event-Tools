package main.java.eu.event.tools;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.CommandHandler;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.HabboPlugin;
import com.eu.habbo.plugin.events.emulator.EmulatorLoadedEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EventTools extends HabboPlugin implements EventListener {
    public static EventTools Instance;
    private final String ADD_COLUMN_QUERY = "ALTER TABLE `users` ADD COLUMN `events_won` INT(11) NOT NULL DEFAULT '0';";

    @Override
    public void onEnable() throws Exception {
        Instance = this;
        Emulator.getPluginManager().registerEvents(this, this);
        if (Emulator.isReady)
            Instance.init();
    }

    @EventHandler
    public static void onEmulatorLoaded(EmulatorLoadedEvent event) {
        Instance.init();
    }

    @Override
    public void onDisable() throws Exception {
        Emulator.getLogging().logStart("[EventTools] Stopped Event tools plugin!");
    }

    @Override
    public boolean hasPermission(Habbo habbo, String s) {
        return false;
    }

    private void init() {
        this.prepareTable();
        this.registerConfigs();
        this.registerTexts();
        this.registerCommands();
        Emulator.getLogging().logStart("[EventTools] Started Event tools plugin!");
    }

    private void registerConfigs() {
        Emulator.getConfig().register("eventtools.room.name.events.prefix", "[E.V]");
        Emulator.getConfig().register("eventtools.open.room.auto", "1");
        Emulator.getConfig().register("eventtools.close.room.auto", "1");
        Emulator.getConfig().register("eventtools.cmd_event_reward.self.reward.not.allowed", "1");
        Emulator.getConfig().register("eventtools.cmd_event_reward.credits", "1000");
        Emulator.getConfig().register("eventtools.cmd_event_reward.pixels", "1000");
        Emulator.getConfig().register("eventtools.cmd_event_reward.gotw", "1000");
        Emulator.getConfig().register("eventtools.cmd_event_reward.gotw.type", "5");
        Emulator.getConfig().register("eventtools.cmd_event_reward.item", "0");
        Emulator.getConfig().register("eventtools.cmd_event_reward.badge.prefix", "JANE");
        Emulator.getConfig().register("eventtools.cmd_event_reward.notification.image", "${image.library.url}notifications/frank_notify.png");
        Emulator.getConfig().register("eventtools.cmd_event_reward.kick.after.reward", "1");
    }

    private void registerTexts() {
        Emulator.getTexts().register("eventtools.cmd_event_reward.notification.message", "The user %user% has won an event.");
        Emulator.getTexts().register("eventtools.keys.cmd_eha", "eha");
        Emulator.getTexts().register("eventtools.cmd_eha.success", "Event alert sent with success.");
        Emulator.getTexts().register("eventtools.keys.cmd_explain_event", "explain_event");
        Emulator.getTexts().register("eventtools.keys.cmd_open_room", "or;open_room");
        Emulator.getTexts().register("eventtools.keys.cmd_close_room", "cr;close_room");
        Emulator.getTexts().register("eventtools.keys.cmd_event_reward", "premiar");
        Emulator.getTexts().register("commands.description.cmd_eha", "Event Alert with more tools");
        Emulator.getTexts().register("commands.description.cmd_explain_event", "Explain a event, with base in a post-it with color yellow in room");
        Emulator.getTexts().register("commands.description.cmd_open_room", "Open the room for all users");
        Emulator.getTexts().register("commands.description.cmd_close_room", "Close the room for all users");
        Emulator.getTexts().register("commands.description.cmd_event_reward", "Give an special reward");
        Emulator.getTexts().register("eventtools.error.cmd_eha.missing_message", "Do you need put the message to send the Event Alert or stay in a Event Room with prefix valid!");
        Emulator.getTexts().register("eventtools.error.cmd_explain.missing_post.it", "This room haven't a post-it with yellow color to explain the event.");
        Emulator.getTexts().register("eventtools.generic.cmd_eha.unlock_room_advice", "Hey, if you're forgot, the room access it's unlocked now.");
        Emulator.getTexts().register("eventtools.generic.cmd_event.lock_room_advice", "Hey, if you're forgot, the room access it's locked now.");
        Emulator.getTexts().register("eventtools.generic.cmd_eha.block_alert_whisper", "An event are running in the hotel, check navigator to go there.");
        Emulator.getTexts().register("eventtools.sucess.cmd_open_room", "This room it's opened now!");
        Emulator.getTexts().register("eventtools.sucess.cmd_close_room", "This room it's closed now!");
        Emulator.getTexts().register("eventtools.cmd_event_reward.missing_user", "Hey, you need put user to give the reward!");
        Emulator.getTexts().register("eventtools.cmd_event_reward.user.not.found", "I not found that user.");
        Emulator.getTexts().register("eventtools.cmd_event_reward.self.reward.not.allowed", "Oops! Can't do that! Self rewards not allowed.");
        Emulator.getTexts().register("eventtools.cmd_event_reward.sucess", "The user %user% got the reward");
        Emulator.getTexts().register("eventtools.cmd_eha.sucess", "The event alert with message %message% has been sent.");
        Emulator.getTexts().register("eventtools.error.cmd_close_room", "Room its already closed.");
        Emulator.getTexts().register("eventtools.error.cmd_open_room", "Room its already opened.");
    }

    private void registerCommands() {
        this.registerPermission("cmd_eha", "'0', '1'", "0", "cmd_event");
        this.registerPermission("cmd_open_room", "'0', '1'", "1", "cmd_mute");
        this.registerPermission("cmd_close_room", "'0', '1'", "1", "cmd_chatcolor");
        this.registerPermission("cmd_explain_event", "'0', '1'", "0", "cmd_eha");
        this.registerPermission("cmd_event_reward", "'0', '1'", "0", "cmd_explain_event");
        Emulator.getGameEnvironment().getPermissionsManager().reload();
        CommandHandler.addCommand(new EventAlertCommand());
        CommandHandler.addCommand(new ExplainEventCommand());
        CommandHandler.addCommand(new OpenRoomCommand());
        CommandHandler.addCommand(new CloseRoomCommand());
        CommandHandler.addCommand(new EventRewardCommand());
    }

    private void prepareTable(){
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(ADD_COLUMN_QUERY)) {
                statement.execute();
            }
        } catch (SQLException e) {
        }
    }

    private boolean registerPermission(String name, String options, String defaultValue, String afterColumn) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("ALTER TABLE  `permissions` ADD  `" + name + "` ENUM(  " + options + " ) NOT NULL DEFAULT  '" + defaultValue + "' AFTER `" + afterColumn + "`")) {
                statement.execute();
                return true;
            }
        } catch (SQLException e) {
        }
        return false;
    }
}