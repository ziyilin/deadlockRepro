package cfg.info;

import soot.Unit;

public abstract class StmtNode extends CFGNode{
	protected Unit stmt;
	public StmtNode(Unit u, Type t) {
		super(t);
		setStmt(u);
	}
	public Unit getStmt() {
		return stmt;
	}
	public void setStmt(Unit stmt) {
		this.stmt = stmt;
	}
	public String toString(){
		return type+":"+stmt.toString();
	}
}
