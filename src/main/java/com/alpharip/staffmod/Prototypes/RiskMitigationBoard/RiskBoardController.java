package com.alpharip.staffmod.RiskMitigationBoard;
/** Access-checked facade for UI actions and local persistence. */
public final class RiskBoardController {
    private final RiskBoardAccess access;private final RiskBoard board;private final RiskBoardService service;private final java.io.File file;
    public RiskBoardController(RiskBoardAccess access,RiskBoard board,java.io.File file){
        if(access==null||board==null)throw new IllegalArgumentException("Access and board are required");
        this.access=access;this.board=board;this.service=new RiskBoardService(board);this.file=file;
    }
    public boolean load(){view();return RiskBoardStorage.load(file,board);}
    public boolean save(){manage();return RiskBoardStorage.save(file,board);}
    public RiskBoard getBoard(){view();return board;} public RiskBoardService getService(){manage();return service;}
    private void view(){if(!access.canView())throw new SecurityException("Staff access required");}
    private void manage(){if(!access.canManage())throw new SecurityException("Staff management access required");}
}
