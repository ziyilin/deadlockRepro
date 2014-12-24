package cfg;

import soot.Unit;

public class NormalNode extends StmtNode{

	public NormalNode(Unit u) {
		super(u, Type.Normal);

	}
	
	public String toString(){
		return type+":"+stmt.toString();
	}
}
