package cycleplugin;

import java.math.MathContext;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.ui.editors.text.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.IBuffer.ITextEditCapability;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import cycleplugin.SearchRequestorMatchInformation.MatchInformation;

public class CycleDisplayer extends ViewPart {

	
	private Text text;
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
		
		tree.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
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
			  TreeNode node=(TreeNode)element;
			  Object nodeElement=node.getValue();
			  String components=new String();
			  if(nodeElement instanceof StronglyConnectedComponent){
				  components=((StronglyConnectedComponent)nodeElement).getPackageNames();
			  }
			  else if(nodeElement instanceof Cycle){
				  components=((Cycle)nodeElement).getPackageNames();
			  }
			  
			  String childrenCount=new String();
			  if(node.getChildren()!=null){
				  childrenCount="("+node.getChildren().length+")";
			  }
			  
			  if(nodeElement instanceof ICompilationUnit)
				  return ((ICompilationUnit)nodeElement).getElementName();
			  
			  return nodeElement.toString()+components;
		  }
		});
		
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
			  
			  /*if(nodeElement instanceof Cycle){
				  return ((Cycle)nodeElement).getPackageNames();
			  }
			  else if(node.getValue() instanceof StronglyConnectedComponent){
				  return ((StronglyConnectedComponent)nodeElement).getPackageNames();
			  }*/
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
	    /*
	    TreeViewerColumn lineVCol = new TreeViewerColumn(treeViewer, SWT.NONE);
		TreeColumn lineCol = descriptionVCol.getColumn();
		lineCol.setResizable(true);
		lineCol.setMoveable(true);

		lineVCol.getColumn().setWidth(200);
		lineVCol.getColumn().setText("Line");
		lineVCol.setLabelProvider(new ColumnLabelProvider() {
		  @Override
		  public String getText(Object element) {
			  TreeNode node=(TreeNode)element;
			  if(node.getValue() instanceof MatchInformation){
				  MatchInformation info=(MatchInformation)node.getValue();
				  return ""+info.lineNumber;
			  }
			  return null;
		  }
		});*/
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
		
		treeViewer.setInput(compNodeArray);
		treeViewer.refresh();
	}
	
	

}
