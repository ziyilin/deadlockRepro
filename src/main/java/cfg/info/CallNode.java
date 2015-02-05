package cfg.info;

import soot.Unit;

public class CallNode extends StmtNode{
	public CallNode(Unit u) {
		super(u, Type.Call);
	
	}
	public String toString(){
		return type+":"+stmt.toString();
	}
}
