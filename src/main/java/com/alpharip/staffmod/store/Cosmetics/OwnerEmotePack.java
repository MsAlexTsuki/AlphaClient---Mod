package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Defines the three owner emote identifiers; animation rendering and access checks are external. */
public final class OwnerEmotePack {
 public static final String NAME="Owner Emote Pack",CATEGORY="OwnerFun",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private static final List<String> EMOTES=Collections.unmodifiableList(Arrays.asList("crown_salute","alpha_wave","royal_pose"));
 public List<String> emotes(){return EMOTES;}
 public boolean contains(String id){return EMOTES.contains(id);}
}