package cycleplugin;

import java.util.LinkedList;

import org.eclipse.jdt.core.IPackageFragment;

public class Cycle {

	private LinkedList<IPackageFragment> packages;
	private LinkedList<Dependency> dependencies;
	
	public Cycle(LinkedList<IPackageFragment> packages){
		assert packages!=null;
		assert packages.size()>=2;
		
		this.packages=packages;
		
		dependencies=new LinkedList<Dependency>();
		for(int i=0; i<packages.size(); i++){
			
			dependencies.add(new Dependency(packages.get(i), packages.get((i+1)%packages.size())));
		}
	}
	
	public String getPackageNames(){
		//get packagenames
		String packageNames=new String();
		for(IPackageFragment jEle: packages){
			packageNames+=" ;"+((IPackageFragment)jEle).getElementName();
		}
		
		return packageNames;
	}
	
	@Override
	public String toString(){
		return "Cycle";
	}
	
	public LinkedList<Dependency> getDependencies(){
		return dependencies;
	}
}
