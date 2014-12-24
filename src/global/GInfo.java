package global;




public class GInfo {
	static private GInfo g = new GInfo();
	public static GInfo v(){
		return g;
	}
	public String confPrefix="";
	public String dumpFile="";
}
