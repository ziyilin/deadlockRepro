package program.analysis;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import cfg.CFGNode;


import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.tagkit.LineNumberTag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;
import thread.builder.ThreadBuilder;
import thread.info.Site;
import thread.info.ThreadInfo;

public class IntraproceduralBuilder {
//	private static Map<String,UnitGraph> methToUnitGraph=new HashMap<String,UnitGraph>();	
	private static Collection<ThreadInfo> threadInfoCollection;
	
	public static Collection<ThreadInfo> Main(String dumpFile,String mainClass)
			throws FileNotFoundException{
		
		// Process the thread dump to get a structure of thread information
		Map<Integer,ThreadInfo> threadDump=ThreadBuilder.apply(dumpFile); 
		threadInfoCollection=threadDump.values();
		// Run the Soot to get the path of each thread
		IntraproceduralBuilder b=new IntraproceduralBuilder();
		b.build(mainClass);
		return threadInfoCollection;
	}
	public void build(String benchmark){
		
		List<String> argsList = new ArrayList<String>();
/*		Boolean interProcedural=false;
		if(interProcedural){
		
			argsList.addAll(Arrays.asList(new String[]{
					"-w","--app",benchmark,
					"-allow-phantom-refs",
					"-keep-line-number",
					"-print-tags-in-output",
	//				"-via-shimple",
					"-p","wjtp","enabled:true",
					"-p", "wjtp.bcfg","on",
					"-src-prec","java",
					"-f","n",
					"-main-class", benchmark,benchmark
			}));
			PackManager.v().getPack("wjtp")
			.add(new Transform("wjtp.bcfg", new SceneTransformer() {
				@Override
				protected void internalTransform(String phaseName,
						Map options) {
	
							Chain<SootClass> cl = Scene.v().getApplicationClasses();
							for (SootClass sClass : cl) {
								List<SootMethod> ml = sClass.getMethods();
//								MethodLoop:
								for (SootMethod sMethod : ml) {
									// Initialize some information: method, class
									
									String methodName = sMethod.getName();
									String classPath = sClass.getName();
									String clsMetInfo = classPath + "."
											+ methodName;

									
									
									
//									if (methToLineNumber.containsKey(clsMetInfo) == false)
//										continue MethodLoop;
									
//									Integer lineNumber=methToLineNumber.get(clsMetInfo);
									
									if (sMethod.hasActiveBody() == false)
										sMethod.retrieveActiveBody();
									Body body = sMethod.getActiveBody();
									
									Chain<Unit> uChain=body.getUnits();
									Boolean methodFound=false;
									for(Unit u:uChain){
										LineNumberTag lnt = (LineNumberTag)u.getTag("LineNumberTag");
										Integer ln = lnt.getLineNumber();
//										if(ln.equals(lineNumber)){
											
											methodFound=true;
											
											// Map invocation:Unit to callee:SootMethod
											
//										}
									}
									if(methodFound){
										UnitGraph uGraph = new ExceptionalUnitGraph(body);
//										methToUnitGraph.put(clsMetInfo, uGraph);
									}
								}
							}
						}
			}));
		}
		else*/
		{
			argsList.addAll(Arrays.asList(new String[]{
					"--app",benchmark,
					"-allow-phantom-refs" ,
					"-keep-line-number",
//					"-print-tags-in-output",
					"-via-shimple",
//					"-p","wstp","enabled:true",
					
					"-p","jtp","enabled:true",
					"-p", "jtp.bcfg","on",
					"-src-prec","java",
					"-f","n",
					"-main-class", benchmark,benchmark
			}));
			PackManager.v().getPack("jtp")
					.add(new Transform("jtp.bcfg", new BodyTransformer() {
						@Override
						protected void internalTransform(Body body,
								String phaseName, Map options) {
							SootMethod sMethod = body.getMethod();
							SootClass sClass = sMethod.getDeclaringClass();
							String methodName = sMethod.getName();
							String classPath = sClass.getName();
							String clsMetInfo = classPath + "." + methodName;
							
							for(ThreadInfo ti:threadInfoCollection){
								List<Site> sl=ti.getSite();
								
								Site:
								for(Site s:sl){
									String methodCompleteName=s.getPackageName()+"."+s.getClassName()+"."+s.getMethodName();
									
									if(clsMetInfo.equals(methodCompleteName)){
										Integer lineNumber=s.getLineNumber();
										Chain<Unit> uChain=body.getUnits();
										
										
										for(Unit u:uChain){
											LineNumberTag lnt = (LineNumberTag)u.getTag("LineNumberTag");
											if(lineNumber.equals(lnt.getLineNumber())){
												// Site is found here
												if (sMethod.hasActiveBody() == false)
													sMethod.retrieveActiveBody();
												// Set SootMethod and UnitGraph for the site
												if(s.getUnitGraph()==null){
													UnitGraph cfg = new ExceptionalUnitGraph(body);
													s.setUnitGraph(cfg);
													s.setMethod(sMethod);
												}
												else if(!s.getMethod().equals(sMethod)){
													System.err.println("Exception found: Duplicate settings of sites are different");
												}
												break Site;
											}
										}
									}
								}
							}
							
						
							
						}
			}));
		}
		soot.Main.main(argsList.toArray(new String[0]));
	}
}
