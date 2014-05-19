package searchHelper;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;

public class LookUp implements LookUpInterface {

	//used to find out which IPackageFragment was added with which IJavaElement
	protected HashMap<IJavaElement, IPackageFragment> lookup;
	
	//all IPackageFragments that were added with an IJavaElement, contains no duplicates
	protected LinkedList<IPackageFragment> packages;
	
	public LookUp(){
		lookup=new HashMap<IJavaElement, IPackageFragment>();
		packages=new LinkedList<IPackageFragment>();
	}
	
	/*
	 * (non-Javadoc)
	 * @see searchHelper.LookUpInterface#add(org.eclipse.jdt.core.IJavaElement, org.eclipse.jdt.core.IPackageFragment)
	 */
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

	/*
	 * (non-Javadoc)
	 * @see searchHelper.LookUpInterface#getPackage(org.eclipse.jdt.core.IJavaElement)
	 */
	@Override
	public IPackageFragment getPackage(IJavaElement jElement) {
		assert jElement!=null;
		
		return lookup.get(jElement);
	}

	/*
	 * (non-Javadoc)
	 * @see searchHelper.LookUpInterface#getAllJElements()
	 */
	@Override
	public LinkedList<IJavaElement> getAllJElements() {
		return new LinkedList<IJavaElement>(lookup.keySet());
	}

	/*
	 * (non-Javadoc)
	 * @see searchHelper.LookUpInterface#getPackages()
	 */
	@Override
	public LinkedList<IPackageFragment> getPackages() {
		return packages;
	}

}
