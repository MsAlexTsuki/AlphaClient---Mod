package com.alpharip.staffmod.staffops.ideas;

/** Foundation for text-to-speech chat preferences and queueing. */
public class TextToSpeechChat {
 private boolean enabled; private boolean readOwnMessages; private boolean readSystemMessages;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public boolean isReadOwnMessages(){return readOwnMessages;} public void setReadOwnMessages(boolean v){readOwnMessages=v;}
 public boolean isReadSystemMessages(){return readSystemMessages;} public void setReadSystemMessages(boolean v){readSystemMessages=v;}
}
