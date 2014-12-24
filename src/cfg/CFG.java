package cfg;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
/**
 * 
 * Class CFG contains an interprocedural control flow graph that stores call info and intraprocedural flow of control 
 * 
 * The intraprocedural information is collected from thread dump. 
 * 
 * The intraprocedural information is obtained from static analysis of Soot framework. 
 * 
 * @author Derrick
 *
 * @since Dec 8, 2014
 */
public class CFG {
	private Set<CFGNode> node;	
	private Map<CFGNode,List<CFGNode>> edge;
	private Map<CFGNode,List<CFGNode>> edgeInverse;

	private List<CFGNode> head;
	private List<CFGNode> tail;
	
	public CFG(){
		node=new HashSet<CFGNode>();
		edge=new HashMap<CFGNode,List<CFGNode>>();
		edgeInverse=new HashMap<CFGNode,List<CFGNode>>();		
		head=new LinkedList<CFGNode>();
		tail=new LinkedList<CFGNode>();
		
	}
	
	public Boolean addEdge(CFGNode source, CFGNode target){
		
		Boolean result=false;
		source=addNode(source);
		target=addNode(target);
		if(edge.containsKey(source)==false){
			List<CFGNode> targetList=new LinkedList<CFGNode>();
			result=targetList.add(target);
			edge.put(source, targetList);
		}else{
			List<CFGNode> targetList=edge.get(source);
			result=targetList.add(target);
		}
		addInverseEdge(source,target);
//		calculateHead();
//		calculateTail();
		return result;
	}
	public Boolean addInverseEdge(CFGNode target, CFGNode source){
		Boolean result=false;
		if(edgeInverse.containsKey(source)==false){
			List<CFGNode> targetList=new LinkedList<CFGNode>();
			result=targetList.add(target);
			edgeInverse.put(source, targetList);
		}else{
			List<CFGNode> targetList=edgeInverse.get(source);
			result=targetList.add(target);
		}
		return result;
		
	}
	public Map<CFGNode,List<CFGNode>> getEdges(){
		return edge;
	}
	public Set<CFGNode> getNodes(){
		return node;
	}
	public CFGNode addNode(CFGNode n){
		for(CFGNode cfgn:node){
			if(cfgn.equals(n)){
				return cfgn;
			}
		}
		node.add(n);
		return n;
	}
	
	public List<CFGNode> getHead() {
		return head;
	}
	public List<CFGNode> getTail() {
		return tail;
	}
	public void addHead(CFGNode h){
		head.add(h);
		node.add(h);
	}
	/*private List<CFGNode> calculateHead(){
		List<CFGNode> result=new LinkedList<CFGNode>();
		for(CFGNode cfn:node){
			if(edgeInverse.containsKey(cfn)==false){
				result.add(cfn);
			}
		}
		head=result;
		return result;
	}
	private List<CFGNode> calculateTail(){
		List<CFGNode> result=new LinkedList<CFGNode>();
		for(CFGNode cfn:node){
			if(edge.containsKey(cfn)==false){
				result.add(cfn);
				break;
			}
		}
		tail=result;
		return result;
	}*/
	
}
