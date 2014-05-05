package cycleplugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

public class SearchRequestorMatchFound extends SearchRequestor {

	Boolean foundRefence=false;
	
	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
		if(match.getAccuracy()==SearchMatch.A_ACCURATE){
			foundRefence=true;
		}
	}

	public Boolean wasReferenceFound(){
		return foundRefence;
	}
}
