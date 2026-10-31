package com.alpharip.staffmod.hud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Standalone client-side utility; requires later module/event registration. */
public final class StaffArmorStatus {
 private boolean enabled(){AlphaModule m=AlphaModuleRegistry.find("staff-armor-status");return m!=null&&m.isEnabled()&&m.isUnlocked();}
 @SubscribeEvent public void render(RenderGameOverlayEvent.Text e){if(e.type!=RenderGameOverlayEvent.ElementType.TEXT||!enabled())return;Minecraft m=Minecraft.getMinecraft();if(m.thePlayer==null||m.fontRendererObj==null||m.gameSettings.showDebugInfo)return;int y=6;m.fontRendererObj.drawStringWithShadow("Armor Status",6,y,-1);y+=12;for(int i=3;i>=0;i--){net.minecraft.item.ItemStack s=m.thePlayer.inventory.armorItemInSlot(i);String label=s==null?"empty":s.getDisplayName()+" ("+(s.getMaxDamage()-s.getItemDamage())+"/"+s.getMaxDamage()+")";m.fontRendererObj.drawStringWithShadow(label,6,y,0xFFCCCCCC);y+=10;}}
}