package cycleplugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.search.MethodDeclarationMatch;
import org.eclipse.jdt.core.search.MethodReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

import cycleplugin.SearchRequestorMethodDeclarations.MethodAndDecPackage;

public class SearchRequestorMatchFound extends SearchRequestor {

	Boolean foundRefence=false;
	
	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
		foundRefence=true;
	}

	public Boolean wasReferenceFound(){
		return foundRefence;
	}
}
