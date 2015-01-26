package cfg;

public abstract class CFGNode {
	public static enum Type {Normal, Call, Return, Enter, Exit, Lock, Unlock};
	protected String type;
/*	private Integer horizontalDistance;//x
	private Integer verticalDistance;//y
*/	public CFGNode(Type typeInference){
		switch (typeInference) {
		case Normal:
			setType("NORMAL");
			break;
		case Call:
			setType("CALL");
			break;
		case Return:
			setType("RETURN");
			break;
		case Enter:
			setType("ENTER");
			break;
		case Exit:
			setType("EXIT");
			break;
		case Lock:
			setType("LOCK");
			break;
		case Unlock:
			setType("UNLOCK");
			break;
		}
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Boolean equals(CFGNode node){
		Boolean result=false;
		if(node.getType().equals(this.getType())){
			if(type.equals("ENTER")){
				EnterNode eNode=(EnterNode)node;
				EnterNode tNode=(EnterNode)this;
				if(eNode.getEnterMethod().equals(tNode.getEnterMethod()))
					result=true;
			}
			else if(type.equals("EXIT")){
				ExitNode eNode=(ExitNode)node;
				ExitNode tNode=(ExitNode)this;
				if(eNode.getExitMethod().equals(tNode.getExitMethod()))
					result=true;
			}
			else{
				StmtNode sNode=(StmtNode)node;
				StmtNode tNode=(StmtNode)this;
				if(sNode.getStmt().equals(tNode.getStmt()))
					result=true;
			}
		}
		return result;
	}
/*	public Integer getHorizontalDistance() {
		return horizontalDistance;
	}
	public void setHorizontalDistance(Integer horizontalDistance) {
		this.horizontalDistance = horizontalDistance;
	}
	public Integer getVerticalDistance() {
		return verticalDistance;
	}
	public void setVerticalDistance(Integer verticalDistance) {
		this.verticalDistance = verticalDistance;
	}*/
}
