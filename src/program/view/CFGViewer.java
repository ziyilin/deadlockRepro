package program.view;

import global.GInfo;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.swing.JApplet;
import javax.swing.JScrollPane;
import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import thread.info.ThreadInfo;
import cfg.CFGNode;
import cfg.builder.CFGBuilder;

/**
 * A demo applet that shows how to use JGraph to visualize JGraphT graphs.
 *
 * @author Barak Naveh
 *
 * @since Aug 3, 2003
 */
public class CFGViewer extends JApplet {
    private static final Color     DEFAULT_BG_COLOR = Color.decode( "#FAFBFF" );
    private static final Dimension DEFAULT_SIZE = new Dimension( 2000, 6400 );

    // 
    private JGraphModelAdapter m_jgAdapter;

    /**
     * @see java.applet.Applet#init().
     */
    public void init(  ) {/*
     // create a JGraphT graph
        ListenableGraph<CFGNode,DefaultEdge> g = new ListenableDirectedGraph<CFGNode,DefaultEdge>( DefaultEdge.class );
        
        // create a visualization using JGraph, via an adapter
        m_jgAdapter = new JGraphModelAdapter( g );

        JGraph jgraph = new JGraph( m_jgAdapter );

        adjustDisplaySettings( jgraph );
        getContentPane().add( jgraph );

//        resize( DEFAULT_SIZE );  
        getContentPane().add( new JScrollPane(jgraph));
        
        CFGBuilder cfgb=new CFGBuilder();
        
        GInfo.v().dumpFile="..\\";
        GInfo.v().confPrefix="..\\";
        cfgb.build(g);
    *
    */
        // create a JGraphT graph
        ListenableGraph g = new ListenableDirectedGraph( DefaultEdge.class );
        
        // create a visualization using JGraph, via an adapter
        m_jgAdapter = new JGraphModelAdapter( g );

        JGraph jgraph = new JGraph( m_jgAdapter );

        adjustDisplaySettings( jgraph );
        getContentPane(  ).add( jgraph );
//        DEFAULT_SIZE.width=x;
//        DEFAULT_SIZE.height=y;
        
        getContentPane().add( new JScrollPane(jgraph));
        // add some sample data (graph manipulated via JGraphT)
       /* g.addVertex( "v1" );
        g.addVertex( "v2" );
        g.addVertex( "v3333" );
        g.addVertex( "v4" );

        g.addEdge( "v1", "v2" );
        g.addEdge( "v1", "v3333" );
        g.addEdge( "v2", "v3333" );
        g.addEdge( "v2", "v4" );
        */
        
        GInfo.v().dumpFile="..\\";
        GInfo.v().confPrefix="..\\";
        CFGBuilder cfgb=new CFGBuilder();
        
       
        cfgb.build(g);
        
        
        // Disable the label of edge
        Set<DefaultEdge> eset=g.edgeSet();
        for(DefaultEdge e:eset){
        	DefaultGraphCell cell = m_jgAdapter.getEdgeCell( e );
        	Map              attr = cell.getAttributes(  );
            GraphConstants.setLabelEnabled( attr, false );
        	 Map cellAttr = new HashMap(  );
             cellAttr.put( cell, attr );
             m_jgAdapter.edit( cellAttr, null, null, null );
        }
        
        // position vertices nicely within JGraph component
        
        
        Integer x=130,y=40;

//        ThreadInfo[] ta=(ThreadInfo[]) .toArray();
        for(ThreadInfo ti:cfgb.threadInfoCollection){
	        Stack<CFGNode> stack=new Stack<CFGNode>();
	        Set<CFGNode> visited=new HashSet<CFGNode>();     
	        CFGNode curr=ti.getCfg().getHead().get(0);
	        visited.add(curr);
	        stack.push(curr);
	        Integer max=-1;
	        while(stack.size()>0){
	        	List<CFGNode> temp=new LinkedList<CFGNode>();
	        	temp.addAll(stack);
	        	x=40;
	        	
	        	while(stack.size()!=0){
	        		CFGNode p=stack.pop();
	        		positionVertexAt( p, x, y );
	        		if(p.toString().length()>max)
	        			max=p.toString().length();

	        		y=y+70;
	        	}
	        	y=y+70;
	        	for(CFGNode t:temp){
	        		for(Object o:g.edgesOf(t)){
	        			DefaultEdge e=(DefaultEdge)o;
	        			CFGNode succ=(CFGNode)g.getEdgeTarget(e);
	        			
	        			if(visited.add(succ))
	        				stack.push(succ);
	        		}
	        	}
	        }
    		x=x+max*20+100;
        }
      /*  positionVertexAt( "v2", 60, 200 );
        positionVertexAt( "v3", 310, 230 );
        positionVertexAt( "v4", 380, 70 );*/

        // that's all there is to it!...
        DEFAULT_SIZE.height=y;
        DEFAULT_SIZE.width=x;
        resize( DEFAULT_SIZE );

    }


    private void adjustDisplaySettings( JGraph jg ) {
        jg.setPreferredSize( DEFAULT_SIZE );

        Color  c        = DEFAULT_BG_COLOR;
        String colorStr = null;

        try {
            colorStr = getParameter( "bgcolor" );
        }
         catch( Exception e ) {}

        if( colorStr != null ) {
            c = Color.decode( colorStr );
        }

        jg.setBackground( c );
    }


    private void positionVertexAt( Object vertex, int x, int y ) {
        DefaultGraphCell cell = m_jgAdapter.getVertexCell( vertex );
        Map              attr = cell.getAttributes(  );
        
        Rectangle2D        b    = GraphConstants.getBounds( attr );        
        b.setRect(x, y, vertex.toString().length()*15, 35);
        
        GraphConstants.setBounds( attr, b );

        Font font=GraphConstants.getFont(attr);
        float size=20;
        font=font.deriveFont(size);
        GraphConstants.setFont(attr, font);
        
        Map cellAttr = new HashMap(  );
        cellAttr.put( cell, attr );
        m_jgAdapter.edit( cellAttr, null, null, null );
    }
}