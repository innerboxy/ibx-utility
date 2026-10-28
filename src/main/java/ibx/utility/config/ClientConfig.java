package ibx.utility.config;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.TranslatableEnum;

import java.util.Locale;

/** 客户端配置(模组列表里的「配置」按钮能改)。 */
public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** 左上角的任务追踪栏怎么显示 */
    public enum TrackerMode implements TranslatableEnum {
        /** 任务名 + 每个目标一行 */
        TEXT,
        /** 一个任务一行:目标图标 + 数字 */
        ICONS,
        /** 同上,前面再加任务图标 */
        ICONS_WITH_QUEST,
        HIDDEN;

        @Override
        public Component getTranslatedName() {
            return Component.translatable("ibx_utility.configuration.trackerMode." + name().toLowerCase(Locale.ROOT));
        }
    }

    public static final ModConfigSpec.EnumValue<TrackerMode> TRACKER_MODE = BUILDER
            .comment("左上角任务追踪栏的样式:文字 / 图标 / 图标带任务 / 不显示(默认)。",
                    "Quest tracker in the top-left corner: TEXT, ICONS, ICONS_WITH_QUEST or HIDDEN (default).")
            .translation("ibx_utility.configuration.trackerMode")
            .defineEnum("trackerMode", TrackerMode.HIDDEN);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
