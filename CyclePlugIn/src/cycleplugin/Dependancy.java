package cycleplugin;

import org.eclipse.jdt.core.IPackageFragment;

public class Dependancy{
	private IPackageFragment start;
	private IPackageFragment end;
	
	public Dependancy(IPackageFragment start, IPackageFragment end){
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
		final Dependancy other = (Dependancy) obj;
		if (start!=other.getStart() || end!=other.getEnd()){
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode(){
		return start.hashCode()+end.hashCode();
	}
	
	
}
