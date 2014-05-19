package searchHelper;

import java.util.LinkedList;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;


/*
 * Filters all the type declarations out of some packages
 * and adds them to the LookUp
 */
public class LookUpType extends LookUp {

	public LookUpType(LinkedList<IPackageFragment> packages){
		super();
		
		makeTypeLookup(packages);
	}
	
	/*
	 * Add all type declarations to in packages to the LookUp
	 */
	private void makeTypeLookup(LinkedList<IPackageFragment> packages){
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
						this.add(typeDeclaration, pack);
					}
				}		
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}
}
