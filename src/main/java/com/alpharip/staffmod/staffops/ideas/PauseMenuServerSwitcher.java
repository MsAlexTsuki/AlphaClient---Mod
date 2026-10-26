package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Server switcher choices for a pause menu. Joining/disconnecting and menu button wiring are not connected. */
public class PauseMenuServerSwitcher {
 public static final class Server {public final String name,address;Server(String n,String a){name=n;address=a;}}
 private final LinkedHashMap<String,Server> servers=new LinkedHashMap<String,Server>();
 public void add(String name,String address){if(name==null||name.trim().isEmpty()||address==null||address.trim().isEmpty())throw new IllegalArgumentException("Name and address required");servers.put(address.trim().toLowerCase(Locale.ROOT),new Server(name.trim(),address.trim()));}
 public boolean remove(String address){return servers.remove(address.trim().toLowerCase(Locale.ROOT))!=null;}
 public List<Server> list(){return Collections.unmodifiableList(new ArrayList<Server>(servers.values()));}
}