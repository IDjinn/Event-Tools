package main.java.eu.event.tools;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.items.ItemInteraction;
import com.eu.habbo.habbohotel.items.PostItColor;
import com.eu.habbo.habbohotel.items.interactions.InteractionPostIt;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.RoomState;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.generic.alerts.StaffAlertWithLinkComposer;
import com.eu.habbo.messages.outgoing.modtool.ModToolIssueHandledComposer;
import gnu.trove.set.hash.THashSet;

import java.util.*;

public class ExplainEventCommand extends Command {

    public ExplainEventCommand() {
        super("cmd_explain_event", Emulator.getTexts().getValue("eventtools.keys.cmd_explain_event").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        Room room = gameClient.getHabbo().getHabboInfo().getCurrentRoom();
        if(room == null)
            return false;

        Set<HabboItem> postIts = new HashSet<>();
        HabboItem item = null;
        for (HabboItem wallItem : room.getWallItems()) {
            if (wallItem == null || wallItem.getBaseItem().getInteractionType().getType() != InteractionPostIt.class)
                continue;

            postIts.add(wallItem);
        }
        if (postIts.size() > 0) {
            if (postIts.size() > 1) {
                item = postIts.stream().filter(wallItem -> wallItem.getExtradata().startsWith(PostItColor.YELLOW.hexColor)).findFirst().orElse(null);
            } else {
                item = (HabboItem) postIts.toArray()[0];
            }

            if (item != null) {
                String message = item.getExtradata().substring(7);
                if (!message.isEmpty()) {
                    if (Emulator.getConfig().getBoolean("eventtools.close.room.auto")) {
                        room.setState(RoomState.LOCKED);
                        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("eventtools.generic.cmd_event.lock_room_advice"), RoomChatMessageBubbles.ALERT);
                    }
                    room.sendComposer(new ModToolIssueHandledComposer(message).compose());
                    return true;
                }
            }
        }
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("eventtools.error.cmd_explain.missing_post.it"), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
