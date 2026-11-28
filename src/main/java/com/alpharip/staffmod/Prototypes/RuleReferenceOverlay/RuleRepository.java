package com.alpharip.staffmod.RuleReferenceOverlay;
import java.util.*;
public interface RuleRepository { List<RuleEntry> getAll(); RuleEntry findById(String id); }