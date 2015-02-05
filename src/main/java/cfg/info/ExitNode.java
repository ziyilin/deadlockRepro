package cfg.info;
import soot.SootMethod;
public class ExitNode extends CFGNode{
	private SootMethod exitMethod;
	public ExitNode(SootMethod m) {
		super(Type.Exit);
		setExitMethod(m);
	}
	public SootMethod getExitMethod() {
		return exitMethod;
	}
	public void setExitMethod(SootMethod exitMethod) {
		this.exitMethod = exitMethod;
	}
	public String toString(){
		return type+":"+exitMethod.getSignature();
	}
}
