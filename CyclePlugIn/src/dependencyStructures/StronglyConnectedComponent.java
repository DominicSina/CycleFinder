package dependencyStructures;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.eclipse.jdt.core.IPackageFragment;

import de.normalisiert.utils.graphs.ElementaryCyclesSearch;
import tarjan.TarjanEdge;
import tarjan.TarjanNode;


/*
 * Represents a strongly connected component.
 * Is able to find all cycles contained within this component.
 */
public class StronglyConnectedComponent {
	
	private LinkedList<IPackageFragment> packages;//the packages that this cycle consists of 
	private LinkedList<Dependency> dependencies;//the dependencies between the packages
	
	public StronglyConnectedComponent(LinkedList<IPackageFragment> packages, LinkedList<Dependency> dependencies){
		assert packages!=null;
		assert packages.size()>=2;
		assert dependencies!=null;
		assert dependencies.size()>=2;
		
		this.packages=new LinkedList<IPackageFragment>(packages);
		
		for(int i=0; i<dependencies.size(); i++){
			Dependency dep=dependencies.get(i);
			if((!packages.contains(dep.getStart()))||(!packages.contains(dep.getEnd()))){
				dependencies.remove(i);
				i--;
			}
		}
		this.dependencies=new LinkedList<>(dependencies);
	}
	
	/*
	 * Static method to build a strongly connected component out of 
	 * TarjanNodes and TarjanEdges
	 */
	public static StronglyConnectedComponent buildStronglyConnectedComponentFromTarjanNodes(LinkedList<TarjanNode<IPackageFragment>> nodes, LinkedList<TarjanEdge<IPackageFragment>> edges){
		assert nodes!=null;
		assert nodes.size()>=2;
		
		LinkedList<IPackageFragment> packages=new LinkedList<IPackageFragment>();
		for(TarjanNode<IPackageFragment> node : nodes){
			packages.add(node.getNode());
		}
		
		LinkedList<Dependency> dependencies=new LinkedList<Dependency>();
		for(TarjanEdge<IPackageFragment> edge : edges){
			if(packages.contains((edge.getStart().getNode()))){
				dependencies.add(new Dependency(edge.getStart().getNode(), edge.getEnd().getNode()));
			}
		}
		StronglyConnectedComponent component=new StronglyConnectedComponent(packages, dependencies);
		
		return component;
	}
	
	public LinkedList<Cycle> findCycles(){
		
		IPackageFragment[] packageArray=new IPackageFragment[packages.size()];
		for(int i=0; i<packageArray.length; i++){
			packageArray[i]=packages.get(i);
		}
		
		ElementaryCyclesSearch eSearcher=new ElementaryCyclesSearch(getAdjMatrix(), packageArray);
		List list=eSearcher.getElementaryCycles();
		
		LinkedList<LinkedList<IPackageFragment>> cycles = new LinkedList<LinkedList<IPackageFragment>>();
		
		for(Vector vec : (List<Vector<IPackageFragment>>)list){
			cycles.add(new LinkedList<IPackageFragment>(vec));
		}
		
		LinkedList<Cycle> result=new LinkedList<Cycle>();
		for(LinkedList<IPackageFragment> cycle : cycles){
			result.add(new Cycle(cycle));
		}
	
		return result;
	}
	
	/*
	 * Compute adjacency matrix
	 */
	private boolean[][] getAdjMatrix(){
		boolean[][] adjMat=new boolean[packages.size()][packages.size()];
		
		for (Dependency dep : dependencies){
			adjMat[packages.indexOf(dep.getStart())][packages.indexOf(dep.getEnd())]=true;
		}
		return adjMat;
	}
		
	/*
	 * returns a list with all the dependencies that start with IPackageFragment pack that are 
	 * contained in this strongly connected component
	 */
	private LinkedList<Dependency> findDependenciesStartingWithPackage(IPackageFragment pack){
		assert pack!=null;
		
		LinkedList<Dependency> startWithPack=new LinkedList<Dependency>(); 
		for (Dependency dep : dependencies){
			if(dep.getStart().equals(pack)){
				startWithPack.add(dep);
			}
		}
		return startWithPack;
	}
	
	@Override
	public String toString(){
		return "Cycle group"+getPackageNames();
	}
	
	/*
	 * returns package names in curved brackets
	 * separated with commas
	 */
	public String getPackageNames(){
		String packageNames="{";
		packageNames+=packages.get(0).getElementName();
		for(int i=1; i<packages.size();i++){
			packageNames+=", "+packages.get(i).getElementName();
		}
		packageNames+="}";
		return packageNames;
	}
}
