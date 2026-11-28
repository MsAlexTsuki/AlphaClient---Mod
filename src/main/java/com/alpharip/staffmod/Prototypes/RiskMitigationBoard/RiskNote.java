package com.alpharip.staffmod.RiskMitigationBoard;
/** Timestamped note attached to a risk record. */
public final class RiskNote {
    private final String id,author,text; private final long createdAt;
    public RiskNote(String id,String author,String text,long createdAt){
        if(id==null||id.trim().isEmpty()||text==null||text.trim().isEmpty())throw new IllegalArgumentException("Note ID and text are required");
        this.id=id;this.author=author==null?"":author.trim();this.text=text.trim();this.createdAt=Math.max(0L,createdAt);
    }
    public String getId(){return id;} public String getAuthor(){return author;}
    public String getText(){return text;} public long getCreatedAt(){return createdAt;}
}
