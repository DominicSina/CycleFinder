package searchHelper;

import java.util.Arrays;
import java.util.LinkedList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import searchHelper.SearchRequestorMatchInformation.MatchInformation;
import dependencyStructures.Dependency;

/*
 * This class masks the interactions with the SearchEngine.
 */
public class Searcher {
	
	//the packages where we want to search for IJavaElements
	private LinkedList<IPackageFragment> packagesToSearch;
	
	//the packages where the IJavaElements we want to search are contained
	private LinkedList<IPackageFragment> packagesToSearchFrom;
	private SearchEngine JDTSearchProvider=new SearchEngine();
	
	/*
	 * Builds packagesToSearch and packagesToSearchFrom based on an IJavaProject.
	 */
	public Searcher(IJavaProject jproject){
		assert jproject!=null;
		
		LinkedList<IPackageFragmentRoot> foldersToSearch=getSrcFolders(jproject);
		LinkedList<IPackageFragment> packagesToSearch=new LinkedList<IPackageFragment>();
		for(IPackageFragmentRoot folderToSearch : foldersToSearch){
			packagesToSearch.addAll(getPackageFragments(folderToSearch));
		}
		this.packagesToSearch=packagesToSearch;
		this.packagesToSearchFrom=packagesToSearch;
		
	}

	/*
	 * Builds packagesToSearch and packagesToSearchFrom based on a Dependency.
	 */
	public Searcher(Dependency dependency){
		assert dependency!=null;
		
		LinkedList<IPackageFragment> packages=new LinkedList<IPackageFragment>();
		packages.add(dependency.getStart());
		
		packagesToSearch=packages;
	
		packages=new LinkedList<IPackageFragment>();
		packages.add(dependency.getEnd());
		
		packagesToSearchFrom=packages;
	}
	
	/*
	 * Returns a list of dependencies caused by methods
	 * searchConstant: use IJavaSearchConstants here
	 */
	public LinkedList<Dependency> searchDependenciesToMethod(int searchConstant){
		return searchDependencies(searchConstant, new LookUpMethod(packagesToSearchFrom));
	}

	/*
	 * Returns a list of dependencies caused by types
	 * searchConstant: use IJavaSearchConstants here
	 */
	public LinkedList<Dependency> searchDependenciesToType(int searchConstant){
		return searchDependencies(searchConstant, new LookUpType(packagesToSearchFrom));
	}
	
	/*
	 * Returns a list with detailed MatchInformation.
	 * Searches references to all methods and all types contained in packagesToSearchFrom
	 * in packagesToSearch
	 */
	public LinkedList<MatchInformation> searchAllDetailedDependencies(){
		LinkedList<MatchInformation> result=new LinkedList<MatchInformation>();
		
		result.addAll(searchDetailedDependencies(IJavaSearchConstants.REFERENCES, new LookUpType(packagesToSearchFrom)));
		result.addAll(searchDetailedDependencies(IJavaSearchConstants.REFERENCES, new LookUpMethod(packagesToSearchFrom)));
		
		return result;
	}
	
	/*
	 * Searches all IJavaElements contained in lookUp in packagesToSearch.
	 * Uses searchConstant (use IJavaSearchConstants) as limiter for what to search.
	 * Returns a LinkedList with MatchInformation
	 */
	private LinkedList<MatchInformation> searchDetailedDependencies(int searchConstant,LookUpInterface lookUp){
		assert lookUp!=null;
		
		LinkedList<MatchInformation> result=new LinkedList<MatchInformation>();
		LinkedList<IJavaElement> allElement=lookUp.getAllJElements();

		try {
			for (IPackageFragment pack : packagesToSearch){
			    // check each method.
				for (IJavaElement jEle : allElement) {
					
					//no need to search package where method was declared
					if(lookUp.getPackage(jEle)!=pack){

						// Finds the references of the method
					    // Create search pattern
					    SearchPattern pattern = SearchPattern.createPattern(jEle, searchConstant);
					    	
					    // Create search scope	
					    IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pack});
								  
					    //create requestor
					    SearchRequestorMatchInformation requestor=new SearchRequestorMatchInformation(pattern);
					        	  
					    JDTSearchProvider.search(pattern, new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()}, scope, requestor, null);
					        	 
					    result.addAll(requestor.getResult());
					}
				}
			}
		}catch (CoreException e) {
			e.printStackTrace();
		}
		return result;
	}

	/*
	 * Searches all IJavaElements contained in lookUp in packagesToSearch.
	 * Uses searchConstant (use IJavaSearchConstants) as limiter for what to search.
	 * Returns a LinkedList with Dependencies
	 */
	private LinkedList<Dependency> searchDependencies(int searchConstant, LookUpInterface lookUp){
		LinkedList<Dependency> result=new LinkedList<Dependency>();
		LinkedList<IJavaElement> allElement=lookUp.getAllJElements();

		try {
			for (IPackageFragment pack : lookUp.getPackages()){
			    // check each method.
				for (IJavaElement jEle : allElement) {
					
					//no need to search package where method was declared
					if(lookUp.getPackage(jEle)!=pack){

						// Finds the references of the method
					    // Create search pattern
					    SearchPattern pattern = SearchPattern.createPattern(jEle, searchConstant);
					       
					    // Create search scope	
					    IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pack});
								  
					    //create requestor
					    SearchRequestorMatchFound requestor=new SearchRequestorMatchFound();
					        	  
					    JDTSearchProvider.search(pattern, new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()}, scope, requestor, null);
					        	 
					    if(requestor.wasReferenceFound()){
					    	result.add(new Dependency(pack, lookUp.getPackage(jEle)));
					    }
					}
				}
			}
		}catch (CoreException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/*
	 * returns all PackageFragmentRoots inside a project
	 */
	private LinkedList<IPackageFragmentRoot> getPackageFragmentRoots(IJavaProject jprojects){
		assert jprojects!=null;
		
		LinkedList<IPackageFragmentRoot> result=new LinkedList<IPackageFragmentRoot>();
		
		try {
			//TODO only gets packageFragRoot from one project
			IPackageFragmentRoot[] packFragRoots = jprojects.getAllPackageFragmentRoots();
			result=new LinkedList<>(Arrays.asList(packFragRoots));
			
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/*
	 * Gets all SourceFolders inside of a java project
	 */
	private LinkedList<IPackageFragmentRoot> getSrcFolders(IJavaProject jProj){
		//get folders&archives
		LinkedList<IPackageFragmentRoot> packFragRoots=getPackageFragmentRoots(jProj);
				
		//determine folders to search -> src for now
		LinkedList<IPackageFragmentRoot> foldersToSearch = new LinkedList<IPackageFragmentRoot>();
		for (IPackageFragmentRoot folder:packFragRoots){
			//if (folder.getElementName().equals("src")&&!folder.isArchive()){
			try {
				if (folder.getKind()==IPackageFragmentRoot.K_SOURCE&&!folder.isArchive()){	
					foldersToSearch.add(folder);
					System.out.println("src folder found");
				}
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return foldersToSearch;
	}
	
	/*
	 * Returns all packages inside of a source folder
	 */
	private LinkedList<IPackageFragment> getPackageFragments(IPackageFragmentRoot sourceToSearch){
		assert sourceToSearch!=null;
		
		LinkedList<IPackageFragment> result=new LinkedList<>();
		try {
			IJavaElement[] fragments=sourceToSearch.getChildren();
			for (IJavaElement jEle : fragments){
				result.add((IPackageFragment)jEle);
			}
			
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return result;
	}
}
