package com.alpharip.staffmod.BuildTimelapse.camera;
/** Implement with a client-only render camera. Never mutate player position, motion or input. */
public interface CameraRuntimePort {
    void activate(CameraPose pose) throws Exception;
    void apply(CameraPose pose) throws Exception;
    void deactivate() throws Exception;
    boolean isAvailable();
    /** Initial independent-camera pose; implementations may snapshot the current player view. */
    default CameraPose getInitialPose() { return CameraPose.origin(); }
}
