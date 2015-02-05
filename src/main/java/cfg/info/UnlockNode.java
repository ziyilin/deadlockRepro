package cfg.info;

import soot.Unit;

public class UnlockNode extends StmtNode{
	public UnlockNode(Unit u){
		super(u, Type.Unlock);
	}
}
