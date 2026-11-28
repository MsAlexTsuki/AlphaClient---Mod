package com.alpharip.staffmod.InventorySearch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import java.lang.reflect.Field;
/** Adds a clickable search box and client-only highlights to supported containers. */
public final class InventorySearchMod {
 private static final Minecraft MC=Minecraft.getMinecraft();private static GuiScreen owner;private static GuiTextField field;private static String query="";
 private static boolean supports(GuiScreen g){if(!(g instanceof GuiContainer))return false;GuiContainer c=(GuiContainer)g;return InventorySearchInventory.supports(c)||(g instanceof GuiChest&&(InventorySearchChest.supports((GuiChest)g)||InventorySearchEnderChest.supports((GuiChest)g)));}
 private static void init(GuiScreen g){if(owner==g&&field!=null)return;owner=g;field=new GuiTextField(9231,MC.fontRendererObj,Math.max(4,g.width/2-100),5,200,18);field.setMaxStringLength(64);field.setText(query);}
 @SubscribeEvent public void onInit(GuiScreenEvent.InitGuiEvent.Post e){if(InventorySearchSettings.isEnabled()&&supports(e.gui))init(e.gui);else if(owner!=e.gui){owner=null;field=null;}}
 @SubscribeEvent public void onMouse(GuiScreenEvent.MouseInputEvent.Pre e){if(!InventorySearchSettings.isEnabled()||!supports(e.gui))return;init(e.gui);if(!Mouse.getEventButtonState())return;int x=Mouse.getEventX()*e.gui.width/MC.displayWidth,y=e.gui.height-Mouse.getEventY()*e.gui.height/MC.displayHeight-1;if(x>=field.xPosition&&x<field.xPosition+field.width&&y>=field.yPosition&&y<field.yPosition+field.height){field.mouseClicked(x,y,Mouse.getEventButton());e.setCanceled(true);}else field.setFocused(false);}
 @SubscribeEvent public void onKey(GuiScreenEvent.KeyboardInputEvent.Pre e){if(!InventorySearchSettings.isEnabled()||owner!=e.gui||field==null||!field.isFocused())return;if(Keyboard.getEventKey()==Keyboard.KEY_ESCAPE){field.setFocused(false);e.setCanceled(true);return;}field.textboxKeyTyped(Keyboard.getEventCharacter(),Keyboard.getEventKey());query=field.getText();e.setCanceled(true);}
 @SubscribeEvent public void onDraw(GuiScreenEvent.DrawScreenEvent.Post e){if(!InventorySearchSettings.isEnabled()||!supports(e.gui))return;init(e.gui);GuiContainer g=(GuiContainer)e.gui;int left=read(g,"guiLeft"),top=read(g,"guiTop"),hits=0,total=0;for(Object o:g.inventorySlots.inventorySlots)if(o instanceof Slot){Slot s=(Slot)o;if(!s.getHasStack())continue;total++;if(g.getSlotUnderMouse()==s)continue;boolean ok=InventorySearchMatcher.matches(s.getStack(),field.getText());if(ok)hits++;int x=left+s.xDisplayPosition,y=top+s.yDisplayPosition;Gui.drawRect(x,y,x+16,y+16,ok?0x5533FF55:0x99000000);}field.drawTextBox();if(field.getText().isEmpty())MC.fontRendererObj.drawStringWithShadow("Search items...",field.xPosition+5,field.yPosition+5,0xFFAAAAAA);MC.fontRendererObj.drawStringWithShadow(hits+"/"+total,field.xPosition+field.width+5,field.yPosition+5,0xFFFFFFFF);}
 private static int read(GuiContainer g,String n){try{Field f=GuiContainer.class.getDeclaredField(n);f.setAccessible(true);return f.getInt(g);}catch(Exception ex){return 0;}}
}