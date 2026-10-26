package com.alpharip.staffmod.staffops.ideas;

/** Foundation for recording signed-update verification results; cryptographic verification must be wired to a trusted key. */
public class SignedUpdateVerification {
 private String releaseId="", signature="", algorithm=""; private boolean verified; private long verifiedAt;
 public String getReleaseId(){return releaseId;} public void setReleaseId(String v){releaseId=v==null?"":v;}
 public String getSignature(){return signature;} public void setSignature(String v){signature=v==null?"":v;}
 public String getAlgorithm(){return algorithm;} public void setAlgorithm(String v){algorithm=v==null?"":v;}
 public boolean isVerified(){return verified;} public long getVerifiedAt(){return verifiedAt;}
 public void recordResult(boolean valid,long timestamp){verified=valid;verifiedAt=timestamp;}
 public void clearResult(){verified=false;verifiedAt=0L;}
}
