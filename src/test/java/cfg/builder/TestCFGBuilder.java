package cfg.builder;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;

import cfg.info.CFGNode;

public class TestCFGBuilder {
	public static void main(String[] args) throws InterruptedException{
		CFGBuilder cfgb=new CFGBuilder();
		Graph<CFGNode, DefaultEdge> g=cfgb.build(new ListenableDirectedGraph<CFGNode, DefaultEdge>( DefaultEdge.class ));
		System.out.println(g.toString());
	}
}
