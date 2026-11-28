package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Stores a player identifier and externally calculated risk indicators. */
public class PlayerRiskProfile {
    private final String playerId;
    private double riskScore;
    private final Map<String, String> indicators = new HashMap<String, String>();

    public PlayerRiskProfile(String playerId) {
        if (playerId == null || playerId.trim().isEmpty()) {
            throw new IllegalArgumentException("playerId cannot be blank");
        }
        this.playerId = playerId.trim();
    }

    public String getPlayerId() {
        return playerId;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        if (Double.isNaN(riskScore) || Double.isInfinite(riskScore)) {
            throw new IllegalArgumentException("riskScore must be finite");
        }
        this.riskScore = Math.max(0.0D, Math.min(1.0D, riskScore));
    }

    public void setIndicator(String name, String value) {
        if (name != null && !name.trim().isEmpty() && value != null) {
            indicators.put(name.trim(), value);
        }
    }

    public Map<String, String> getIndicators() {
        return Collections.unmodifiableMap(new HashMap<String, String>(indicators));
    }
}
