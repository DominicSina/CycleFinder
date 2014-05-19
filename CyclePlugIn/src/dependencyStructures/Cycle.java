package dependencyStructures;

import java.util.LinkedList;

import org.eclipse.jdt.core.IPackageFragment;

/*
 * Represents a package cycle
 */
public class Cycle {

	private LinkedList<IPackageFragment> packages;//the packages that this cycle consists of 
	private LinkedList<Dependency> dependencies;//the dependencies between the packages
	
	/*
	 * Just assumes that all the packages are dependent on each other in the
	 * same order that the LinkedList has
	 * => able to construct cycles that don't exist with this
	 */
	public Cycle(LinkedList<IPackageFragment> packages){
		assert packages!=null;
		assert packages.size()>=2;
		
		this.packages=packages;
		
		dependencies=new LinkedList<Dependency>();
		for(int i=0; i<packages.size(); i++){
			
			dependencies.add(new Dependency(packages.get(i), packages.get((i+1)%packages.size())));
		}
	}
	
	/*
	 * returns package names in curved brackets
	 * separated with commas
	 */
	private String getPackageNames(){
		//get packagenames
		String packageNames="{";
		packageNames+=packages.get(0).getElementName();
		for(int i=1; i<packages.size();i++){
			packageNames+=", "+packages.get(i).getElementName();
		}
		packageNames+="}";
		return packageNames;
	}
	
	@Override
	public String toString(){
		return "Cycle"+getPackageNames();
	}
	
	public LinkedList<Dependency> getDependencies(){
		return dependencies;
	}
}
