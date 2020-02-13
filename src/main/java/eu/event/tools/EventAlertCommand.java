package main.java.eu.event.tools;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.RoomState;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import gnu.trove.map.hash.THashMap;

import java.util.Map;

public class EventAlertCommand extends Command {
    public EventAlertCommand() {
        super("cmd_eha", Emulator.getTexts().getValue("eventtools.keys.cmd_eha").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() != null) {
            StringBuilder message = new StringBuilder();
            String eventPrefix = Emulator.getConfig().getValue("eventtools.room.name.events.prefix", "[E.V]");
            if (params.length <= 1 && !eventPrefix.isEmpty() && gameClient.getHabbo().getHabboInfo().getCurrentRoom().getName().startsWith(eventPrefix)) {
                message.append(gameClient.getHabbo().getHabboInfo().getCurrentRoom().getName().substring(eventPrefix.length()));
            } else if (params.length >= 2) {
                for (int i = 1; i < params.length; i++) {
                    message.append(params[i]);
                    message.append(" ");
                }
            } else {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("eventtools.error.cmd_eha.missing_message"), RoomChatMessageBubbles.ALERT);
                return true;
            }

            if (Emulator.getConfig().getBoolean("eventtools.open.room.auto", true)) {
                gameClient.getHabbo().getHabboInfo().getCurrentRoom().setUsersMax(100);
                gameClient.getHabbo().getHabboInfo().getCurrentRoom().setState(RoomState.OPEN);
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("eventtools.generic.cmd_eha.unlock_room_advice"), RoomChatMessageBubbles.ALERT);
            }

            THashMap<String, String> codes = new THashMap<>();
            codes.put("ROOMNAME", gameClient.getHabbo().getHabboInfo().getCurrentRoom().getName());
            codes.put("ROOMID", gameClient.getHabbo().getHabboInfo().getCurrentRoom().getId() + "");
            codes.put("USERNAME", gameClient.getHabbo().getHabboInfo().getUsername());
            codes.put("LOOK", gameClient.getHabbo().getHabboInfo().getLook());
            codes.put("TIME", Emulator.getDate().toString());
            codes.put("MESSAGE", message.toString());

            ServerMessage msg = new BubbleAlertComposer("hotel.event", codes).compose();
            for (Map.Entry<Integer, Habbo> set : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet()) {
                Habbo habbo = set.getValue();
                if (Emulator.getConfig().getBoolean("eventtools.cmd_eha.ignore.alert.in.event.room", true) &&
                        habbo.getHabboInfo().getCurrentRoom() != null &&
                        habbo.getHabboInfo().getCurrentRoom() == gameClient.getHabbo().getHabboInfo().getCurrentRoom())
                    continue;

                if (habbo.getHabboStats().blockStaffAlerts) {
                    habbo.whisper(Emulator.getTexts().getValue("eventtools.generic.cmd_eha.block_alert_whisper")
                            .replace("%room%", String.valueOf(habbo.getHabboInfo().getCurrentRoom().getId())), RoomChatMessageBubbles.ALERT);
                    continue;
                }

                habbo.getClient().sendResponse(msg);
            }
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("eventtools.cmd_eha.success"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        return true;
    }
}
