package com.alpharip.staffmod.AppealReviewWorkspace;
import java.util.UUID;
public final class AppealReviewNote {
 private final String id,author,text;private final long createdAt;
 public AppealReviewNote(String author,String text,long at){if(text==null||text.trim().isEmpty()||at<=0)throw new IllegalArgumentException("Note and timestamp required");id=UUID.randomUUID().toString();this.author=author==null?"":author.trim();this.text=text.trim();createdAt=at;}
 public String getId(){return id;}public String getAuthor(){return author;}public String getText(){return text;}public long getCreatedAt(){return createdAt;}
}