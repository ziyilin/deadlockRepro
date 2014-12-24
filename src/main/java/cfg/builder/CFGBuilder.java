package cfg.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ini4j.Wini;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import program.analysis.IntraproceduralBuilder;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.internal.JInvokeStmt;
import soot.tagkit.LineNumberTag;
import soot.toolkits.graph.UnitGraph;
import thread.info.Site;
import thread.info.ThreadInfo;
import cfg.CFGNode;
import cfg.CallNode;
import cfg.EnterNode;
import cfg.NormalNode;
import cfg.StmtNode;

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

	public Graph build(Graph<CFGNode, DefaultEdge> g) {
		try {
			String mainClass = getMainClassPath();
			String threadDump = getDumpFilePath();
			// Collect an interprocedural control flow graph for each thread
			/* Collection<ThreadInfo> */
			threadInfoCollection = IntraproceduralBuilder.Main(threadDump,
					mainClass);

			for (ThreadInfo ti : threadInfoCollection) {
				System.out.println(ti.getThreadName());
				List<Site> stackTrace = ti.getSite();
				Integer stackDepth = stackTrace.size();
				StmtNode traceNode = null;
				StmtNode currNode = null;
				Unit currUnit = null;

				for (Integer i = stackDepth - 1; i >= 0; i = i - 1) {
					Site currSite = stackTrace.get(i);
					if (currSite.getUnitGraph() == null)
						continue;
					SootMethod method = currSite.getMethod();
					UnitGraph ug = currSite.getUnitGraph();

					EnterNode enterNode = new EnterNode(method);
					if (traceNode == null)
						ti.getCfg().addHead(enterNode);
					if (traceNode != null && traceNode.getType().equals("CALL")) {

						CallNode callNode = (CallNode) traceNode;
						// Check if the call statement and the callee method
						// matches
						if (callNode.getStmt().toString()
								.contains(method.getSignature()))
							// Add an inter-procedural edge to the CFG of the
							// current thread
							ti.getCfg().addEdge(callNode, enterNode);
					}

					for (Unit u : ug) {
						LineNumberTag lnt = (LineNumberTag) u
								.getTag("LineNumberTag");
						if (lnt.getLineNumber() == currSite.getLineNumber()) {
							// Site in the stack trace is found here
							currUnit = u;
							if (u instanceof JInvokeStmt)
								traceNode = new CallNode(u);
							else
								traceNode = new NormalNode(u);
							break;
						}
					}

					// Add intra-procedural paths from enterNode to currNode
					// First, link enterNode to nodes without predecessors
					for (Unit u : ug) {

						List<Unit> preds = ug.getPredsOf(u);

						// The node without predecessors is a unique head of the
						// CFG
						if (preds.size() == 0) {
							// link the enterNode to the head of the CFG
							if (u instanceof JInvokeStmt)
								ti.getCfg().addEdge(enterNode, new CallNode(u));
							else
								ti.getCfg().addEdge(enterNode,
										new NormalNode(u));
						}
					}
					// Second, link nodes without predecessors to the currNode
					List<Unit> worklist = new LinkedList<Unit>();
					worklist.add(currUnit);
					while (worklist.size() > 0) {
						currUnit = worklist.get(0);
						worklist.remove(0);
						List<Unit> preds = ug.getPredsOf(currUnit);
						worklist.addAll(preds);
						if (currUnit instanceof JInvokeStmt) {
							currNode = new CallNode(currUnit);
						} else {
							currNode = new NormalNode(currUnit);
						}
						for (Unit p : preds) {
							StmtNode predNode = null;
							if (p instanceof JInvokeStmt) {
								predNode = new CallNode(p);
							} else {
								predNode = new NormalNode(p);
							}
							ti.getCfg().addEdge(predNode, currNode);
						}
					}

				}
			}

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

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return g;
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
