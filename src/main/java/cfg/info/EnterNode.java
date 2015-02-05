package cfg.info;

import soot.SootMethod;
public class EnterNode extends CFGNode{
	private SootMethod enterMethod;
	public EnterNode(SootMethod m) {
		super(Type.Enter);
		setEnterMethod(m);
	}
	public SootMethod getEnterMethod() {
		return enterMethod;
	}
	public void setEnterMethod(SootMethod enterMethod) {
		this.enterMethod = enterMethod;
	}
	public String toString(){
		return type+":"+enterMethod.getSignature();
	}
}
