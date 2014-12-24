package thread.info;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import cfg.CFG;

/** 
 * Class ThreadInfo constructs a name of thread and a list of site information.
 *
 * @author Derrick
 *
 * @since Dec 8, 2014
 */
public class ThreadInfo {
	private String threadName;
	private List<Site> site;
	private CFG cfg;
	public ThreadInfo(String name){
		setThreadName(name);
		site=new LinkedList<Site>();
		setCfg(new CFG());
	}
	public String toString(){
		String result="";
		result+="\""+threadName+"\":\n";
		for(Site s:site){
			result+=s.toString();
			result+="\n";
			Stack<ObjectInfo> objectStack=s.getObjectReference();
			if (objectStack != null) {
				for (ObjectInfo o : objectStack) {
					result += o.toString();
					result += "\n";
				}
			}
		}
		return result;
	}
	public String getThreadName() {
		return threadName;
	}
	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}
	public void pushSite(Site s){
		site.add(s);
	}
	public List<Site> getSite(){
		return site;
	}
	public CFG getCfg() {
		return cfg;
	}
	public void setCfg(CFG cfg) {
		this.cfg = cfg;
	}
}