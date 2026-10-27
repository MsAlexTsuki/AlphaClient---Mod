package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Foundation for scrollable item tooltips.
 *
 * Keeps a bounded viewport over tooltip lines and supports mouse-wheel scrolling.
 * Minecraft tooltip-rendering/input hooks are still required to connect this model
 * to the in-game item tooltip.
 */
public final class ScrollableTooltips {
    public static final String FEATURE_NAME = "Scrollable Tooltips";
    public static final String DESCRIPTION =
            "Shows long item tooltips in a scrollable viewport instead of letting them grow off-screen.";

    private boolean enabled = true;
    private int maxVisibleLines = 8;
    private int scrollStep = 1;
    private int scrollOffset = 0;
    private List<String> tooltipLines = Collections.emptyList();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        enabled = value;
        if (!value) {
            scrollOffset = 0;
        }
    }

    public int getMaxVisibleLines() {
        return maxVisibleLines;
    }

    public void setMaxVisibleLines(int value) {
        maxVisibleLines = Math.max(1, Math.min(40, value));
        clampScrollOffset();
    }

    public int getScrollStep() {
        return scrollStep;
    }

    public void setScrollStep(int value) {
        scrollStep = Math.max(1, Math.min(10, value));
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    /** Set the lines for the currently hovered item tooltip. */
    public void setTooltipLines(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            tooltipLines = Collections.emptyList();
        } else {
            tooltipLines = Collections.unmodifiableList(new ArrayList<String>(lines));
        }
        scrollOffset = 0;
        clampScrollOffset();
    }

    public List<String> getTooltipLines() {
        return tooltipLines;
    }

    /** Returns the visible tooltip lines, preserving their original order. */
    public List<String> getVisibleLines() {
        if (!enabled || tooltipLines.size() <= maxVisibleLines) {
            return tooltipLines;
        }
        int end = Math.min(tooltipLines.size(), scrollOffset + maxVisibleLines);
        return Collections.unmodifiableList(
                new ArrayList<String>(tooltipLines.subList(scrollOffset, end)));
    }

    /** Scroll by mouse-wheel direction: positive moves down, negative moves up. */
    public void scroll(int wheelDirection) {
        if (!enabled || wheelDirection == 0 || tooltipLines.size() <= maxVisibleLines) {
            return;
        }
        int direction = wheelDirection > 0 ? 1 : -1;
        scrollOffset += direction * scrollStep;
        clampScrollOffset();
    }

    public boolean canScroll() {
        return enabled && tooltipLines.size() > maxVisibleLines;
    }

    public boolean canScrollUp() {
        return canScroll() && scrollOffset > 0;
    }

    public boolean canScrollDown() {
        return canScroll() && scrollOffset < tooltipLines.size() - maxVisibleLines;
    }

    public void resetScroll() {
        scrollOffset = 0;
    }

    private void clampScrollOffset() {
        int maxOffset = Math.max(0, tooltipLines.size() - maxVisibleLines);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));
    }
}
