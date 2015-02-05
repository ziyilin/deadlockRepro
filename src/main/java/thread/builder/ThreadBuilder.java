package thread.builder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import thread.info.ObjectInfo;
import thread.info.SiteInfo;
import thread.info.ThreadInfo;

public class ThreadBuilder {
	
	public static Map<Integer,ThreadInfo> apply(String dumpfile){
		String content=read(dumpfile);//;read("dump.txt");//"src\\edu\\sjtu\\stap\\checkmate\\sample\\fulldump"
		Integer begin=content.indexOf("Java stack information for the threads listed above")+1;
//		Integer end=content.indexOf("Found 1 deadlock");
		Integer end=content.length();
		String javaStackInfo=content.subSequence(begin, end).toString();
//		System.out.println(javaStackInfo);
		
		// 找出死锁涉及的所有线程的名称
		Map<Integer,ThreadInfo> threadPosToInfo=new HashMap<Integer,ThreadInfo>();
		Map<Integer,String> threadPosToName=new HashMap<Integer,String>();
		Integer stackInfoLength=javaStackInfo.length();
		for(Integer i=0;i<stackInfoLength;i=i+1){
			if(javaStackInfo.charAt(i)=='"'){
				i=i+1;
				Integer threadPos=i;
				String threadName="";
				while(javaStackInfo.charAt(i)!='"'){
					threadName=threadName+javaStackInfo.charAt(i);
					i=i+1;
				}
				threadPosToName.put(threadPos, threadName);
			}
		}

		// 找出每个线程对应的执行路径
		for(Integer threadPos:threadPosToName.keySet()){
			Integer threadBegin=threadPos;
			String threadName=threadPosToName.get(threadPos);
			ThreadInfo tInfo=new ThreadInfo(threadName);
			threadPosToInfo.put(threadPos, tInfo);
			
			// 把光标移动到路径开始点
			Integer index=threadBegin;
			while(javaStackInfo.charAt(index)!=':'){
				index=index+1;
			}
			index=index+1;
			
			// 获取路径（连续方法调用点组成的字符串数组）
			String path="";
			while(javaStackInfo.charAt(index)!='"' && index<javaStackInfo.length()-1){
				path=path+javaStackInfo.charAt(index);
				index=index+1;
			}
			
			// 获取各个方法调用点
			String[] site=path.split("at ");
			for(Integer j=1;j<site.length;j=j+1){//从1号下标开始，0号下标数据无意义
				String element=site[j].trim();
				
				if(element.contains("-")){
					String[] eleAry=element.split("-");
					SiteInfo newSite=new SiteInfo(eleAry[0].trim());
					tInfo.pushSite(newSite);
					for(Integer lockIndex=1;lockIndex<eleAry.length;lockIndex++){//从1号下标开始，0号下标是site
						String detail=eleAry[lockIndex].trim();
						String objectAddress="";
						Integer k=0;
						while(detail.charAt(k)!='<'){
							k=k+1;
						}
						k=k+1;
						while(detail.charAt(k)!='>'){
							objectAddress=objectAddress+detail.charAt(k);
							k=k+1;
						}
						ObjectInfo.State s = null;
						if(detail.contains("locked")&&detail.indexOf("locked")<k)
							s=ObjectInfo.State.LOCKED;
						else if(detail.contains("waiting")&&detail.indexOf("waiting")<k)
							s=ObjectInfo.State.WAITING;
						
						while(detail.charAt(k)!='('){
							k=k+1;
						}
						k=k+3;
						String objectType="";
						while(detail.charAt(k)!=')'){
							objectType=objectType+detail.charAt(k);
							k=k+1;
						}
						
						newSite.pushObjectReference(new ObjectInfo(objectType,objectAddress,s));
					}
				}else{
					tInfo.pushSite(new SiteInfo(element));
				}
				
			}
			
//			System.out.println(new ArrayList<String>(Arrays.asList(site)));
			
			
		}
		
		
		for(Integer threadPos:threadPosToInfo.keySet()){
			System.out.println(threadPosToInfo.get(threadPos));
		}
		
		return threadPosToInfo;
	}
		
	public static String read(String file){
		
		BufferedReader br = null;
		String ret = null;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	    try {
			StringBuilder sb = new StringBuilder();
			String line = null;
			line = br.readLine();
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
	        ret = sb.toString();
	        br.close();
	    }catch(IOException e) {
	    	e.printStackTrace();
	    }
	    return ret;
	}
}
