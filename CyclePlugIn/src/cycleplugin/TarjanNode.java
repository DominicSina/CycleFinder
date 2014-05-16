package cycleplugin;

public class TarjanNode<T> {

	private T node;
	private boolean isIndexSet=false;
	private int index;
	private int lowestConnectedIndex;
	
	public TarjanNode(T node){
		assert node!=null;
		
		this.node=node;
	}
	
	public int getIndex(){
		return index;
	}
	
	public int getlowestConnecedIndex(){
		return lowestConnectedIndex;
	}
	
	public void setlowestConnecedIndex(int i){
		assert i>=0;
		
		lowestConnectedIndex=i;
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
