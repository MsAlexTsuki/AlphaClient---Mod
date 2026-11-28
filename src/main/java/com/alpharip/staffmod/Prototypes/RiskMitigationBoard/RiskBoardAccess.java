package com.alpharip.staffmod.RiskMitigationBoard;
/**
 * Authorization adapter boundary. Production implementation must delegate to
 * the established StaffMods access checks; this prototype grants no permissions.
 */
public interface RiskBoardAccess { boolean canView(); boolean canManage(); }
