package com.alpharip.staffmod.AutoLoadingServerProfiles.matching;

import com.alpharip.staffmod.AutoLoadingServerProfiles.model.ServerProfileBinding;
import com.alpharip.staffmod.AutoLoadingServerProfiles.model.ServerProfileMatch;
import java.util.Collection;

/** Selects the most specific enabled exact-address or parent-domain binding. */
public final class ServerProfileMatcher {
    public ServerProfileMatch findBest(String connectedAddress, Collection<ServerProfileBinding> bindings) {
        if (connectedAddress == null || connectedAddress.trim().isEmpty() || bindings == null) return null;

        ServerProfileBinding best = null;
        int bestSpecificity = -1;
        boolean bestExact = false;
        String connected = ServerAddressNormalizer.normalize(connectedAddress);

        for (ServerProfileBinding binding : bindings) {
            if (binding == null || !binding.isEnabled()) continue;
            String configured = binding.getServerAddress();
            if (!ServerAddressNormalizer.matches(configured, connected)) continue;

            boolean exact = configured.equals(connected);
            int specificity = ServerAddressNormalizer.specificity(configured);
            if (best == null || (exact && !bestExact)
                    || (exact == bestExact && specificity > bestSpecificity)) {
                best = binding;
                bestSpecificity = specificity;
                bestExact = exact;
            }
        }

        if (best == null) return null;
        return new ServerProfileMatch(best, bestExact
                ? ServerProfileMatch.Type.EXACT : ServerProfileMatch.Type.DOMAIN);
    }
}
