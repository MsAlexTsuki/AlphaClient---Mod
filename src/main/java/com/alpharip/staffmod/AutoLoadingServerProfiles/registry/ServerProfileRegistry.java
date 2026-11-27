package com.alpharip.staffmod.AutoLoadingServerProfiles.registry;

import com.alpharip.staffmod.AutoLoadingServerProfiles.matching.ServerAddressNormalizer;
import com.alpharip.staffmod.AutoLoadingServerProfiles.model.ServerProfileBinding;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory registry for server-to-profile assignments. */
public final class ServerProfileRegistry {
    private final Map<String, ServerProfileBinding> bindings = new LinkedHashMap<String, ServerProfileBinding>();

    public synchronized void put(ServerProfileBinding binding) {
        if (binding == null) throw new IllegalArgumentException("Binding is required");
        bindings.put(binding.getServerAddress(), binding);
    }

    public synchronized boolean remove(String serverAddress) {
        return bindings.remove(ServerAddressNormalizer.normalize(serverAddress)) != null;
    }

    public synchronized ServerProfileBinding get(String serverAddress) {
        return bindings.get(ServerAddressNormalizer.normalize(serverAddress));
    }

    public synchronized List<ServerProfileBinding> getAll() {
        return Collections.unmodifiableList(new ArrayList<ServerProfileBinding>(bindings.values()));
    }

    public synchronized void replaceAll(Collection<ServerProfileBinding> values) {
        bindings.clear();
        if (values == null) return;
        for (ServerProfileBinding binding : values) put(binding);
    }

    public synchronized int size() { return bindings.size(); }
}
