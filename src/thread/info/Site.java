package thread.info;

import java.util.Stack;
import soot.SootMethod;
import soot.toolkits.graph.UnitGraph;
/** 
 * 
 * Class Site constructs a call site in form of <package>.<class>.<method>(<class>.java:<#line>)
 * 
 * The class Site also contains a set of object where current thread is waiting for or locked on  
 * 
 * @author Derrick
 *
 * @since Dec 8, 2014
 */
public class Site {
	private String packageName;
	private String className;
	private String methodName;
	private Integer lineNumber;
	private SootMethod method;
	private UnitGraph unitGraph;
	private Stack<ObjectInfo> objectReference;
	
	public Site(String element) {
		
		Integer index=0;
		String siteInfo="";
		while(element.charAt(index)!='('){
			siteInfo+=element.charAt(index);
			index++;
		}
		index=index+1;
		String position="";
		while(element.charAt(index)!=')'){
			position+=element.charAt(index);
			index=index+1;
		}
		String[] methodFullName = siteInfo.split("\\.");
		Integer metFullNameLen=methodFullName.length;
		setMethodName(methodFullName[metFullNameLen - 1].trim());
		setClassName(methodFullName[metFullNameLen - 2].trim());
		String packageName=methodFullName[0];
		for(int i = 1;i <= metFullNameLen-3; i = i + 1){
			packageName+="."+methodFullName[i];
		}
		setPackageName(packageName);
		
		if(position.contains(":")){
			String[] posAry=position.split(":");
			String[] javaFile=posAry[0].trim().split("\\.");
			setClassName(javaFile[0].trim());
			setLineNumber(Integer.parseInt(posAry[1].trim()));
		}
	}
	public String toString(){
		String result = "\tat ";
		result+=getPackageName();
		result+=".";		
		result+=getClassName();
		result+=".";
		result+=getMethodName();
		if(lineNumber==null){
			result+="(Unknown Source)";
		}
		else{
			result+="("+className+".java:"+lineNumber+")";
		}
		return result;
	}

	public Stack<ObjectInfo> getObjectReference() {
		return objectReference;
	}
	public void pushObjectReference(ObjectInfo objectInfo) {
		if(objectReference==null)
			this.objectReference=new Stack<ObjectInfo>();
		this.objectReference.push(objectInfo);
	}
	public void setClassName(String cn){
		this.className=cn;
	}
	public String getClassName(){
		return this.className;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public Integer getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(Integer lineNumber) {
		this.lineNumber = lineNumber;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public SootMethod getMethod() {
		return method;
	}
	public void setMethod(SootMethod method) {
		this.method = method;
	}
	public UnitGraph getUnitGraph() {
		return unitGraph;
	}
	public void setUnitGraph(UnitGraph ug) {
		this.unitGraph = ug;
	}
	



}
