package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Stores local display copies of action receipts. This is not an authoritative audit source. */
public final class PrivilegedActionReceipts {
 public static final String NAME="Privileged Action Receipts", CATEGORY="Security", STATUS="NEW";
 public static final boolean REQUIRES_SUPABASE=true, REQUIRES_SERVER_SUPPORT=true;
 // TODO(Supabase): shared receipt storage is added by the owner later.
 // TODO(Server): authoritative receipts must originate from the server/plugin.
 private final List<String> receipts=new ArrayList<String>();
 public synchronized void recordLocal(String value){if(value==null||value.trim().isEmpty())throw new IllegalArgumentException("receipt");receipts.add(value.trim());}
 public synchronized List<String> snapshot(){return Collections.unmodifiableList(new ArrayList<String>(receipts));}
}