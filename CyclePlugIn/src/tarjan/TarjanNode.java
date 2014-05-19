package tarjan;

/*
 * A node in a TarjanGraph. Has some special instance variables
 * needed by Tarjan's Algorithm.
 * isIndexSet: determines if this index has been traversed in Tarjan's Algorithm  
 * index: unique id of a node, based on in which order they were traversed in Tarjan's Algorithm
 * lowerConnectedIndex: the index of node that is connected to this node and has a lower index
 * 
*/
public class TarjanNode<T> {

	private T node;
	private boolean isIndexSet=false;
	private int index;
	private int lowerConnectedIndex;
	
	public TarjanNode(T node){
		assert node!=null;
		
		this.node=node;
	}
	
	public int getIndex(){
		return index;
	}
	
	public int getlowestConnecedIndex(){
		return lowerConnectedIndex;
	}
	
	public void setlowestConnecedIndex(int i){
		assert i>=0;
		
		lowerConnectedIndex=i;
	}
	
	public void setIndex(int i){
		assert i>=0;
		
		isIndexSet=true;
		index=i;
	}
	
	public boolean isIndexSet(){
		return isIndexSet;
	}
	
	public T getNode(){
		return node;
	}
	
	@Override
	public String toString(){
		return "TNode "+node.toString();
	}
	
	@Override
	public boolean equals(Object a){
		if ( this == a ) return true;
		if ( !(a instanceof TarjanNode<?>) ) return false;
		TarjanNode<?> sameType = (TarjanNode<?>)a;
		return
			this.node.equals(sameType.node);
		}
	
	@Override
	public int hashCode(){
		return this.node.hashCode();
	}
}
