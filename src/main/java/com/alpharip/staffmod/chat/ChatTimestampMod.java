package com.alpharip.staffmod.chat;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Prefixes received chat with local HH:mm:ss time, avoiding duplicate prefixes. */
public final class ChatTimestampMod {
    private final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss ");

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || event.message == null) return;
        AlphaModule module = AlphaModuleRegistry.find("chat-timestamp");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        String message = event.message.getFormattedText();
        if (message.matches("^\\u00a78?\\[\\d{2}:\\d{2}:\\d{2}\\].*")) return;
        event.message = new ChatComponentText("§8[" + format.format(new Date()) + "] §r" + message);
    }
}
