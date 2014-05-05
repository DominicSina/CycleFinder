package cycleplugin;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

public class SearchRequestorMethodDeclarations extends SearchRequestor{
	private LinkedList<MatchInformation> result=new LinkedList<MatchInformation>();
	 
	
	@Override
	public void acceptSearchMatch(SearchMatch match){
		
		MatchInformation matchinfo=new MatchInformation(match);
		result.add(matchinfo);
		/*
		IMember member = (IMember) match.getElement();
		CompilationUnit cuNode = retrieveCo+++mpilationUnit(member.getCompilationUnit());
	
		ASTNode node = ASTNodeSearchUtil.getAstNode(match, cuNode);*/

	}
	
	public LinkedList<MatchInformation> getResult() {
		return result;
	}
	
	private IJavaElement getIJavaElement(IPath path){
		IResource resource=ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		if (resource == null) {
			return null;
		}
		IJavaElement javaElement=JavaCore.create(resource);
		if (javaElement == null) {
			return null;
		}
		return javaElement;
	}
	 
	public class MatchInformation{
		String matchType;
		IResource resource;
		int lineNumber;
		ICompilationUnit compilationUnit;
		int offset;
		int length;
		
		public MatchInformation(SearchMatch match){
			assert match!=null;
			
			ICompilationUnit compUnit =(ICompilationUnit)getIJavaElement(match.getResource().getFullPath()); //creates now Element, problems with equals() perhaps

			IBuffer buf=null;
			try {
				buf = compUnit.getBuffer();
			} catch (JavaModelException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			LineNumberReader lineReader=new LineNumberReader(new StringReader(buf.getContents()));
			try {
				lineReader.skip(match.getOffset());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			this.matchType=match.toString();
			this.resource=match.getResource();
			this.lineNumber=lineReader.getLineNumber()+1;
			this.compilationUnit=compUnit;
			this.offset=match.getOffset();
			this.length=match.getLength();
		}
		
		public MatchInformation(String matchType, IResource resource, int lineNumber){
			assert matchType!=null;
			assert resource!=null;
			assert lineNumber>=0;
			
			this.matchType=matchType;
			this.resource=resource;
			this.lineNumber=lineNumber;
		}
		
		@Override
		public String toString(){
			return matchType; 
		}
	}
}


