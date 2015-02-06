package cfg.builder;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;

import thread.info.ThreadInfo;

import cfg.info.CFGNode;

public class TestCFGBuilder {
	public static void main(String[] args) throws InterruptedException{
		CFGBuilder cfgb=new CFGBuilder();
		Graph<CFGNode, DefaultEdge> g=cfgb.build(new ListenableDirectedGraph<CFGNode, DefaultEdge>( DefaultEdge.class ));
		for (ThreadInfo ti : cfgb.threadInfoCollection) {
			System.out.println("-----"+ti.getThreadName()+"-----");
			Stack<CFGNode> stack = new Stack<CFGNode>();
			Set<CFGNode> visited = new HashSet<CFGNode>();
			CFGNode curr = ti.getCfg().getHead().get(0);
			visited.add(curr);
			stack.push(curr);
			while (stack.size() > 0) {
				List<CFGNode> temp = new LinkedList<CFGNode>();
				temp.addAll(stack);
				while (stack.size() != 0) {
					CFGNode p = stack.pop();
					System.out.print(p+"\t\t");
				}
				System.out.println();
				for (CFGNode t : temp) {
					for (Object o : g.edgesOf(t)) {
						DefaultEdge e = (DefaultEdge) o;
						CFGNode succ = (CFGNode) g.getEdgeTarget(e);
						if (visited.add(succ))
							stack.push(succ);
					}
				}
			}
		}
	}
}
