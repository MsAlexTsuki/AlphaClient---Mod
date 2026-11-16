package com.alpharip.staffmod.caseescalationtimer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Emits one local event when a case first enters a notification state. */
public final class EscalationNotificationManager {
    private final Map<String, EscalationNotificationType> lastStates = new HashMap<String, EscalationNotificationType>();

    public synchronized List<EscalationNotification> collect(List<EscalationCase> cases,
                                                               EscalationPolicy policy, long now) {
        List<EscalationNotification> result = new ArrayList<EscalationNotification>();
        if (cases == null) return result;
        for (EscalationCase item : cases) {
            EscalationStatus status = EscalationFormatter.status(item, policy, now);
            EscalationNotificationType type = toType(status);
            EscalationNotificationType previous = lastStates.get(item.getId());
            if (type != null && type != previous) {
                result.add(new EscalationNotification(item.getId(), item.getTitle(), type, now));
                lastStates.put(item.getId(), type);
            } else if (type == null) {
                lastStates.remove(item.getId());
            }
        }
        return result;
    }

    public synchronized void reset() { lastStates.clear(); }

    private EscalationNotificationType toType(EscalationStatus status) {
        if (status == EscalationStatus.WARNING) return EscalationNotificationType.WARNING;
        if (status == EscalationStatus.DUE) return EscalationNotificationType.DEADLINE_REACHED;
        if (status == EscalationStatus.OVERDUE) return EscalationNotificationType.OVERDUE;
        return null;
    }
}
