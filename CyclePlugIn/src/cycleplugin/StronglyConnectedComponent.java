package cycleplugin;

import java.util.LinkedList;

import org.eclipse.jdt.core.IPackageFragment;

public class StronglyConnectedComponent {

	
	private LinkedList<IPackageFragment> packages;
	private LinkedList<Dependency> dependencies;
	
	public StronglyConnectedComponent(LinkedList<IPackageFragment> packages, LinkedList<Dependency> dependencies){
		assert packages!=null;
		assert packages.size()>=2;
		assert dependencies!=null;
		assert dependencies.size()>=2;
		
		this.packages=new LinkedList<IPackageFragment>(packages);
		this.dependencies=new LinkedList<>(dependencies);
	}
	
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
		
		LinkedList<LinkedList<IPackageFragment>> cyclesFound=new LinkedList<LinkedList<IPackageFragment>>();
		for (IPackageFragment pack : packages){	
			findCylce(cyclesFound, pack, pack, new LinkedList<IPackageFragment>(), dependencies, 0, packages.size());
		}
		
		LinkedList<Cycle> result=new LinkedList<Cycle>();
		for(LinkedList<IPackageFragment> cycleList : cyclesFound){
			result.add(new Cycle(cycleList));
		}
		
		return result;
	}
	
	public void findCylce(LinkedList<LinkedList<IPackageFragment>> result, IPackageFragment startingPackage, IPackageFragment currentPackage, LinkedList<IPackageFragment> visitedPackages, LinkedList<Dependency> dependencies, int deapth, int maxDeapth){
		assert startingPackage!=null;
		assert visitedPackages!=null;
		assert result!=null;
		
		if (currentPackage==startingPackage&&visitedPackages.size()>0){
			boolean foundMatchingCycle=false;
			for(LinkedList<IPackageFragment> detectedCycle : result){
				if (detectedCycle.containsAll(visitedPackages)){
					foundMatchingCycle=true;
				}
			}
			if(foundMatchingCycle==false){
				result.add(visitedPackages);
			}
			//return visitedNodes;
		}
		else if(visitedPackages.contains(currentPackage)){
			//return null;
		}
		else if(deapth>=maxDeapth){
			//return null;
		}
		else{
			LinkedList<Dependency> outgoingEdges=findDependenciesStartingWithPackage(currentPackage, dependencies);
			for(Dependency edge : outgoingEdges){
				LinkedList<IPackageFragment> visitedNodesNew=new LinkedList<IPackageFragment>(visitedPackages);
				visitedNodesNew.add(currentPackage);
				findCylce(result, startingPackage, edge.getEnd(), visitedNodesNew, dependencies, deapth+1, maxDeapth);
			}
		}
		
	}
	
	private LinkedList<Dependency> findDependenciesStartingWithPackage(IPackageFragment pack, LinkedList<Dependency> allDependencies){
		assert pack!=null;
		assert allDependencies!=null;
		
		LinkedList<Dependency> startWithPack=new LinkedList<Dependency>(); 
		for (Dependency dep : allDependencies){
			if(dep.getStart().equals(pack)){
				startWithPack.add(dep);
			}
		}
		
		return startWithPack;
	}
	
	@Override
	public String toString(){
		return "Strongly connected Component";
	}
	
	public String getPackageNames(){
		//get packagenames
		String packageNames=new String();
		for(IPackageFragment jEle: packages){
			packageNames+=" ;"+((IPackageFragment)jEle).getElementName();
		}
		
		return packageNames;
	}

	
	/*
	public StronglyConnectedComponent(LinkedList<TarjanNode<IPackageFragment>> packages){
		assert packages!=null;
		assert packages.size()>=2;
		
		this.packages=new LinkedList<IPackageFragment>(packages);
	}*/
}
