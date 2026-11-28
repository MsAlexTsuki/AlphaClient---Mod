package com.alpharip.staffmod.InventorySearch;
import net.minecraft.item.ItemStack;
import java.util.Locale;
/** Case-insensitive matching of all search words against an item's visible name. */
public final class InventorySearchMatcher {
 private InventorySearchMatcher(){}
 public static boolean matches(ItemStack stack,String query){
  if(stack==null)return false;
  if(query==null||query.trim().isEmpty())return true;
  String name=stack.getDisplayName().toLowerCase(Locale.ROOT);
  for(String word:query.trim().toLowerCase(Locale.ROOT).split("\\s+"))if(!name.contains(word))return false;
  return true;
 }
}