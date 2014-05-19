package tarjan;

/*
 * Represents an edge in a directed graph
 */
public class TarjanEdge<T> {

	private TarjanNode<T> start;
	private TarjanNode<T> end;
	
	/*
	 * Needs a starting TarjanNode and an ending TarjanNode
	 */
	public TarjanEdge(TarjanNode<T> start, TarjanNode<T> end){
		assert start!=null;
		assert end!=null;
		
		this.start=start;
		this.end=end;
	}
	
	@Override
	public String toString(){
		return "TEdge "+start.toString()+"-->"+end.toString();
	}
	
	public TarjanNode<T> getStart(){
		return start;
	}
	
	public TarjanNode<T> getEnd(){
		return end;
	}
}
