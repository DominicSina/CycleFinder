package cycleplugin;

import java.util.LinkedList;

import javax.swing.CellEditor;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.internal.console.ConsoleView;
import org.eclipse.ui.part.ViewPart;

public class CycleDisplayer extends ViewPart {

	
	private Text text;
	private TreeViewer treeViewer;
	private Tree tree;
	
	public CycleDisplayer() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		treeViewer=new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL
			      | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		
		createColumns();

		tree=treeViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		
		treeViewer.setContentProvider(new TreeNodeContentProvider());
				
		// define layout for the viewer
	    GridData gridData = new GridData();
	    gridData.verticalAlignment = GridData.FILL;
	    gridData.horizontalSpan = 2;
	    gridData.grabExcessHorizontalSpace = true;
	    gridData.grabExcessVerticalSpace = true;
	    gridData.horizontalAlignment = GridData.FILL;
	    treeViewer.getControl().setLayoutData(gridData);
	    
	    treeViewer.refresh();
	}
	
	private void createColumns(){
		// create a column for the first name
		TreeViewerColumn descriptionVCol = new TreeViewerColumn(treeViewer, SWT.NONE);
		TreeColumn descriptionCol = descriptionVCol.getColumn();
	    descriptionCol.setResizable(true);
	    descriptionCol.setMoveable(true);

		
		descriptionVCol.getColumn().setWidth(200);
		descriptionVCol.getColumn().setText("Description");
		descriptionVCol.setLabelProvider(new ColumnLabelProvider() {
		  @Override
		  public String getText(Object element) {
		    return ((TreeNode)element).getValue().toString();
		  }
		});
		
		TreeViewerColumn packagesVCol = new TreeViewerColumn(treeViewer, SWT.NONE);
		TreeColumn packagesCol = descriptionVCol.getColumn();
		packagesCol.setResizable(true);
		packagesCol.setMoveable(true);
	    
	    packagesVCol.getColumn().setWidth(200);
	    packagesVCol.getColumn().setText("Packages");
	    packagesVCol.setLabelProvider(new ColumnLabelProvider() {
		  @Override
		  public String getText(Object element) {
			  TreeNode node=(TreeNode)element;
			  if(node.getValue() instanceof Cycle){
				  return ((Cycle)node.getValue()).getPackageNames();
			  }
			  else if(node.getValue() instanceof StronglyConnectedComponent){
				  return ((StronglyConnectedComponent)node.getValue()).getPackageNames();
			  }
			  else if(node.getValue() instanceof Dependency){
				  Dependency dep=(Dependency)node.getValue();
				  return ""+dep.getStart().getElementName()+" --> "+dep.getEnd().getElementName();
			  }
			  return null;
		  }
		});

	    TreeViewerColumn locationVCol = new TreeViewerColumn(treeViewer, SWT.NONE);
		TreeColumn locationCol = descriptionVCol.getColumn();
		locationCol.setResizable(true);
		locationCol.setMoveable(true);

	    locationVCol.getColumn().setWidth(200);
	    locationVCol.getColumn().setText("Location");
	    locationVCol.setLabelProvider(new ColumnLabelProvider() {
		  @Override
		  public String getText(Object element) {
		    return "ABC";
		  }
		});
	}
	
	
	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}
	
	public void display(LinkedList<StronglyConnectedComponent> comps){
		assert comps!=null;
		
		LinkedList<TreeNode> compNodes=new LinkedList<TreeNode>();
		for(StronglyConnectedComponent comp : comps){
			TreeNode node=new TreeNode(comp); 
			compNodes.add(node);
			
			LinkedList<Cycle> cycles=comp.findCycles();
			TreeNode[] cycleNodes=new TreeNode[cycles.size()];
			for(int i=0; i<cycles.size();i++){
				cycleNodes[i]=new TreeNode(cycles.get(i));
			}
			node.setChildren(cycleNodes);
			
			if(node.getChildren()!=null){	
				for(TreeNode cycleNode : node.getChildren()){
					LinkedList<Dependency> dependencies=((Cycle)cycleNode.getValue()).getDependencies();
					
					TreeNode[] depNodes=new TreeNode[dependencies.size()];
					for(int i=0;i<dependencies.size();i++){
						depNodes[i]=new TreeNode(dependencies.get(i));
					}
					cycleNode.setChildren(depNodes);
				}
			}
		}
		
		
		TreeNode[] compNodeArray=new TreeNode[compNodes.size()];
		for(int i=0;i<compNodes.size();i++){
			compNodeArray[i]=compNodes.get(i);
		}
		
		treeViewer.setInput(compNodeArray);
		treeViewer.refresh();
	}
	
	

}
