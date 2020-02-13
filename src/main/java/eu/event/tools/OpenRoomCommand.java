package main.java.eu.event.tools;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.RoomState;

public class OpenRoomCommand extends Command {
    public OpenRoomCommand() {
        super("cmd_open_room", Emulator.getTexts().getValue("eventtools.keys.cmd_open_room").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] strings) throws Exception {
        Room room = gameClient.getHabbo().getHabboInfo().getCurrentRoom();
        if(room != null) {
            if(room.getState() != RoomState.OPEN) {
                room.setState(RoomState.OPEN);
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("eventtools.sucess.cmd_open_room"), RoomChatMessageBubbles.ALERT);
                return true;
            }
            else{
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("eventtools.error.cmd_open_room"), RoomChatMessageBubbles.ALERT);
            }
        }
        return false;
    }
}
