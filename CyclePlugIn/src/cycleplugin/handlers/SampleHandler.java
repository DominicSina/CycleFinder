package cycleplugin.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Stack;

import javax.annotation.Resources;
import javax.security.auth.login.LoginContext;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.*;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.handlers.CyclePageHandler;
import org.eclipse.ui.internal.handlers.WizardHandler.New;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.util.ResourceCompilationUnit;
import org.eclipse.core.*;
import org.eclipse.jdt.core.search.*;

import cycleplugin.CycleDisplayer;
import cycleplugin.Dependency;
import cycleplugin.LookUp;
import cycleplugin.LookUpInterface;
import cycleplugin.SearchRequestorMethodDeclarations;
import cycleplugin.StronglyConnectedComponent;
import cycleplugin.SearchRequestorMethodDeclarations.MethodAndDecPackage;
import cycleplugin.SearchRequestorMatchFound;
import cycleplugin.LookUpInterface;
import cycleplugin.TarjanEdge;
import cycleplugin.TarjanGraph;
import cycleplugin.TarjanNode;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SampleHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public SampleHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 * @throws ExecutionException 
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException{
		//TODO need to make sure this was called trough rightclick on a project
		
		//gets the selected Project
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
			        .getActiveSite(event).getSelectionProvider().getSelection();
		IJavaProject projectToSearch=(IJavaProject)selection.getFirstElement();
		
		IPackageFragmentRoot folderToSearch=getSrcFolder(projectToSearch);

		LinkedList<IPackageFragment> packages=getPackageFragments(folderToSearch);//TODO not sure if all children of IPackageFragmentRoots are IPackageFragments
		
		LinkedList<Dependency> dependencies=searchDependencies(IJavaSearchConstants.REFERENCES, makeTypeLookup2(packages));
		
		//filter jprojects out of projects 
		String projectNames=projectToSearch.getProject().getName();
		
		//get packages in selected folders
		String packageNames=new String();
		for(IPackageFragment jEle: packages){
			packageNames+=" ;"+((IPackageFragment)jEle).getElementName();
		}
		
		String depStr=new String();
		for(Dependency dep : dependencies){
			depStr+=dep.getStart().getElementName()+"->"+dep.getEnd().getElementName()+"  \n ";	
		}
		
		CycleDisplayer cycleView;
		try{
			ConsolePlugin plugin = ConsolePlugin.getDefault();
			cycleView=(CycleDisplayer)plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("CyclePlugIn.view");
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ExecutionException("ViewPart not found");
		}
		
		TarjanGraph tGraph=new TarjanGraph(dependencies); 
		LinkedList<StronglyConnectedComponent> components=tGraph.tarjan();
		
		cycleView.display(components);
		return null;
	}
	
	private IPackageFragmentRoot getSrcFolder(IJavaProject jProj) throws ExecutionException{
		//get folders&archives
		LinkedList<IPackageFragmentRoot> packFragRoots=getPackageFragmentRoots(jProj);
				
		//determine folders to search -> src for now
		IPackageFragmentRoot folderToSearch = null;
		for (IPackageFragmentRoot folder:packFragRoots){
			if (folder.getElementName().equals("src")&&!folder.isArchive()){
				folderToSearch=folder;
				System.out.println("src folder found");
			}
		}
		if (folderToSearch==null){
			throw new ExecutionException("no src folder found");
		}
		return folderToSearch;
	}
	
	
	private LookUpInterface makeTypeLookup2(LinkedList<IPackageFragment> packages) throws ExecutionException{
		assert packages!=null;
		
		LookUpInterface lookup=new LookUp();
		
		try {
			for (IPackageFragment pack : packages){
			    //get all classes inside a package
				ICompilationUnit[] classes=pack.getCompilationUnits();
								
				for (ICompilationUnit unit : classes){
					// get all the type declaration of the class.
					IType[] typeDeclarationList = unit.getTypes();

					// get methods lists
					for (IType typeDeclaration : typeDeclarationList) {
						lookup.add(typeDeclaration, pack);
					}
				}		
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
			throw new ExecutionException(e.getMessage(),e.getCause());
		}
		catch (CoreException e) {
			e.printStackTrace();
			throw new ExecutionException(e.getMessage(),e.getCause());
		}
		
		return lookup;
	}

	private LookUpInterface makeMethodLookup2(LinkedList<IPackageFragment> packages) throws ExecutionException{
		assert packages!=null;
		
		LookUpInterface lookup=new LookUp();
		
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
					    	 
					    	 lookup.add(method, pack);
					    	 
					     }
					}
				}		
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
			throw new ExecutionException(e.getMessage(),e.getCause());
		}
		catch (CoreException e) {
			e.printStackTrace();
			throw new ExecutionException(e.getMessage(),e.getCause());
		}
		
		return lookup;
	}

	
	private LinkedList<Dependency> searchDependencies(int searchConstant, LookUpInterface lookUp) throws ExecutionException{
		LinkedList<Dependency> result=new LinkedList<Dependency>();
		LinkedList<IJavaElement> allElement=lookUp.getAllMethods();

		try {
			for (IPackageFragment pack : lookUp.getPackages()){
			    // check each method.
				for (IJavaElement jEle : allElement) {
					
					//no need to check package where method was declared
					if(lookUp.getPackage(jEle)!=pack){

						// Finds the references of the method
					    SearchEngine JDTSearchProvider=new SearchEngine();
					    
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
		} catch (JavaModelException e) {
			e.printStackTrace();
			throw new ExecutionException(e.getMessage(),e.getCause());
		}
		catch (CoreException e) {
			e.printStackTrace();
			throw new ExecutionException(e.getMessage(),e.getCause());
		}
		return result;
	}
	
	private LinkedList<IPackageFragmentRoot> getPackageFragmentRoots(IJavaProject jprojects) throws ExecutionException{
		assert jprojects!=null;
		
		try {
			//TODO only gets packageFragRoot from one project
			IPackageFragmentRoot[] packFragRoots = jprojects.getAllPackageFragmentRoots();
			LinkedList<IPackageFragmentRoot> result=new LinkedList<>(Arrays.asList(packFragRoots));
			return result;
			
		} catch (JavaModelException e) {
			e.printStackTrace();
			throw new ExecutionException(e.getMessage(),e.getCause());
		}
	}
	
	private LinkedList<IPackageFragmentRoot> getPackageFragmentRoots(LinkedList<IJavaProject> jprojects) throws ExecutionException{
		assert jprojects!=null;
		
		try {
			//TODO only gets packageFragRoot from one project
			IPackageFragmentRoot[] packFragRoots = jprojects.get(0).getAllPackageFragmentRoots();
			LinkedList<IPackageFragmentRoot> result=new LinkedList<>(Arrays.asList(packFragRoots));
			return result;
			
		} catch (JavaModelException e) {
			e.printStackTrace();
			throw new ExecutionException(e.getMessage(),e.getCause());
		}
	}
	
	private LinkedList<IPackageFragment> getPackageFragments(IPackageFragmentRoot sourceToSearch) throws ExecutionException{
		assert sourceToSearch!=null;
		
		try {
			IJavaElement[] fragments=sourceToSearch.getChildren();//TODO not sure if all children of IPackageFragmentRoots are IPackageFragments
			LinkedList<IPackageFragment> result=new LinkedList<>();
			for (IJavaElement jEle : fragments){
				result.add((IPackageFragment)jEle); //no better option to get fragments from fragmentRoots?
			}
			return result;
			
		} catch (JavaModelException e) {
			e.printStackTrace();
			throw new ExecutionException(e.getMessage(),e.getCause());
		}
	}
	
	private LinkedList<IPackageFragment> getPackageFragments(LinkedList<IPackageFragmentRoot> sourcesToSearch) throws ExecutionException{
		assert sourcesToSearch!=null;
		
		try {
			//TODO seems to only return the default package
			IJavaElement[] fragments=sourcesToSearch.get(0).getChildren();//TODO not sure if all children of IPackageFragmentRoots are IPackageFragments
			LinkedList<IPackageFragment> result=new LinkedList<>();
			for (IJavaElement jEle : fragments){
				result.add((IPackageFragment)jEle); //no better option to get fragments from fragmentRoots?
			}
			return result;
			
		} catch (JavaModelException e) {
			e.printStackTrace();
			throw new ExecutionException(e.getMessage(),e.getCause());
		}
	}
}
