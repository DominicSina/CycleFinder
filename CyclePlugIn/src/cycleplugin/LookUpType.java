package cycleplugin;

import java.util.LinkedList;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public class LookUpType extends LookUp {


	public LookUpType(LinkedList<IPackageFragment> packages){
		super();
		
		makeTypeLookup(packages);
	}
	
	
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
