package ibx.utility.client;

import ibx.utility.client.screen.QuestBoardScreen;
import ibx.utility.network.QuestActionPayload;
import ibx.utility.network.QuestBoardClosedPayload;
import ibx.utility.network.QuestBoardPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

/** 客户端:服务端最近发来的悬赏板内容;收到 OPEN 就打开界面,REFRESH 刷新开着的界面,CLOSE 关掉(出发了)。关界面时告诉服务端不用再发。 */
public final class ClientQuestBoard {
    private static QuestBoardPayload current = new QuestBoardPayload(QuestBoardPayload.Mode.REFRESH, List.of(), List.of(), List.of());

    private ClientQuestBoard() {}

    public static QuestBoardPayload current() {
        return current;
    }

    public static void accept(QuestBoardPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (payload.mode() == QuestBoardPayload.Mode.CLOSE) {
            if (minecraft.screen instanceof QuestBoardScreen) {
                minecraft.setScreen(null);
            }
            return;
        }
        current = payload;
        if (minecraft.screen instanceof QuestBoardScreen screen) {
            screen.refresh();
        } else if (payload.mode() == QuestBoardPayload.Mode.OPEN) {
            minecraft.setScreen(new QuestBoardScreen());
        }
    }

    public static void send(QuestActionPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    /** 悬赏板界面关掉了(还连着服务器时才发) */
    public static void closed() {
        if (Minecraft.getInstance().getConnection() != null) {
            ClientPacketDistributor.sendToServer(QuestBoardClosedPayload.INSTANCE);
        }
    }
}
