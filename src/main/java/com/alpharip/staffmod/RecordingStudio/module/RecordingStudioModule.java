package com.alpharip.staffmod.RecordingStudio.module;

import java.util.List;

/**
 * Optional adapter implemented by a feature module that can provide a recording workflow.
 * Recording Studio depends only on this contract, never on a concrete feature module.
 */
public interface RecordingStudioModule {
    String getId();
    String getDisplayName();
    boolean isAvailable();
    List<String> getOptions();
    void prepare(String optionId) throws Exception;
    void onRecordingStart(long now) throws Exception;
    void onRecordingPause(long now) throws Exception;
    void onRecordingResume(long now) throws Exception;
    void onRecordingStop() throws Exception;
    String getStatus();
}
