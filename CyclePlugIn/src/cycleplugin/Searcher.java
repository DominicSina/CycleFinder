package cycleplugin;

import java.util.Arrays;
import java.util.LinkedList;

import javax.naming.directory.SearchControls;

import org.eclipse.core.commands.ExecutionException;
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
import org.eclipse.jdt.internal.ui.IJavaStatusConstants;

import cycleplugin.SearchRequestorMatchInformation.MatchInformation;

public class Searcher {
	
	private LinkedList<IPackageFragment> packagesToSearch;
	private LinkedList<IPackageFragment> packagesWithJElementsToSearchFor;
	private SearchEngine JDTSearchProvider=new SearchEngine();
	
	public Searcher(IJavaProject jproject){
		assert jproject!=null;
		
		LinkedList<IPackageFragmentRoot> foldersToSearch=getSrcFolders(jproject);
		LinkedList<IPackageFragment> packagesToSearch=new LinkedList<IPackageFragment>();
		for(IPackageFragmentRoot folderToSearch : foldersToSearch){
			packagesToSearch.addAll(getPackageFragments(folderToSearch));//TODO not sure if all children of IPackageFragmentRoots are IPackageFragments
		}
		
		this.packagesToSearch=packagesToSearch;
	}
	
	public Searcher(Dependency dependency){
		assert dependency!=null;
		
		LinkedList<IPackageFragment> packages=new LinkedList<IPackageFragment>();
		packages.add(dependency.getStart());
		
		packagesToSearch=packages;
	
		packages=new LinkedList<IPackageFragment>();
		packages.add(dependency.getEnd());
		
		packagesWithJElementsToSearchFor=packages;
	}
	
	public LinkedList<Dependency> searchDependenciesToMethod(int searchConstant){
		return searchDependencies(searchConstant, new LookUpMethod(packagesToSearch));
	}
	
	public LinkedList<Dependency> searchDependenciesToType(int searchConstant){
		return searchDependencies(searchConstant, new LookUpType(packagesToSearch));
	}
	
	public LinkedList<MatchInformation> searchAllDetailedDependencies(){
		LinkedList<MatchInformation> result=new LinkedList<MatchInformation>();
		
		/*
		result.addAll(searchDetailedDependencies(IJavaSearchConstants.LOCAL_VARIABLE_DECLARATION_TYPE_REFERENCE, new LookUpType(packagesToSearch)));
		//System.out.println("LOCAL_VARIABLE_DECLARATION_TYPE_REFERENCE found");
		result.addAll(searchDetailedDependencies(IJavaSearchConstants.FIELD_DECLARATION_TYPE_REFERENCE, new LookUpType(packagesToSearch)));
		//System.out.println("FIELD_DECLARATION_TYPE_REFERENCE found");
		result.addAll(searchDetailedDependencies(IJavaSearchConstants.SUPERTYPE_TYPE_REFERENCE, new LookUpType(packagesToSearch)));
		//System.out.println("SUPERTYPE_TYPE_REFERENCE found");
		result.addAll(searchDetailedDependencies(IJavaSearchConstants.PARAMETER_DECLARATION_TYPE_REFERENCE, new LookUpType(packagesToSearch)));
		//System.out.println("PARAMETER_DECLARATION_TYPE_REFERENCE found");
		result.addAll(searchDetailedDependencies(IJavaSearchConstants.RETURN_TYPE_REFERENCE, new LookUpType(packagesToSearch)));
		//System.out.println("RETURN_TYPE_REFERENCE found");
		result.addAll(searchDetailedDependencies(IJavaSearchConstants.IMPORT_DECLARATION_TYPE_REFERENCE, new LookUpType(packagesToSearch)));
		//System.out.println("IMPORT_DECLARATION_TYPE_REFERENCE found");
		result.addAll(searchDetailedDependencies(IJavaSearchConstants.METHOD, new LookUpMethod(packagesToSearch)));
		//System.out.println("METHOD found");
		*/
		result.addAll(searchDetailedDependencies(IJavaSearchConstants.REFERENCES, new LookUpType(packagesWithJElementsToSearchFor)));
		//System.out.println("LOCAL_VARIABLE_DECLARATION_TYPE_REFERENCE found");
		result.addAll(searchDetailedDependencies(IJavaSearchConstants.REFERENCES, new LookUpMethod(packagesWithJElementsToSearchFor)));
		//System.out.println("LOCAL_VARIABLE_DECLARATION_TYPE_REFERENCE found");
		
		
		
		//result.addAll(searchDetailedDependencies(IJavaSearchConstants.SUPERTYPE_TYPE_REFERENCE, new LookUpType(packagesToSearch)));
		
		
		//result.addAll(searchDetailedDependencies(IJavaSearchConstants.REFERENCES, new LookUpMethod(packagesToSearch)));
		
		return result;
	}
	
	private LinkedList<MatchInformation> searchDetailedDependencies(int searchConstant,LookUpInterface lookUp){
		assert lookUp!=null;
		
		LinkedList<MatchInformation> result=new LinkedList<MatchInformation>();
		LinkedList<IJavaElement> allElement=lookUp.getAllJElements();

		try {
			for (IPackageFragment pack : packagesToSearch){
			    // check each method.
				for (IJavaElement jEle : allElement) {
					
					//no need to check package where method was declared
					if(lookUp.getPackage(jEle)!=pack){

						// Finds the references of the method
					    // Create search pattern
					    SearchPattern pattern = SearchPattern.createPattern(jEle, searchConstant);
					    	//	CLASS_INSTANCE_CREATION_TYPE_REFERENCE);
					       
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
	
	private LinkedList<Dependency> searchDependencies(int searchConstant, LookUpInterface lookUp){
		LinkedList<Dependency> result=new LinkedList<Dependency>();
		LinkedList<IJavaElement> allElement=lookUp.getAllJElements();

		try {
			for (IPackageFragment pack : lookUp.getPackages()){
			    // check each method.
				for (IJavaElement jEle : allElement) {
					
					//no need to check package where method was declared
					if(lookUp.getPackage(jEle)!=pack){

						// Finds the references of the method
					    // Create search pattern
					    SearchPattern pattern = SearchPattern.createPattern(jEle, searchConstant);
					    //	CLASS_INSTANCE_CREATION_TYPE_REFERENCE);
					       
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
	
	private LinkedList<IPackageFragmentRoot> getSrcFolders(IJavaProject jProj){
		//get folders&archives
		LinkedList<IPackageFragmentRoot> packFragRoots=getPackageFragmentRoots(jProj);
				
		//determine folders to search -> src for now
		LinkedList<IPackageFragmentRoot> foldersToSearch = new LinkedList<IPackageFragmentRoot>();
		for (IPackageFragmentRoot folder:packFragRoots){
			//if (folder.getElementName().equals("src")&&!folder.isArchive()){
			try {
				if (folder.getKind()==folder.K_SOURCE&&!folder.isArchive()){	
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
	
	private LinkedList<IPackageFragment> getPackageFragments(IPackageFragmentRoot sourceToSearch){
		assert sourceToSearch!=null;
		
		LinkedList<IPackageFragment> result=new LinkedList<>();
		try {
			IJavaElement[] fragments=sourceToSearch.getChildren();//TODO not sure if all children of IPackageFragmentRoots are IPackageFragments
			for (IJavaElement jEle : fragments){
				result.add((IPackageFragment)jEle); //no better option to get fragments from fragmentRoots?
			}
			
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return result;
	}
}
