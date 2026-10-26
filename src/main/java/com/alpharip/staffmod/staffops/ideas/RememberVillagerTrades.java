package com.alpharip.staffmod.staffops.ideas;

/** Foundation for storing remembered villager trades. */
public class RememberVillagerTrades {
 private final java.util.Map<String,String> rememberedTrades = new java.util.LinkedHashMap<String,String>();
 public void remember(String villagerId, String tradeDescription) {
  if (villagerId == null || villagerId.trim().isEmpty() || tradeDescription == null) return;
  rememberedTrades.put(villagerId, tradeDescription);
 }
 public String getTrade(String villagerId) { return rememberedTrades.get(villagerId); }
 public boolean forget(String villagerId) { return rememberedTrades.remove(villagerId) != null; }
 public java.util.Map<String,String> getTrades() {
  return java.util.Collections.unmodifiableMap(rememberedTrades);
 }
}
