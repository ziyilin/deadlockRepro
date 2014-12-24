package main;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;


import cfg.CFGNode;
import cfg.builder.CFGBuilder;


public class Main {
	public static void main(String[] args) throws InterruptedException{
		CFGBuilder cfgb=new CFGBuilder();
		Graph g=cfgb.build(new ListenableDirectedGraph<CFGNode, DefaultEdge>( DefaultEdge.class ));
	}
}
