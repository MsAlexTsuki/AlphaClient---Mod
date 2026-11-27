package com.alpharip.staffmod.AutoLoadingServerProfiles.storage;

import com.alpharip.staffmod.AutoLoadingServerProfiles.model.ServerProfileBinding;
import java.io.IOException;
import java.util.List;

/** Persistence boundary; implementations can be replaced without changing matching logic. */
public interface ServerProfileStore {
    List<ServerProfileBinding> load() throws IOException;
    void save(List<ServerProfileBinding> bindings) throws IOException;
}
