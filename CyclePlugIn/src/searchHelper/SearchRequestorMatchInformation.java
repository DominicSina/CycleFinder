package searchHelper;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.LinkedList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.search.matching.ConstructorPattern;
import org.eclipse.jdt.internal.core.search.matching.MethodPattern;
import org.eclipse.jdt.internal.core.search.matching.TypeReferencePattern;

/*
 * Used to process each found match and store the gained information
 */
public class SearchRequestorMatchInformation extends SearchRequestor{
	private LinkedList<MatchInformation> result=new LinkedList<MatchInformation>();
	private int fineGrain;
	private IJavaElement toSearch;
	
	public SearchRequestorMatchInformation(SearchPattern searchPattern){
		assert searchPattern!=null;
		
		toSearch=searchPattern.focus;
		
		//TODO very likely to not catch all cases
		if(searchPattern instanceof TypeReferencePattern){
			fineGrain=((TypeReferencePattern)searchPattern).fineGrain;
		}
		else if(searchPattern instanceof MethodPattern){
			fineGrain=((MethodPattern)searchPattern).fineGrain;
		}
		else if(searchPattern instanceof ConstructorPattern){
			fineGrain=((ConstructorPattern)searchPattern).fineGrain;
		}
		else{
			new Exception("Match type not supported, finegrain not assigned").printStackTrace();
		}
	}
	
	@Override
	public void acceptSearchMatch(SearchMatch match){
		
		MatchInformation matchinfo=new MatchInformation(match);
		result.add(matchinfo);
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
	 
	/*
	 * Stores different information on a match
	 */
	public class MatchInformation{
		public String matchType=new String(); //a description what kind of dependency was found
		public IResource resource; //the source file where the match was found
		public ICompilationUnit compilationUnit; //the compilation unit to the source file
		public IJavaElement from; //IJavaElement that was found
		public IJavaElement to; //IJavaElement that was searched
		public String fromDescription=new String(); //description IJavaElement that was found 
		public String toDescription=new String(); //description IJavaElement that was searched
		public int lineNumber; //in which line the match is in the source file
		public int offset; //char offset in the source file where the match starts
		public int length; //how long the match is measured in chars 
		public int fineGrain;//useful to determine which kind of IJavaSearchConstant was used
		
		/*
		 * Constructor extract the needed information from a match
		 */
		public MatchInformation(SearchMatch match){
			assert match!=null;
			
			//set involved IJavaElements
			this.from=(IJavaElement)match.getElement();
			this.to=SearchRequestorMatchInformation.this.toSearch;
			
			this.fineGrain=SearchRequestorMatchInformation.this.fineGrain;
			
			//store resource where match was created
			this.resource=match.getResource();
			
			//store compilationUnit where the match was created
			ICompilationUnit compUnit =(ICompilationUnit)getIJavaElement(match.getResource().getFullPath()); //creates now Element, problems with equals() perhaps
			this.compilationUnit=compUnit;
			
			//find and store line number where the match was created, also store offset&length
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

			this.lineNumber=lineReader.getLineNumber()+1;
			this.offset=match.getOffset();
			this.length=match.getLength();
			
			////create match type based on the IJavaElements in to and from
			//case 1: from is a method
			if(from.getElementType()==IJavaElement.METHOD&&this.matchType.equals("")){
				IMethod method=(IMethod)from;
				
				//create String to describe the parameters of from
				String paramSignature=getMethodParameterDescription(method);
				
				//case 1.1: from is a method && to is a type
				if(to.getElementType()==IJavaElement.TYPE){
					try {
						IType type=(IType)to;
						
						String[] methodParams=method.getParameterTypes();
						
						//check if type of "to" was found in a parameter type of from
						for(String methParamType:methodParams){
							String typeName=Signature.createTypeSignature(type.getElementName(), false);
							if(methParamType.equals(typeName)){
							
								this.matchType="Method Signature reference";
								this.fromDescription=compUnit.getParent().getElementName()+"."+compUnit.getElementName().substring(0, compUnit.getElementName().length()-5)+"."+method.getElementName()+paramSignature;
								this.toDescription=type.getFullyQualifiedName('.');
							}
						}

						//check if type of "to" was found in the return type of from
						if(method.getReturnType().equals(Signature.createTypeSignature(type.getElementName(), false))){
							this.matchType="Method Signature reference";
							this.fromDescription=compUnit.getParent().getElementName()+"."+compUnit.getElementName().substring(0, compUnit.getElementName().length()-5)+"."+method.getElementName()+paramSignature;
							this.toDescription=type.getFullyQualifiedName('.');
						}
						
						/*
						//check if it was a constructor reference
						String shouldbeNew=compUnit.getSource().substring(offset-4, offset-1);
						char char1=compUnit.getSource().charAt(offset+length+1);
						if(shouldbeNew.equals("new")&&char1=='('){
							this.matchType="toRemove";
							this.fromDescription=compUnit.getParent().getElementName()+"."+compUnit.getElementName().substring(0, compUnit.getElementName().length()-5)+"."+method.getElementName()+paramSignature;
							this.toDescription=type.getFullyQualifiedName('.');
						}*/
						
						//if none of the above, it has to be a local type reference inside the method
						if(this.matchType.equals("")){
							this.matchType="Local Type reference";
							this.fromDescription=compUnit.getParent().getElementName()+"."+compUnit.getElementName().substring(0, compUnit.getElementName().length()-5)+"."+method.getElementName()+paramSignature;
							this.toDescription=type.getFullyQualifiedName('.');
						}
					} catch (JavaModelException e) {
						e.printStackTrace();
					}
				}
				
				//case 1.2: from is a method && to is a method
				else if(to.getElementType()==IJavaElement.METHOD){
					IMethod methodTo=(IMethod)to;
					
					//create String to describe the parameters of to
					String paramSignatureTo=getMethodParameterDescription(methodTo);
						
					this.matchType="Method reference";
					this.fromDescription=compUnit.getParent().getElementName()+"."+compUnit.getElementName().substring(0, compUnit.getElementName().length()-5)+"."+method.getElementName()+paramSignature;
					this.toDescription=methodTo.getCompilationUnit().getParent().getElementName()+"."+methodTo.getCompilationUnit().getElementName().substring(0, methodTo.getCompilationUnit().getElementName().length()-5)+"."+methodTo.getElementName()+paramSignatureTo;
				}
			}
			
			//case 2: from is a type && to is a type
			if(from.getElementType()==IJavaElement.TYPE&&to.getElementType()==IJavaElement.TYPE&&this.matchType.equals("")){
				try {
					IType typeFrom=(IType)from;
					IType typeTo=(IType)to;
					
					String[] superNames=new String[0];
					superNames = typeFrom.getSuperInterfaceNames();
					
					//iterate trough all implemented interfaces
					for(String name:superNames){
						if(name.equals(typeTo.getElementName())){
							this.matchType="Inheritance reference";
							this.fromDescription=typeFrom.getFullyQualifiedName('.');
							this.toDescription=typeTo.getFullyQualifiedName('.');
						}
					}
					//check if superclass caused the match
					if(typeFrom.getSuperclassName()!=null&&typeFrom.getSuperclassName().equals(typeTo.getElementName())){
						this.matchType="Inheritance reference";
						this.fromDescription=typeFrom.getFullyQualifiedName('.');
						this.toDescription=typeTo.getFullyQualifiedName('.');
					}
					
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
			}
			
			//case 3: from is a field
			if(from.getElementType()==IJavaElement.FIELD&&this.matchType.equals("")){
				IField field=(IField)from;
				if (to.getElementType()==IJavaElement.TYPE){
					IType type=(IType)to;//can only be a type most likely if the match was created in a field
					this.matchType="Field reference";
					this.fromDescription=compUnit.getParent().getElementName()+"."+compUnit.getElementName().substring(0, compUnit.getElementName().length()-5)+"."+field.getElementName();
					this.toDescription=type.getFullyQualifiedName('.');
				
				}
				else{
					this.matchType="Unknown Field reference";
					this.fromDescription=compUnit.getParent().getElementName()+"."+compUnit.getElementName().substring(0, compUnit.getElementName().length()-5)+"."+field.getElementName();
					this.toDescription=to.getElementName();
				}
			}
			
			//case 4: from is an import declaration
			if(from.getElementType()==IJavaElement.IMPORT_DECLARATION&&this.matchType.equals("")){
				IImportDeclaration importDec=(IImportDeclaration)from;
				IType type=(IType)to;
				
				this.matchType="Import declaration reference";
				this.fromDescription=compUnit.getParent().getElementName()+"."+compUnit.getElementName().substring(0, compUnit.getElementName().length()-5)+"."+"imports";
				this.toDescription=type.getFullyQualifiedName('.');	
			}
			if(this.matchType.equals("")){
				//is executed when match could not be classified
				//sets from/toDescription to meaningful but unformatted string
				this.matchType="Undefined reference";
				this.fromDescription=from.getElementName();
				this.toDescription=to.getElementName();	
			}
		}
		
		public MatchInformation(String matchType, IResource resource, int lineNumber){
			assert matchType!=null;
			assert resource!=null;
			assert lineNumber>=0;
			
			this.matchType=matchType;
			this.resource=resource;
			this.lineNumber=lineNumber;
		}
		
		private String getMethodParameterDescription(IMethod method){
			//create String to describe the parameters of to
			String paramSignature=new String();
			paramSignature+="(";
			String[] paramTypes=method.getParameterTypes();
			if(paramTypes.length>=1){
				if(paramTypes[0].length()>=2){
					paramSignature+=paramTypes[0].substring(0,paramTypes[0].length()-1);
				}
				else{
					paramSignature+=paramTypes[0];
				}
			}
			for(int i=1;i<paramTypes.length;i++){
				//remove 'Q' at the beginning and ';' at the end of the paramTypes
				if(paramTypes[i].length()>=3){
					paramSignature+=",";
					paramSignature+=paramTypes[i].substring(1,paramTypes[i].length()-1);
				}
				else{
					paramSignature+=paramTypes[i];
				}
				
			}
			paramSignature+=")";
			return paramSignature;
		}
		
		@Override
		public String toString(){
			return matchType; 
		}
	}
}


