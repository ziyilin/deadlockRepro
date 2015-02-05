package cfg.info;

import soot.Unit;

public class LockNode extends StmtNode{
	public LockNode(Unit u){
		super(u,Type.Lock);
	}
}
