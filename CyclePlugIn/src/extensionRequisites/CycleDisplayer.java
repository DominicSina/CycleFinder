package extensionRequisites;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import searchHelper.Searcher;
import searchHelper.SearchRequestorMatchInformation.MatchInformation;
import dependencyStructures.Cycle;
import dependencyStructures.Dependency;
import dependencyStructures.StronglyConnectedComponent;


/*
 * Displays the data on dependencies, cycles and strongly connected components gained.
 * Only need the strongly connected components
 */
public class CycleDisplayer extends ViewPart {

	private TreeViewer treeViewer;
	private Tree tree;
	private HashMap<Dependency, LinkedList<MatchInformation>> matchInfo=new HashMap<Dependency, LinkedList<MatchInformation>>();
	
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
		
		//listens if double click, displays code that causes the match by double clicking
		//on the row with the MatchInformation
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				TreeItem[] selectedItems=tree.getSelection();
				if (selectedItems.length==1){
					TreeNode treeN=(TreeNode)selectedItems[0].getData();
					if(treeN.getValue() instanceof MatchInformation){
						MatchInformation matchInfo=(MatchInformation)treeN.getValue();
						ICompilationUnit compUnit=matchInfo.compilationUnit;
						try {
							IEditorPart editPart=JavaUI.openInEditor(compUnit);
							JavaUI.revealInEditor(editPart, (IJavaElement)matchInfo.compilationUnit);
							ITextEditor editor=(ITextEditor)editPart;
							editor.selectAndReveal(matchInfo.offset, matchInfo.length);
							//JavaUI.revealInEditor(editPart, matchInfo.compilationUnit.codeSelect(matchInfo.offset, matchInfo.length)[1]);
							
						} catch (PartInitException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (JavaModelException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		});
		
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
	
	/*
	 * Adds 3 collumns, each able to display the right information from a 
	 * StronglyConnectedComponent, Cycle, Dependency and MatchInformation 
	 */
	private void createColumns(){
		//column with description in it
		TreeViewerColumn descriptionVCol = new TreeViewerColumn(treeViewer, SWT.NONE);
		TreeColumn descriptionCol = descriptionVCol.getColumn();
	    descriptionCol.setResizable(true);
	    descriptionCol.setMoveable(true);

		descriptionVCol.getColumn().setWidth(200);
		descriptionVCol.getColumn().setText("Description");
		descriptionVCol.setLabelProvider(new ColumnLabelProvider() {
		  @Override
		  public String getText(Object element) {
			  TreeNode node=(TreeNode)element;
			  Object nodeElement=node.getValue();
			  
			  //how many children this node has (currently not used)
			  /*String childrenCount=new String();
			  if(node.getChildren()!=null){
				  childrenCount="("+node.getChildren().length+")";
			  }*/
			  
			  //display filename if node.getValue is an ICompilationUnit
			  if(nodeElement instanceof ICompilationUnit)
				  return ((ICompilationUnit)nodeElement).getElementName();
			  
			  return nodeElement.toString();
		  }
		});
		
		//column describing where the Dependency/MatchInformation is created 
		//(other ClassTypes dont display something here) 
		TreeViewerColumn fromVCol = new TreeViewerColumn(treeViewer, SWT.NONE);
		TreeColumn fromCol = fromVCol.getColumn();
		fromCol.setResizable(true);
		fromCol.setMoveable(true);
	    
	    fromVCol.getColumn().setWidth(200);
	    fromVCol.getColumn().setText("From");
	    fromVCol.setLabelProvider(new ColumnLabelProvider() {
		  @Override
		  public String getText(Object element) {
			  TreeNode node=(TreeNode)element;
			  Object nodeElement=node.getValue();
			 
			  if(node.getValue() instanceof Dependency){
				  Dependency dep=(Dependency)nodeElement;
				  return ""+dep.getStart().getElementName();
			  }
			  else if(node.getValue() instanceof MatchInformation){
				  MatchInformation matchInfo=(MatchInformation)nodeElement;
				  return ""+matchInfo.fromDescription;
			  }
			  return null;
		  }
		});

	    //column describing where the Dependency/MatchInformation leads
	    //(other ClassTypes dont display something here) 
	    TreeViewerColumn toVCol = new TreeViewerColumn(treeViewer, SWT.NONE);
		TreeColumn toCol = toVCol.getColumn();
		toCol.setResizable(true);
		toCol.setMoveable(true);

	    toVCol.getColumn().setWidth(200);
	    toVCol.getColumn().setText("To");
	    toVCol.setLabelProvider(new ColumnLabelProvider() {
		  @Override
		  public String getText(Object element) {
			  TreeNode node=(TreeNode)element;
			  Object nodeElement=node.getValue();
			  
			  if(node.getValue() instanceof Dependency){
				  Dependency dep=(Dependency)nodeElement;
				  return ""+dep.getEnd().getElementName();
			  }
			  else if(node.getValue() instanceof MatchInformation){
				  MatchInformation matchInfo=(MatchInformation)nodeElement;
				  return ""+matchInfo.toDescription;
			  }
			  return null;
		  }
		});
	}
	
	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}
	
	/*
	 * Displays StronglyConnectedComponents and the Cycles, Dependencies and MatchInformations contained
	 * in them
	 */
	public void display(LinkedList<StronglyConnectedComponent> comps){
		assert comps!=null;
		
		//build tree
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
					
					for(TreeNode depNode : cycleNode.getChildren()){
						
						////perform detailed search on all package dependencies
						Dependency dep=(Dependency)depNode.getValue();
						LinkedList<MatchInformation> matchInfos;
						//avoid doing the same searches multiple times
						if(matchInfo.containsKey(dep)){
							matchInfos= new LinkedList<MatchInformation>(matchInfo.get(dep));
						}
						else{
							Searcher searcher=new Searcher(dep);
							matchInfos=searcher.searchAllDetailedDependencies();
							matchInfo.put(dep, new LinkedList<MatchInformation>(matchInfos));
						}
						
						
						////group the matches by their classes
						//collect all classes
						LinkedList<ICompilationUnit> classes=new LinkedList<ICompilationUnit>();
						for(MatchInformation matchInfo : matchInfos){
							if(!classes.contains(matchInfo.compilationUnit)){
								classes.add(matchInfo.compilationUnit);
							}
						}
						
						TreeNode[] classNodes=new TreeNode[classes.size()];
						for(int i=0; i<classes.size();i++){
							classNodes[i]=new TreeNode(classes.get(i));
						}
						depNode.setChildren(classNodes);
						
						//determine which matches were found in which class
						for(TreeNode classNode:classNodes){
							LinkedList<MatchInformation> matchesInClass=new LinkedList<MatchInformation>();
							
							for(int i=0;i<matchInfos.size();i++){
								MatchInformation matchInfo=matchInfos.get(i);
								if(matchInfo.compilationUnit.equals((classNode.getValue()))){
									matchesInClass.add(matchInfo);
									matchInfos.remove(matchInfo);
									i--;
								}	
							}
							
							TreeNode[] infoNodes=new TreeNode[matchesInClass.size()];
							for(int i=0; i<matchesInClass.size();i++){
								infoNodes[i]=new TreeNode(matchesInClass.get(i));
							}
							classNode.setChildren(infoNodes);
						}
					}
				}
			}
		}
		
		TreeNode[] compNodeArray=new TreeNode[compNodes.size()];
		for(int i=0;i<compNodes.size();i++){
			compNodeArray[i]=compNodes.get(i);
		}
		
		//display tree
		sortTree(compNodeArray);
		treeViewer.setInput(compNodeArray);
		treeViewer.refresh();
	}
	
	/*
	 * recursively sorts this Array and all children of the TreeNodes
	 */
	private void sortTree(TreeNode[] nodes){
		
		Arrays.sort(nodes, new TreeNodeComparator());
		
		for (TreeNode node:nodes){
			if(node.getChildren()!=null){
				sortTree(node.getChildren());
			}
		}
		
	}
	
	/*
	 * Sorts TreeNodes according to how many children they have
	 * Does explicitly not sort TreeNodes containing dependencies
	 */
	private class TreeNodeComparator implements Comparator<TreeNode>{
		@Override
		public int compare(TreeNode o1, TreeNode o2) {
			
			//dont sort the dependencies
			if(o1.getValue() instanceof Dependency ||o2.getValue() instanceof Dependency){
				return 0;
			}
			
			TreeNode[] children1=o1.getChildren();
			TreeNode[] children2=o2.getChildren();
			
			int length1=0;
			int length2=0;
			
			if(children1!=null){
				length1=children1.length;
			}			
			if(children2!=null){
				length2=children2.length;
			}

			return length2-length1;
		}
	}

}
