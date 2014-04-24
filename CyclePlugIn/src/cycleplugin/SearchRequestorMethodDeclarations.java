package cycleplugin;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.MethodDeclarationMatch;
import org.eclipse.jdt.core.search.MethodReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

public class SearchRequestorMethodDeclarations extends SearchRequestor{
	private LinkedList<MethodAndDecPackage> result=new LinkedList<MethodAndDecPackage>();
	 
	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
		if (match instanceof MethodDeclarationMatch) {
			MethodDeclarationMatch methodMatch = (MethodDeclarationMatch) match;
			IPackageFragment pack =(IPackageFragment)getIJavaElement(methodMatch.getResource().getFullPath()).getParent(); //creates now Element, problems with equals() perhaps
			IMethod method=(IMethod)methodMatch.getElement(); //this works
			
			MethodAndDecPackage methPack=new MethodAndDecPackage(pack, method);
			result.add(methPack);
		}
	}
	 
	private IJavaElement getIJavaElement(IPath path){
		IResource resource=ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		if (resource == null) {
			return null;
		}
		IJavaElement javaElement=JavaCore.create(resource);
		if (javaElement == null) {
			return null;
		}
		return javaElement;
	}
		 
	
	public LinkedList<MethodAndDecPackage> getResult() {
		return result;
	}
	 
	public class MethodAndDecPackage{
		IPackageFragment declarationPack;
		IMethod method;
		
		public MethodAndDecPackage(IPackageFragment declarationPack, IMethod method){
			assert declarationPack!=null;
			assert method!=null;
			
			this.declarationPack=declarationPack;
			this.method=method;
		}
		
		public IMethod getMethod(){
			return method;
		}
		
		public IPackageFragment getDeclarationPAck(){
			return declarationPack;
		}
	}
}


