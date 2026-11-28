package com.alpharip.staffmod.RiskMitigationBoard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/** In-memory register; persistence is handled by RiskBoardStorage. */
public final class RiskBoard {
    private final Map<String,RiskRecord> records=new LinkedHashMap<String,RiskRecord>();
    public synchronized void add(RiskRecord r){if(r==null)throw new IllegalArgumentException("Risk is required");if(records.containsKey(r.getId()))throw new IllegalArgumentException("Risk ID already exists");records.put(r.getId(),r);}
    public synchronized RiskRecord get(String id){return records.get(id);}
    public synchronized boolean remove(String id){return records.remove(id)!=null;}
    public synchronized List<RiskRecord> getAll(){return Collections.unmodifiableList(new ArrayList<RiskRecord>(records.values()));}
    public synchronized int size(){return records.size();}
    public synchronized void replaceAll(List<RiskRecord> values){records.clear();if(values!=null)for(RiskRecord r:values)if(r!=null&&!records.containsKey(r.getId()))records.put(r.getId(),r);}
}
