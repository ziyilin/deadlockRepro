package thread.info;
public class ObjectInfo {
	public enum State{LOCKED, WAITING};
	private String objectType;
	private String objectAddr;
	private State objectState;
	public ObjectInfo(String type, String objectAddress, State s) {
		setObjectAddr(objectAddress);
		setObjectState(s);
		setObjectType(type);
	}	
	public String toString(){
		String stateDesc="\t - ";
		if(objectState==State.LOCKED)
			stateDesc+="locked";
		else
			stateDesc+="waiting to lock";
		return stateDesc+" <"+objectAddr+"> (a "+objectType+")";
	}
	
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	public String getObjectAddr() {
		return objectAddr;
	}
	public void setObjectAddr(String objectAddr) {
		this.objectAddr = objectAddr;
	}
	public State getObjectState() {
		return objectState;
	}
	public void setObjectState(State objectState) {
		this.objectState = objectState;
	}
	
}
