package com.alpharip.staffmod.developer;

import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Small opt-in on-screen diagnostic overlay for authorized developer ranks. */
public final class DeveloperDebugOverlay {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        if (!DeveloperDiagnostics.isDebugOverlayEnabled()) return;
        if (!hasAccess()) {
            DeveloperDiagnostics.setDebugOverlayEnabled(false);
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.fontRendererObj == null) return;
        int x = 6;
        int y = 6;
        mc.fontRendererObj.drawStringWithShadow("ALPHA DEBUG", x, y, 0xFFFFD866);
        y += 11;
        mc.fontRendererObj.drawStringWithShadow("Rank: " + PermissionService.getEffectiveRank().name(), x, y, 0xFFE7EAF0);
        y += 10;
        mc.fontRendererObj.drawStringWithShadow("World: " + (mc.theWorld != null) + " | Player: " + (mc.thePlayer != null), x, y, 0xFFE7EAF0);
        y += 10;
        mc.fontRendererObj.drawStringWithShadow(DeveloperDiagnostics.profilerOverlayLine(), x, y, 0xFFE7EAF0);
        y += 10;
        mc.fontRendererObj.drawStringWithShadow("Test: " + DeveloperDiagnostics.isTestMode()
                + " | Sandbox: " + DeveloperDiagnostics.isSandboxMode(), x, y, 0xFFE7EAF0);
    }

    private boolean hasAccess() {
        return PermissionService.hasAccess(PermissionService.Rank.DEVELOPER)
                || PermissionService.hasAccess(PermissionService.Rank.OWNER)
                || PermissionService.hasAccess(PermissionService.Rank.FOUNDER);
    }
}