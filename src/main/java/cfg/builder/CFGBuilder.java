package cfg.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ini4j.Wini;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import program.analysis.IntraproceduralBuilder;
import soot.G;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JEnterMonitorStmt;
import soot.jimple.internal.JExitMonitorStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.tagkit.LineNumberTag;
import soot.toolkits.graph.UnitGraph;
import thread.info.SiteInfo;
import thread.info.ThreadInfo;
import cfg.info.CFG;
import cfg.info.CFGNode;
import cfg.info.CallNode;
import cfg.info.EnterNode;
import cfg.info.ExitNode;
import cfg.info.LockNode;
import cfg.info.NormalNode;
import cfg.info.ReturnNode;
import cfg.info.StmtNode;
import cfg.info.UnlockNode;

public class CFGBuilder {
	private static final String BUILDER_INI = "builder.ini";
	private String dumpFilePath;
	private String mainClassPath;
	public Collection<ThreadInfo> threadInfoCollection;

	public CFGBuilder() {
		try {
			initialize();
		} catch (NoSuchFileException e) {
			e.printStackTrace();
		}
	}

	private void initialize() throws NoSuchFileException {
		Wini ini = null;
		try {
			ini = new Wini(new File(BUILDER_INI));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (ini == null) {
			System.err.println("Program failed to load the .ini file.");
			throw new NoSuchFileException(BUILDER_INI);
		}
		setDumpFilePath(ini.get("thread dump", "filepath", String.class));
		setMainClassPath(ini.get("main class", "classpath", String.class));
		System.out.println("Read thread dump: " + getDumpFilePath());
		System.out.println("Analyze programs:" + getMainClassPath());
	}

	public Graph<CFGNode, DefaultEdge> build(Graph<CFGNode, DefaultEdge> g) {
		String mainClass = getMainClassPath();
		String threadDump = getDumpFilePath();
		// Collect an interprocedural control flow graph for each thread
		/* Collection<ThreadInfo> */
		try{
			threadInfoCollection = IntraproceduralBuilder.Main(threadDump,mainClass);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (ThreadInfo ti : threadInfoCollection) {
			System.out.println(ti.getThreadName());
			List<SiteInfo> stackTrace = ti.getSite();
			Integer stackDepth = stackTrace.size();
			StmtNode traceNode = null;
			Unit traceUnit = null;
			
			// Visit all sites from the top level caller to the bottom level callee
			for (Integer i = stackDepth - 1; i >= 0; i = i - 1) {
				SiteInfo currSite = stackTrace.get(i);
				if (currSite.getUnitGraph() == null)
					continue;
				SootMethod method = currSite.getMethod();
				UnitGraph ug = currSite.getUnitGraph();

				EnterNode enterNode = new EnterNode(method);
				
				// Define a head CFG node of the current thread
				if (traceNode == null)
					ti.getCfg().addHead(enterNode);
				
				if (traceNode != null) {
					// The trace node found in the caller will be linked to 
					// the enter node of the current method after redundant check
					if( traceNode.getType().equals("CALL")){
						
						CallNode callNode = (CallNode) traceNode;
						// Check if the call statement and the callee method matches
						if (callNode.getStmt().toString().contains(
								method.getSignature())){
							// Add an inter-procedural edge to the CFG of the
							// current thread
							
							ti.getCfg().addEdge(callNode, enterNode);
							
							String callType="CALL";
							if(method.isStatic()){
								callType = "STATIC "+callType;
							}
							if(method.isSynchronized()){
								callType = "SYNC "+callType;
							}
							callNode.setType(callType);
						}
					}
				}
				
				// Trace the CFG node at the site of the reported line number 
				SootMethod callee = null;
				if(i>0)
					callee=stackTrace.get(i-1).getMethod();
				for (Unit u : ug) {
					LineNumberTag lnt = (LineNumberTag) u.getTag("LineNumberTag");
					// If the line number of the reported site and that of the CFG unit
					//  matches and the invocation statement of the reported site and
					// that of the CFG unit matches, the unit will be a trace node to start from
					if (lnt.getLineNumber() == currSite.getLineNumber()) {
						traceUnit = u;
						traceNode = createStmtNode(u);
						if(callee!=null)
							if(u.toString().contains(callee.getSignature()))
								break;	
					}
				}

				// Add intra-procedural paths from enterNode to traceNode
				// First, link enterNode to nodes without predecessors
				for (Unit u : ug) {

					List<Unit> preds = ug.getPredsOf(u);

					// The node without predecessors is a unique head of the CFG
					if (preds.size() == 0) {
						// link the enterNode to the head of the CFG
						StmtNode newNode=createStmtNode(u);
						ti.getCfg().addEdge(enterNode, newNode);							
					}
				}
				
				// Second, link nodes without predecessors to traceNode
				// The traced unit is treated as a start point 
				List<Unit> worklist = new LinkedList<Unit>();
				worklist.add(traceUnit);
				
				Unit currUnit = null;
				StmtNode currNode = null;
				Integer index=0;
				while (worklist.size() > 0) {
					currUnit = worklist.get(0);
					worklist.remove(0);
					List<Unit> preds = ug.getPredsOf(currUnit);
					worklist.addAll(preds);
					if(index>0)
						currNode=createStmtNode(currUnit);
					else
						currNode=traceNode;
					index = index + 1;
					for (Unit p : preds) {
						StmtNode predNode = null;
						predNode = createStmtNode(p);

						ti.getCfg().addEdge(predNode, currNode);
					}
				}
			}
			
			// Visit each call node without enter node in the successors

			do{
				
				Map<CFGNode,List<CFGNode>> edges=ti.getCfg().getEdges();
				Set<CFGNode> callNodesWithoutEnter=new HashSet<CFGNode>();
				for(CFGNode fNode:edges.keySet()){
					if(fNode.getType().contains("CALL")){
						List<CFGNode> tSet=edges.get(fNode);
						Boolean hasEnter=false;
						for(CFGNode tNode:tSet){
							if(tNode.getType().equals("ENTER")){
								hasEnter=true;
								break;
							}
						}
						if(!hasEnter){
							callNodesWithoutEnter.add(fNode);
						}
					}
				}
				if(callNodesWithoutEnter.size()==0)
					break;
				for(CFGNode fNode:callNodesWithoutEnter){
					List<CFGNode> tSet=edges.get(fNode);
					// Find the callees
					Map<SootMethod, UnitGraph> m2cfg=IntraproceduralBuilder.methodToCFG;
					Boolean findCallee=false;
					for(SootMethod met:m2cfg.keySet()){
						if(fNode.toString().contains(met.getSignature())){
							findCallee=true;
							// Add the found callee from one to five
							SootMethod callee=met;
							UnitGraph cfg = m2cfg.get(callee);
							EnterNode calleeEnter = new EnterNode(callee);
							ExitNode calleeExit = new ExitNode(callee);
							CallNode callNode=(CallNode) fNode;
							ReturnNode returnNode=new ReturnNode(callNode.getStmt());
							// First, add the edges between return node and the call node's old successors
							for(CFGNode tNode:tSet){
								ti.getCfg().addEdge(returnNode, tNode);
							}
							// Second, remove the edge between call node and its old successors						
							tSet.clear();
							
							// Third, add the edge between call node and enter node
							ti.getCfg().addEdge(callNode, calleeEnter);
							
							// Fourth, add the edge between exit node and return node
							ti.getCfg().addEdge(calleeExit, returnNode);
							
							// Fifth, add the path between enter node and exit node of the callee method
							for(Unit u:cfg){
								if(cfg.getPredsOf(u).size()==0){
									StmtNode head=createStmtNode(u);
									ti.getCfg().addEdge(calleeEnter, head);
									
									List<Unit> wlist=new LinkedList<Unit>();
									wlist.add(u);
									Set<Unit> calleeVisited=new HashSet<Unit>();
									while(wlist.size()>0){
										Unit curr=wlist.get(0);
										wlist.remove(0);										
									    if(!calleeVisited.add(curr))
									    	continue;
										List<Unit> succs=cfg.getSuccsOf(curr);
										for(Unit succ:succs){
											ti.getCfg().addEdge(createStmtNode(curr),createStmtNode(succ));
										}
										wlist.addAll(succs);
										
									}
									
								}
							}
							for(Unit u:cfg){
								if(cfg.getSuccsOf(u).size()==0){
									StmtNode tail=createStmtNode(u);
									ti.getCfg().addEdge(tail, calleeExit);
								}
							}
							
						}
					}
					if(!findCallee){
						fNode.setType("NORMAL");
						System.err.println(fNode.toString()+" has no callee");
					}
				}
				
				
			}while(true);
		}

		
		
		
		
		// add cfg to graph g
		for (ThreadInfo ti : threadInfoCollection) {
			Set<CFGNode> nodes = ti.getCfg().getNodes();
			Map<CFGNode, List<CFGNode>> edges = ti.getCfg().getEdges();
			for (CFGNode node : nodes)
				g.addVertex(node);
			for (CFGNode source : edges.keySet()) {
				List<CFGNode> targets = edges.get(source);
				for (CFGNode target : targets) {
					g.addEdge(source, target);
				}
			}
		}

	

		return g;
	}
	
	private StmtNode createStmtNode(Unit u){
		StmtNode traceNode=null;
		
		if (u instanceof JInvokeStmt){
			
			traceNode = new CallNode(u);
		
		}else if(u instanceof JAssignStmt){
			
			if(((JAssignStmt) u).containsInvokeExpr())
				traceNode = new CallNode(u);
			else	
				traceNode = new NormalNode(u);
			
		}
		else if(u instanceof JEnterMonitorStmt){
			traceNode = new LockNode(u);
		}
		else if(u instanceof JExitMonitorStmt){
			traceNode = new UnlockNode(u);
		}
		else{
			traceNode = new NormalNode(u);
		}
		
		return traceNode;
	}
	public String getDumpFilePath() {
		return dumpFilePath;
	}

	public void setDumpFilePath(String filepath) {
		this.dumpFilePath = filepath;
	}

	public String getMainClassPath() {
		return mainClassPath;
	}

	public void setMainClassPath(String classpath) {
		this.mainClassPath = classpath;
	}
}
