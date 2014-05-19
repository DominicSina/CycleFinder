package dependencyStructures;

import java.util.LinkedList;

import org.eclipse.jdt.core.IPackageFragment;

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
	
	/*
	 * Returns all the cycles contained in this strongly connected component.
	 * Uses a recursive depth-first algorithm. The recursion end when it has reached
	 * a depth equals to the number of packages in this component(the longest cycle can't be 
	 * longer than that). 
	 */
	public LinkedList<Cycle> findCycles(){
		
		LinkedList<LinkedList<IPackageFragment>> cyclesFound=new LinkedList<LinkedList<IPackageFragment>>();
		for (IPackageFragment pack : packages){	
			findCycle(cyclesFound, pack, pack, new LinkedList<IPackageFragment>(), 0, packages.size());
		}
		
		LinkedList<Cycle> result=new LinkedList<Cycle>();
		for(LinkedList<IPackageFragment> cycleList : cyclesFound){
			result.add(new Cycle(cycleList));
		}
		
		return result;
	}
	
	/*
	 * Recursive part of the cycle finding algorithm
	 */
	private void findCycle(LinkedList<LinkedList<IPackageFragment>> result, IPackageFragment startingPackage, IPackageFragment currentPackage, LinkedList<IPackageFragment> visitedPackages, int deapth, int maxDeapth){
		assert startingPackage!=null;
		assert visitedPackages!=null;
		assert result!=null;
		
		//possible new cycle found
		if (currentPackage==startingPackage&&visitedPackages.size()>0){
			boolean foundMatchingCycle=false;
			for(LinkedList<IPackageFragment> detectedCycle : result){
				if (detectedCycle.containsAll(visitedPackages)&&visitedPackages.containsAll(detectedCycle)){
					foundMatchingCycle=true;
				}
			}
			if(foundMatchingCycle==false){
				//cycle indeed was new
				result.add(visitedPackages);
			}
		}
		else if(visitedPackages.contains(currentPackage)||deapth>=maxDeapth){
			//end of recursion;
		}
		else{
			LinkedList<Dependency> outgoingEdges=findDependenciesStartingWithPackage(currentPackage);
			for(Dependency edge : outgoingEdges){
				LinkedList<IPackageFragment> visitedNodesNew=new LinkedList<IPackageFragment>(visitedPackages);
				visitedNodesNew.add(currentPackage);
				findCycle(result, startingPackage, edge.getEnd(), visitedNodesNew, deapth+1, maxDeapth);
			}
		}
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
	private String getPackageNames(){
		String packageNames="{";
		packageNames+=packages.get(0).getElementName();
		for(int i=1; i<packages.size();i++){
			packageNames+=", "+packages.get(i).getElementName();
		}
		packageNames+="}";
		return packageNames;
	}
}
