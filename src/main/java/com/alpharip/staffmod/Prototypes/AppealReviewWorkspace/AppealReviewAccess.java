package com.alpharip.staffmod.AppealReviewWorkspace;
/** Authorization boundary only; this interface grants no rank or permission. */
public interface AppealReviewAccess {
 boolean canViewAppeals();
 boolean canReviewAppeals();
 boolean canAssignReviewers();
 boolean canEscalateAppeals();
 boolean canManageReopenRequests();
}