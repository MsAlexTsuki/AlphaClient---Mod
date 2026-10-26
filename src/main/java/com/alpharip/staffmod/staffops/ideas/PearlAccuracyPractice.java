package com.alpharip.staffmod.staffops.ideas;

/** Foundation for ender-pearl accuracy practice tracking. */
public class PearlAccuracyPractice {
 private int throwsRecorded; private int successfulLandings;
 public void recordThrow(){throwsRecorded++;} public void recordLanding(boolean accurate){if(accurate)successfulLandings++;} public int getThrowsRecorded(){return throwsRecorded;} public int getSuccessfulLandings(){return successfulLandings;} public double getAccuracy(){return throwsRecorded==0?0.0:(double)successfulLandings/throwsRecorded;}
}
