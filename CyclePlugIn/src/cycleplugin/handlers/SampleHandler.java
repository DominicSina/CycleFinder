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
import org.eclipse.swt.widgets.TypedListener;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.*;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.core.*;
import org.eclipse.jdt.core.search.*;

import cycleplugin.CycleDisplayer;
import cycleplugin.Dependency;
import cycleplugin.LookUp;
import cycleplugin.LookUpInterface;
import cycleplugin.LookUpType;
import cycleplugin.SearchRequestorMethodDeclarations;
import cycleplugin.Searcher;
import cycleplugin.StronglyConnectedComponent;
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
		
		//finds ALL dependencies
		Searcher searcher=new Searcher(projectToSearch);
		LinkedList<Dependency> dependencies=searcher.searchDependenciesToType(IJavaSearchConstants.REFERENCES);
		
		
		//transfer dependencies to a tarjan graph and find the strongly connected components on it
		TarjanGraph tGraph=new TarjanGraph(dependencies); 
		LinkedList<StronglyConnectedComponent> components=tGraph.tarjan();
		
		//create view
		CycleDisplayer cycleView;
		try{
			ConsolePlugin plugin = ConsolePlugin.getDefault();
			cycleView=(CycleDisplayer)plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("CyclePlugIn.view");
		} catch (PartInitException e) {
			e.printStackTrace();
			throw new ExecutionException("ViewPart not found");
		}
						
		//give View the necessary data
		cycleView.display(components);
		return null;
	}

}
