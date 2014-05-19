package dependencyStructures;

import org.eclipse.jdt.core.IPackageFragment;

/*
 * Represents a package dependency
 * (basically is a directed edge)
 */
public class Dependency{
	private IPackageFragment start;
	private IPackageFragment end;
	
	public Dependency(IPackageFragment start, IPackageFragment end){
		assert start!=null;
		assert end!=null;
		
		this.start=start;
		this.end=end;
	}
	
	public IPackageFragment getStart(){
		return start;
	}
	
	public IPackageFragment getEnd(){
		return end;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Dependency other = (Dependency) obj;
		if ((!start.equals(other.getStart())) || (!end.equals(other.getEnd()))){
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode(){
		return start.hashCode()+end.hashCode();
	}
	
	@Override
	public String toString(){
		return "Dependency";
	}
	
}
