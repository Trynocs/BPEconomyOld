package com.trynocs.bPEconomy.jobs;

import java.util.UUID;

public class PlayerJob {
    private final UUID playerUUID;
    private String jobName;
    // Potentially add experience, level, etc. later

    public PlayerJob(UUID playerUUID, String jobName) {
        this.playerUUID = playerUUID;
        this.jobName = jobName;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
}
