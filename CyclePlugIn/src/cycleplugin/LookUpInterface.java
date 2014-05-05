package cycleplugin;

import java.util.LinkedList;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;

public interface LookUpInterface {

	public void add(IJavaElement jElement, IPackageFragment pack);

	public IPackageFragment getPackage(IJavaElement jElement);
	
	public LinkedList<IJavaElement> getAllJElements();
	
	public LinkedList<IPackageFragment> getPackages();
	
}
