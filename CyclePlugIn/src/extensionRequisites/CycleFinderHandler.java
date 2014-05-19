package extensionRequisites;

import java.util.LinkedList;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.*;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;

import searchHelper.Searcher;
import tarjan.TarjanGraph;
import dependencyStructures.Dependency;
import dependencyStructures.StronglyConnectedComponent;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class CycleFinderHandler extends AbstractHandler {
	
	/**
	 * The constructor.
	 */
	public CycleFinderHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context. 
	 * @throws ExecutionException 
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		//gets the selected Project
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
			        .getActiveSite(event).getSelectionProvider().getSelection();
		IJavaProject projectToSearch=(IJavaProject)selection.getFirstElement();
		
		//finds ALL dependencies
		Searcher searcher=new Searcher(projectToSearch);
		LinkedList<Dependency> dependencies=searcher.searchDependenciesToType(IJavaSearchConstants.REFERENCES);
		
		
		//transfer dependencies to a tarjan graph and find the strongly connected components on it
		TarjanGraph tGraph=new TarjanGraph(dependencies); 
		LinkedList<StronglyConnectedComponent> components=tGraph.tarjan();
		
		//display view
		CycleDisplayer cycleView;
		try{
			ConsolePlugin plugin = ConsolePlugin.getDefault();
			cycleView=(CycleDisplayer)plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("CycleFinder.view");
		} catch (PartInitException e) {
			e.printStackTrace();
			throw new ExecutionException("ViewPart not found");
		}
						
		//give View the necessary data
		cycleView.display(components);
		return null;
	}

}
