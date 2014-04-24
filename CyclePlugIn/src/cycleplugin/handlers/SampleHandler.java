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
import cycleplugin.Dependancy;
import cycleplugin.LookUp;
import cycleplugin.LookUpInterface;
import cycleplugin.SearchRequestorMethodDeclarations;
import cycleplugin.SearchRequestorMethodDeclarations.MethodAndDecPackage;
import cycleplugin.SearchRequestorMatchFound;
import cycleplugin.LookUpInterface;
import cycleplugin.TarjanEdge;
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
		
		LinkedList<Dependancy> dependencies=searchDependencies(IJavaSearchConstants.REFERENCES, makeTypeLookup2(packages));
		
		//filter jprojects out of projects 
		String projectNames=projectToSearch.getProject().getName();
		
		//get packages in selected folders
		String packageNames=new String();
		for(IPackageFragment jEle: packages){
			packageNames+=" ;"+((IPackageFragment)jEle).getElementName();
		}
		
		String depStr=new String();
		for(Dependancy dep : dependencies){
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
		
		
		
		//transform dependencies to TarjanEdges&TarjanNodes (only packages with dependencies are considered, makes sense
		// as only these can even form strong connections)
		HashMap<IPackageFragment, TarjanNode<IPackageFragment>> map=new HashMap<IPackageFragment, TarjanNode<IPackageFragment>>();
		LinkedList<TarjanNode<IPackageFragment>> nodes=new LinkedList<TarjanNode<IPackageFragment>>();
		for(Dependancy dep : dependencies){
			if(!map.containsKey(dep.getStart())){
				map.put(dep.getStart(), new TarjanNode<IPackageFragment>(dep.getStart()));
				nodes.add(map.get(dep.getStart()));
			}
			if(!map.containsKey(dep.getEnd())){
				map.put(dep.getEnd(), new TarjanNode<IPackageFragment>(dep.getEnd()));
				nodes.add(map.get(dep.getEnd()));
			}
		}
		LinkedList<TarjanEdge<IPackageFragment>> edges=new LinkedList<TarjanEdge<IPackageFragment>>();
		for(Dependancy dep : dependencies){
			edges.add(new TarjanEdge<IPackageFragment>(map.get(dep.getStart()), map.get(dep.getEnd())));
		}
		
		LinkedList<LinkedList<TarjanNode<IPackageFragment>>> strongConnections=tarjan(nodes, edges);
		String sCon=new String();
		for(LinkedList<TarjanNode<IPackageFragment>> connections : strongConnections){
			for(TarjanNode<IPackageFragment> node : connections){
				sCon+=node.getNode().getElementName()+" - ";
			}
			sCon+=" || ";
		}
		
		LinkedList<LinkedList<TarjanNode<IPackageFragment>>> cycles=new LinkedList<LinkedList<TarjanNode<IPackageFragment>>>();
		for(LinkedList<TarjanNode<IPackageFragment>> strongComp : strongConnections){
			
			LinkedList<LinkedList<TarjanNode<IPackageFragment>>> result=findCyclesInStrConComponent(strongComp, edges);
			cycles.addAll(result);
		}
		
		String cycleString=new String();
		for(LinkedList<TarjanNode<IPackageFragment>> cycle : cycles){
			for(TarjanNode<IPackageFragment> node: cycle){
				cycleString+=node.getNode().getElementName() +" - ";
			}
			cycleString+="\n";
		}
		
		cycleView.display("Projects:"+projectNames+"\n"
				+" Packages:"+packageNames+"\n"
				+" Dependencies: "+depStr+"\n"
				+" StrongCons: "+sCon+"\n"
				+" Cycles: "+cycleString);
		
		return null;	
	}
	
	public LinkedList<LinkedList<TarjanNode<IPackageFragment>>> findCyclesInStrConComponent(LinkedList<TarjanNode<IPackageFragment>> nodes, LinkedList<TarjanEdge<IPackageFragment>> edges){
		assert nodes!=null;
		assert nodes.size()>0;
		assert edges!=null;
		
		LinkedList<LinkedList<TarjanNode<IPackageFragment>>> result=new LinkedList<LinkedList<TarjanNode<IPackageFragment>>>();
		for (TarjanNode<IPackageFragment> node : nodes){

			findCylce(result, node, node, new LinkedList<TarjanNode<IPackageFragment>>(), edges, 0, nodes.size());
		}
		return result;
	}
	
	public void findCylce(LinkedList<LinkedList<TarjanNode<IPackageFragment>>> result, TarjanNode<IPackageFragment> startingNode, TarjanNode<IPackageFragment> currentNode, LinkedList<TarjanNode<IPackageFragment>> visitedNodes, LinkedList<TarjanEdge<IPackageFragment>> edges, int deapth, int maxDeapth){
		assert startingNode!=null;
		assert visitedNodes!=null;
		assert result!=null;
		
		if (currentNode==startingNode&&visitedNodes.size()>0){
			boolean foundMatchingCycle=false;
			for(LinkedList<TarjanNode<IPackageFragment>> detectedCycle : result){
				if (detectedCycle.containsAll(visitedNodes)){
					foundMatchingCycle=true;
				}
			}
			if(foundMatchingCycle==false){
				result.add(visitedNodes);
			}
			//return visitedNodes;
		}
		else if(visitedNodes.contains(currentNode)){
			//return null;
		}
		else if(deapth>=maxDeapth){
			//return null;
		}
		else{
			LinkedList<TarjanEdge<IPackageFragment>> outgoingEdges=findEdgesStartingWithNode(currentNode, edges);
			for(TarjanEdge<IPackageFragment> edge : outgoingEdges){
				LinkedList<TarjanNode<IPackageFragment>> visitedNodesNew=new LinkedList<TarjanNode<IPackageFragment>>(visitedNodes);
				visitedNodesNew.add(currentNode);
				findCylce(result, startingNode, edge.getEnd(), visitedNodesNew, edges, deapth+1, maxDeapth);
			}
		}
		
	}
	
	public LinkedList<LinkedList<TarjanNode<IPackageFragment>>> tarjan(LinkedList<TarjanNode<IPackageFragment>> nodes, LinkedList<TarjanEdge<IPackageFragment>> edges){
		assert nodes!=null;
		assert edges!=null;
		
		Integer index=0;
		Stack<TarjanNode<IPackageFragment>> s=new Stack<TarjanNode<IPackageFragment>>();
		LinkedList<LinkedList<TarjanNode<IPackageFragment>>> resultingConnections=new LinkedList<LinkedList<TarjanNode<IPackageFragment>>>();
		
		for(TarjanNode<IPackageFragment> node : nodes){
			if(!node.isIndexSet()){
				LinkedList<TarjanNode<IPackageFragment>> connec=strongConnect(node, edges, index, s);
				if (connec!=null){
					resultingConnections.add(connec);
				}
			}
		}
		
		return resultingConnections;
	}
	
	private LinkedList<TarjanEdge<IPackageFragment>> findEdgesStartingWithNode(TarjanNode<IPackageFragment> node, LinkedList<TarjanEdge<IPackageFragment>> edges){
		assert node!=null;
		assert edges!=null;
		
		LinkedList<TarjanEdge<IPackageFragment>> startWithNode=new LinkedList<TarjanEdge<IPackageFragment>>(); 
		for (TarjanEdge<IPackageFragment> edge : edges){
			if(edge.getStart().equals(node)){
				startWithNode.add(edge);
			}
		}
		
		return startWithNode;
	}
	
	private LinkedList<TarjanNode<IPackageFragment>> strongConnect(TarjanNode<IPackageFragment> node, LinkedList<TarjanEdge<IPackageFragment>> edges, Integer index, Stack<TarjanNode<IPackageFragment>> s){
		assert node!=null;
		
		node.setIndex(index);
		node.setlowestConnecedIndex(index);
		index++;
		s.push(node);
		
		LinkedList<TarjanEdge<IPackageFragment>> startWithNode=findEdgesStartingWithNode(node, edges);
		
		for (TarjanEdge<IPackageFragment> edge : startWithNode){
			if(!edge.getEnd().isIndexSet()){
				strongConnect(edge.getEnd(), edges, index, s);
				edge.getStart().setlowestConnecedIndex(Math.min(edge.getStart().getlowestConnecedIndex(), edge.getEnd().getlowestConnecedIndex()));
			}
			else if(s.contains(edge.getEnd())){
				edge.getStart().setlowestConnecedIndex(Math.min(edge.getStart().getlowestConnecedIndex(), edge.getEnd().getIndex()));
			}
		}
		
		if(node.getIndex()==node.getlowestConnecedIndex()){
			LinkedList<TarjanNode<IPackageFragment>> connectedComponent=new LinkedList<TarjanNode<IPackageFragment>>();
			TarjanNode<IPackageFragment> toAdd;
			do{
				toAdd=s.pop();
				connectedComponent.add(toAdd);
				
			}while(toAdd!=node);
			
			return connectedComponent;
		}
		return null;
	}
	
	/*
	private void getCycles(LinkedList<Dependancy> dependencies) throws ExecutionException{
		assert dependencies!=null;
		
		//build "nodes", dependencies are "edges"
		LinkedList<IPackageFragment> packages=new LinkedList<IPackageFragment>();
		for (Dependancy dependency : dependencies){
			if(packages.contains(dependency.getStart())){
				packages.add(dependency.getStart());
			}
			if(packages.contains(dependency.getEnd())){
				packages.add(dependency.getEnd());
			}
		}
		
		for(IPackageFragment pack : packages){
			checkOutNode(pack, new LinkedList<IPackageFragment>(Arrays.asList(pack)), dependencies);
			
		}
		
	}
	
	private LinkedList<IPackageFragment> checkOutNode(IPackageFragment startingPackage, IPackageFragment currentPackage, LinkedList<IPackageFragment> connectedNodes, LinkedList<Dependancy> dependancies){
		LinkedList<Dependancy> edgesWithStartCurPack=new LinkedList<Dependancy>();
		for(Dependancy dep : dependancies){
			if(dep.getStart().equals(currentPackage)){
				edgesWithStartCurPack.add(dep);
			}
		}
		
		for(Dependancy dep : edgesWithStartCurPack){
			
			checkOutNode(startingPackage, currentPackage, connectedNodes, dependancies);
			
		}
		
		
	}*/
	
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

	
	private LinkedList<Dependancy> searchDependencies(int searchConstant, LookUpInterface lookUp) throws ExecutionException{
		LinkedList<Dependancy> result=new LinkedList<Dependancy>();
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
					    	result.add(new Dependancy(pack, lookUp.getPackage(jEle)));
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
