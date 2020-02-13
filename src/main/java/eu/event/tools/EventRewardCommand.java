package main.java.eu.event.tools;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import gnu.trove.map.hash.THashMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EventRewardCommand extends Command {
    public EventRewardCommand() {
        super("cmd_event_reward", Emulator.getTexts().getValue("eventtools.keys.cmd_event_reward").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return false;
        } else if (params.length == 1) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("eventtools.cmd_event_reward.missing_user"), RoomChatMessageBubbles.ALERT);
            return true;
        } else {
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(params[1]);
            if (habbo == null) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("eventtools.cmd_event_reward.user.not.found"), RoomChatMessageBubbles.ALERT);
                return true;
            } else if (habbo.equals(gameClient.getHabbo()) && Emulator.getConfig().getBoolean("eventtools.cmd_event_reward.self.reward.not.allowed", true)) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("eventtools.cmd_event_reward.self.reward.not.allowed"), RoomChatMessageBubbles.ALERT);
                return true;
            } else {
                habbo.giveCredits(Emulator.getConfig().getInt("eventtools.cmd_event_reward.credits", 1000));
                habbo.givePixels(Emulator.getConfig().getInt("eventtools.cmd_event_reward.pixels", 1000));
                habbo.givePoints(Emulator.getConfig().getInt("eventtools.cmd_event_reward.gotw.type", 5), Emulator.getConfig().getInt("eventtools.cmd_event_reward.gotw"));
                Item item = Emulator.getGameEnvironment().getItemManager().getItem(Emulator.getConfig().getInt("eventtools.cmd_event_reward.item", -1));
                if (item != null) {
                    HabboItem hItem = Emulator.getGameEnvironment().getItemManager().createItem(habbo.getHabboInfo().getId(), item, 0, 0, "");
                    habbo.getInventory().getItemsComponent().addItem(hItem);
                }
                if (Emulator.getConfig().getBoolean("eventtools.cmd_event_reward.kick.after.reward", true) && habbo.getHabboInfo().getRank().getLevel() < gameClient.getHabbo().getHabboInfo().getRank().getLevel())
                    gameClient.getHabbo().getHabboInfo().getCurrentRoom().kickHabbo(habbo, true);

                try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
                    try (PreparedStatement statement = connection.prepareStatement("UPDATE `users` SET `events_won` = `events_won` + 1 WHERE `id` = ?")) {
                        statement.setInt(1, habbo.getHabboInfo().getId());
                        statement.execute();
                    }
                    try (PreparedStatement statement = connection.prepareStatement("SELECT `events_won` FROM `users` WHERE `id` = ?")) {
                        statement.setInt(1, habbo.getHabboInfo().getId());
                        ResultSet set = statement.executeQuery();
                        set.next();
                        String newLevelGames = Emulator.getConfig().getValue("eventtools.cmd_event_reward.badge.prefix") + set.getInt("events_won");
                        habbo.addBadge(newLevelGames);

                        THashMap<String, String> keys = new THashMap();
                        keys.put("display", "BUBBLE");
                        keys.put("image", Emulator.getConfig().getValue("eventtools.cmd_event_reward.notification.image"));
                        keys.put("message", Emulator.getTexts().getValue("eventtools.cmd_event_reward.notification.message").replace("%user%", habbo.getHabboInfo().getUsername()));
                        BubbleAlertComposer packet = new BubbleAlertComposer("event", keys);
                        for (Habbo habboToNotif : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().values()) {
                            if (habboToNotif == null || habboToNotif.getClient() == null)
                                continue;

                            habboToNotif.getClient().sendResponse(packet);
                        }
                        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("eventtools.cmd_event_reward.sucess").replace("%user%", habbo.getHabboInfo().getUsername()), RoomChatMessageBubbles.ALERT);
                    }
                } catch (SQLException e) {
                    Emulator.getLogging().handleException(e);
                }
            }
        }
        return true;
    }
}
