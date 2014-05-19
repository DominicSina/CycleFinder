package tarjan;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

import org.eclipse.jdt.core.IPackageFragment;

import dependencyStructures.Dependency;
import dependencyStructures.StronglyConnectedComponent;

/*
 * represents a directed graph,
 * able to execute Tarjan's algorithm to
 * find all the strongly connected components
*/
public class TarjanGraph {

	private LinkedList<TarjanNode<IPackageFragment>> nodes;
	private LinkedList<TarjanEdge<IPackageFragment>> edges;
	
	/*
	 * Builds a directed graph out of a list of 
	 * dependencies. More specific it collects all the
	 * IPackageFragments inside the dependencies and
	 * builds TarjanNodes out of them then connects them
	 * with TarjanEdges.
	 */
	public TarjanGraph(LinkedList<Dependency> dependencies){
		assert dependencies!=null;
		
		//transform dependencies to TarjanEdges&TarjanNodes (only packages with dependencies are considered, makes sense
		// as only these can even form strong connections)
		HashMap<IPackageFragment, TarjanNode<IPackageFragment>> map=new HashMap<IPackageFragment, TarjanNode<IPackageFragment>>();
		nodes=new LinkedList<TarjanNode<IPackageFragment>>();
		for(Dependency dep : dependencies){
			if(!map.containsKey(dep.getStart())){
				map.put(dep.getStart(), new TarjanNode<IPackageFragment>(dep.getStart()));
				nodes.add(map.get(dep.getStart()));
			}
			if(!map.containsKey(dep.getEnd())){
				map.put(dep.getEnd(), new TarjanNode<IPackageFragment>(dep.getEnd()));
				nodes.add(map.get(dep.getEnd()));
			}
		}
		edges=new LinkedList<TarjanEdge<IPackageFragment>>();
		for(Dependency dep : dependencies){
			edges.add(new TarjanEdge<IPackageFragment>(map.get(dep.getStart()), map.get(dep.getEnd())));
		}	
	}
	 
	/*
	 * Returns a list with all the TarjanEdges that go out from a
	 * specific node
	 */
	private LinkedList<TarjanEdge<IPackageFragment>> findEdgesStartingWithNode(TarjanNode<IPackageFragment> node){
		assert node!=null;
		assert nodes.contains(node);
		
		LinkedList<TarjanEdge<IPackageFragment>> startWithNode=new LinkedList<TarjanEdge<IPackageFragment>>(); 
		for (TarjanEdge<IPackageFragment> edge : edges){
			if(edge.getStart().equals(node)){
				startWithNode.add(edge);
			}
		}
		return startWithNode;
	}
	
	/*
	 * Returns all the StronglyConnectedComponents in this TarjanGraph.
	 * Filters those StronglyConnectedComponents with only one node out.
	 */
	public LinkedList<StronglyConnectedComponent> tarjan(){
		assert nodes!=null;
		assert edges!=null;
		
		Integer index=0;
		Stack<TarjanNode<IPackageFragment>> s=new Stack<TarjanNode<IPackageFragment>>();
		LinkedList<LinkedList<TarjanNode<IPackageFragment>>> resultingConnections=new LinkedList<LinkedList<TarjanNode<IPackageFragment>>>();
		
		for(TarjanNode<IPackageFragment> node : nodes){
			if(!node.isIndexSet()){
				strongConnect(resultingConnections, node, index, s);
			}
		}
		
		//filter all components with only one element out
		for(int i=0; i<resultingConnections.size(); i++){
			if(resultingConnections.get(i).size()<=1){
				resultingConnections.remove(i);
				i--;
			}
		}
		
		//transformation to StronglyConnectedComponent
		LinkedList<StronglyConnectedComponent> result=new LinkedList<StronglyConnectedComponent>();
		for(LinkedList<TarjanNode<IPackageFragment>> component : resultingConnections){
			result.add(StronglyConnectedComponent.buildStronglyConnectedComponentFromTarjanNodes(component, edges));
		}
		return result;
	}
	
	/*
	 * Recursive part of Tarjan's Algorithm
	 */
	private void strongConnect(LinkedList<LinkedList<TarjanNode<IPackageFragment>>> result,TarjanNode<IPackageFragment> node, Integer index, Stack<TarjanNode<IPackageFragment>> stack){
		assert node!=null;
		
		node.setIndex(index);
		node.setlowestConnecedIndex(index);
		index++;
		stack.push(node);
		
		LinkedList<TarjanEdge<IPackageFragment>> startWithNode=findEdgesStartingWithNode(node);
		
		for (TarjanEdge<IPackageFragment> edge : startWithNode){
			if(!edge.getEnd().isIndexSet()){
				strongConnect(result, edge.getEnd(), index, stack);
				edge.getStart().setlowestConnecedIndex(Math.min(edge.getStart().getlowestConnecedIndex(), edge.getEnd().getlowestConnecedIndex()));
			}
			else if(stack.contains(edge.getEnd())){
				edge.getStart().setlowestConnecedIndex(Math.min(edge.getStart().getlowestConnecedIndex(), edge.getEnd().getIndex()));
			}
		}
		
		if(node.getIndex()==node.getlowestConnecedIndex()){
			LinkedList<TarjanNode<IPackageFragment>> connectedComponent=new LinkedList<TarjanNode<IPackageFragment>>();
			TarjanNode<IPackageFragment> toAdd;
			do{
				toAdd=stack.pop();
				connectedComponent.add(toAdd);
				
			}while(toAdd!=node);
			
			result.add(connectedComponent);
		}
	}
	
}
