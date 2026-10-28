package ibx.utility.client;

import ibx.utility.client.screen.QuestBoardScreen;
import ibx.utility.network.QuestActionPayload;
import ibx.utility.network.QuestBoardPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

/** 客户端:服务端最近发来的悬赏板内容;收到 open 的就打开界面,否则刷新开着的界面。 */
public final class ClientQuestBoard {
    private static QuestBoardPayload current = new QuestBoardPayload(false, List.of(), List.of());

    private ClientQuestBoard() {}

    public static QuestBoardPayload current() {
        return current;
    }

    public static void accept(QuestBoardPayload payload) {
        current = payload;
        Minecraft minecraft = Minecraft.getInstance();
        if (payload.open()) {
            minecraft.setScreen(new QuestBoardScreen());
        } else if (minecraft.screen instanceof QuestBoardScreen screen) {
            screen.refresh();
        }
    }

    public static void send(QuestActionPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }
}
