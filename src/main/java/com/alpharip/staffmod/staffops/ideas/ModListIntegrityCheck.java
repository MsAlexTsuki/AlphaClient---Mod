package com.alpharip.staffmod.staffops.ideas;

import java.util.*;

/** Compares mod identifiers supplied by a caller; it does not inspect Forge's runtime registry. */
public final class ModListIntegrityCheck {
    public static final String FEATURE_NAME="Mod List Integrity Check", CATEGORY="Security", STATUS="NEW";
    public static final class Result {
        public final Set<String> missing, unexpected;
        private Result(Set<String> m,Set<String> u){missing=Collections.unmodifiableSet(m);unexpected=Collections.unmodifiableSet(u);}
        public boolean isClean(){return missing.isEmpty()&&unexpected.isEmpty();}
    }
    public Result compare(Collection<String> expected, Collection<String> actual) {
        Set<String> e=normalized(expected),a=normalized(actual);
        Set<String> missing=new TreeSet<String>(e);missing.removeAll(a);
        Set<String> unexpected=new TreeSet<String>(a);unexpected.removeAll(e);
        return new Result(missing,unexpected);
    }
    private Set<String> normalized(Collection<String> values){Set<String>s=new TreeSet<String>();if(values!=null)for(String v:values)if(v!=null&&!v.trim().isEmpty())s.add(v.trim().toLowerCase(Locale.ROOT));return s;}
}