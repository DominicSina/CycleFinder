package searchHelper;

import java.util.LinkedList;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;

/*
 * Object that is used to store IJavaElements that later can be searched.
 * These IJavaElements should be methods or types.
 * Remembers an IPackageFragment in which this IJavaElement should be contained
 */
public interface LookUpInterface {

	/*
	 * Adds a IJavaElement and the IPackageFragment in which it is contained.
	 * It should only be possible to add each IJavaElement once
	 */
	public void add(IJavaElement jElement, IPackageFragment pack);

	/*
	 * Returns the IPackageFragement that was associated with this IJavaElement when it
	 * was added.
	 */
	public IPackageFragment getPackage(IJavaElement jElement);
	
	/*
	 * Returns all JElements that were added to this LookUp as a LinkedList
	 */
	public LinkedList<IJavaElement> getAllJElements();
	
	/*
	 * Returns all IPackageFragments that were added to this LookUp as a LinkedList, should
	 * only put each IPackageFragmet once in the list
	 */
	public LinkedList<IPackageFragment> getPackages();
	
}
