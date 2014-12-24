package program.view;

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
import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * A demo applet that shows how to use JGraph to visualize JGraphT graphs.
 *
 * @author Barak Naveh
 *
 * @since Aug 3, 2003
 */
public class JGraphAdapterDemo extends JApplet {
    private static final Color     DEFAULT_BG_COLOR = Color.decode( "#FAFBFF" );
    private static final Dimension DEFAULT_SIZE = new Dimension( 530, 320 );

    // 
    private JGraphModelAdapter m_jgAdapter;

    /**
     * @see java.applet.Applet#init().
     */
    public void init(  ) {
        // create a JGraphT graph
        ListenableGraph g = new ListenableDirectedGraph( DefaultEdge.class );
        
        // create a visualization using JGraph, via an adapter
        m_jgAdapter = new JGraphModelAdapter( g );

        JGraph jgraph = new JGraph( m_jgAdapter );

        adjustDisplaySettings( jgraph );
        getContentPane(  ).add( jgraph );
        resize( DEFAULT_SIZE );

        // add some sample data (graph manipulated via JGraphT)
        g.addVertex( "v1" );
        g.addVertex( "v2" );
        g.addVertex( "v3333" );
        g.addVertex( "v4" );

        g.addEdge( "v1", "v2" );
        g.addEdge( "v4", "v3333" );
//        g.addEdge( "v2", "v3333" );
//        g.addEdge( "v2", "v4" );
        
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
        String curr="v1";
        Stack<String> stack=new Stack();
        Set<String> visited=new HashSet<String>();        
        visited.add(curr);
        stack.push(curr);
        while(stack.size()>0){
        	List<String> temp=new LinkedList<String>();
        	temp.addAll(stack);
        	y=40;
        	Integer max=-1;
        	while(stack.size()!=0){
        		String p=stack.pop();
        		positionVertexAt( p, x, y );
        		if(p.length()>max)
        			max=p.length();
        		y=y+100;
        	}
        	x=x+max*20+40;
        	for(String t:temp){
        		for(Object o:g.edgesOf(t)){
        			DefaultEdge e=(DefaultEdge)o;
        			String succ=(String)g.getEdgeTarget(e);
        			
        			if(visited.add(succ))
        				stack.push(succ);
        		}
        	}
        }
        
      /*  positionVertexAt( "v2", 60, 200 );
        positionVertexAt( "v3", 310, 230 );
        positionVertexAt( "v4", 380, 70 );*/

        // that's all there is to it!...
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