package cycleplugin;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;

public class LookUp implements LookUpInterface {

	HashMap<IJavaElement, IPackageFragment> lookup;
	LinkedList<IPackageFragment> packages;
	
	public LookUp(){
		lookup=new HashMap<IJavaElement, IPackageFragment>();
		packages=new LinkedList<IPackageFragment>();
	}
	
	@Override
	public void add(IJavaElement jElement, IPackageFragment pack) {
		assert jElement!=null;
		assert pack!=null;
		assert !lookup.containsKey(jElement);
		
		if(!packages.contains(pack)){
			packages.add(pack);
		}
		lookup.put(jElement, pack);
	}

	@Override
	public IPackageFragment getPackage(IJavaElement jElement) {
		assert jElement!=null;
		
		return lookup.get(jElement);
	}

	@Override
	public LinkedList<IJavaElement> getAllMethods() {
		return new LinkedList<IJavaElement>(lookup.keySet());
	}

	@Override
	public LinkedList<IPackageFragment> getPackages() {
		return packages;
	}

}
