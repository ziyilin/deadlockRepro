package program.synthesis;

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
import cfg.builder.CFGBuilder;
import cfg.info.CFGNode;

/**
 * An applet that shows how to use JGraph to visualize control flow graphs.
 *
 * @author kejun xiao
 *
 * @since Dec 27, 2014
 */
public class CFGViewer extends JApplet {
	private static final long serialVersionUID = 1L;
	private static final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFF");
	private static final Dimension DEFAULT_SIZE = new Dimension(2000, 6400);
	private JGraphModelAdapter<CFGNode, DefaultEdge> m_jgAdapter;
	public void init() {
		// create a JGraphT graph
		ListenableGraph<CFGNode, DefaultEdge> g = new ListenableDirectedGraph<CFGNode, DefaultEdge>(DefaultEdge.class);
		// create a visualization using JGraph, via an adapter
		m_jgAdapter = new JGraphModelAdapter<CFGNode, DefaultEdge>(g);
		JGraph jgraph = new JGraph(m_jgAdapter);
		adjustDisplaySettings(jgraph);
		getContentPane().add(jgraph);
		getContentPane().add(new JScrollPane(jgraph));
		// build the cfg
		CFGBuilder cfgb = new CFGBuilder();
		cfgb.build(g);
		// remove the label of edge
		Set<DefaultEdge> eset = g.edgeSet();
		for (DefaultEdge e : eset) {
			DefaultGraphCell cell = m_jgAdapter.getEdgeCell(e);
			Map attr = cell.getAttributes();
			GraphConstants.setLabelEnabled(attr, false);
			Map cellAttr = new HashMap();
			cellAttr.put(cell, attr);
			m_jgAdapter.edit(cellAttr, null, null, null);
		}
		// position vertices nicely within JGraph component
		Integer x=0,y=0;
		Integer maxWidth = 0;
		Integer xMax = 0;
		Integer count=0;
		Integer xStart=40;
		Integer yStart=40;
		Integer yStep=60;
		for (ThreadInfo ti : cfgb.threadInfoCollection) {
			count = count + 1;
			Stack<CFGNode> stack = new Stack<CFGNode>();
			Set<CFGNode> visited = new HashSet<CFGNode>();
			CFGNode curr = ti.getCfg().getHead().get(0);
			visited.add(curr);
			stack.push(curr);
			x = xStart+x+maxWidth;
			y = yStart;
			while (stack.size() > 0) {
				List<CFGNode> temp = new LinkedList<CFGNode>();
				temp.addAll(stack);
			
				Integer xTemp=x;
				while (stack.size() != 0) {
					CFGNode p = stack.pop();
					Integer width=positionVertexAt(p, x, y);
					x=x+width+30;
					if (width > maxWidth)
						maxWidth = width;
					if(x>xMax)
						xMax=x;
				}
				x=xTemp;
				
				y = y + yStep;
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

		DEFAULT_SIZE.height = y;
		DEFAULT_SIZE.width = xMax;
		resize(DEFAULT_SIZE);

	}

	private void adjustDisplaySettings(JGraph jg) {
		jg.setPreferredSize(DEFAULT_SIZE);
		Color c = DEFAULT_BG_COLOR;
		String colorStr = null;
		try {
			colorStr = getParameter("bgcolor");
		} catch (Exception e) {
		}
		if (colorStr != null) {
			c = Color.decode(colorStr);
		}
		jg.setBackground(c);
	}
	
	private Integer positionVertexAt(Object vertex, int x, int y) {
		DefaultGraphCell cell = m_jgAdapter.getVertexCell(vertex);
		Map attr = cell.getAttributes();
		Integer width=vertex.toString().length()*12;
		
		Rectangle2D b = GraphConstants.getBounds(attr);
		b.setRect(x, y, width, 35);
		GraphConstants.setBounds(attr, b);
	
		Font font = GraphConstants.getFont(attr);
		float size=20;
		font = font.deriveFont(size);
		GraphConstants.setFont(attr, font);
		
		String vLabel=vertex.toString();
		String tPart=vLabel.split("\\.")[0];
		if(tPart.contains("LOCK")||tPart.contains("SYNC"))
			GraphConstants.setBackground(attr, Color.gray);

		Map cellAttr = new HashMap();
		cellAttr.put(cell, attr);
		m_jgAdapter.edit(cellAttr, null, null, null);
		return width;
	}
}