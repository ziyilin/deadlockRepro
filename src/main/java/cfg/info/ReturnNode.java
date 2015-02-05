package cfg.info;

import soot.Unit;

public class ReturnNode extends StmtNode{

	public ReturnNode(Unit u) {
		super(u, Type.Return);
	
	}
	
	public String toString(){
		return type+":"+stmt.toString();
	}
}
