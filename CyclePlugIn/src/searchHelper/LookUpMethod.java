package searchHelper;

import java.util.LinkedList;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/*
 * Filters all the method declarations out of the packages
 * and adds them to the lookUp 
 */
public class LookUpMethod extends LookUp {

	public LookUpMethod(LinkedList<IPackageFragment> packages){
		super();
		
		makeMethodLookup(packages);
	}
	
	
	/*
	 * Add all method declarations to in packages to the LookUp
	 */
	private void makeMethodLookup(LinkedList<IPackageFragment> packages){
		assert packages!=null;	
		try {
			for (IPackageFragment pack : packages){
			    //get all classes inside a package
				ICompilationUnit[] classes=pack.getCompilationUnits();
								
				for (ICompilationUnit unit : classes){
					// get all the type declaration of the class.
					IType[] typeDeclarationList = unit.getTypes();
					
					// get methods lists
					for (IType typeDeclaration : typeDeclarationList) {
					     IMethod[] methodList = typeDeclaration.getMethods();
					     
					     // add each method.
					     for (IMethod method : methodList) {
					    	 this.add(method, pack);
					     }
					}
				}		
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}
}
