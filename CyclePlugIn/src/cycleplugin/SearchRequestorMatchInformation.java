package cycleplugin;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;

import org.eclipse.core.commands.ITypedParameter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.LocalVariableDeclarationMatch;
import org.eclipse.jdt.core.search.LocalVariableReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeReferenceMatch;
import org.eclipse.jdt.internal.core.search.matching.ConstructorPattern;
import org.eclipse.jdt.internal.core.search.matching.MethodPattern;
import org.eclipse.jdt.internal.core.search.matching.TypeDeclarationPattern;
import org.eclipse.jdt.internal.core.search.matching.TypeReferencePattern;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeUniverseSet;
import org.eclipse.jdt.internal.ui.search.JavaSearchContentProvider;
import org.eclipse.*;

public class SearchRequestorMatchInformation extends SearchRequestor{
	private LinkedList<MatchInformation> result=new LinkedList<MatchInformation>();
	private int fineGrain;
	private IJavaElement toSearch;
	
	public SearchRequestorMatchInformation(SearchPattern searchPattern){
		assert searchPattern!=null;
		
		toSearch=searchPattern.focus;
		
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
			int a=0;
			a=a+0;
		}
		
	}
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
		String matchType=new String();
		IResource resource;
		ICompilationUnit compilationUnit;
		IJavaElement from;
		IJavaElement to;
		String fromDescription=new String();
		String toDescription=new String();
		int lineNumber;
		int offset;
		int length;
		
		public MatchInformation(SearchMatch match){
			assert match!=null;
			
			this.from=(IJavaElement)match.getElement();
			this.to=SearchRequestorMatchInformation.this.toSearch;
			
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
			
			//((TypeReferenceMatch)match).
			
			//this.matchType=match.toString();
			/*
			if(fineGrain==IJavaSearchConstants.LOCAL_VARIABLE_DECLARATION_TYPE_REFERENCE){//Local_Var... only return TypeReferneceMAtches
				this.matchType="Local variable declaration type reference";
			}
			else if(fineGrain==IJavaSearchConstants.FIELD_DECLARATION_TYPE_REFERENCE){
				this.matchType="Field declaration type reference";
			}
			else if(fineGrain==IJavaSearchConstants.FIELD_DECLARATION_TYPE_REFERENCE){
				this.matchType="Field declaration type reference";
			}
			else if(fineGrain==IJavaSearchConstants.SUPERTYPE_TYPE_REFERENCE){
				this.matchType="Super Type Reference";
			}
			else if(fineGrain==IJavaSearchConstants.PARAMETER_DECLARATION_TYPE_REFERENCE){
				this.matchType="Parameter declaration type reference";
			}
			else if(fineGrain==IJavaSearchConstants.RETURN_TYPE_REFERENCE){
				this.matchType="Return type reference";
			}
			else if(fineGrain==IJavaSearchConstants.IMPORT_DECLARATION_TYPE_REFERENCE){
				this.matchType="Import declaration type reference";
			}
			else if(from.getElementType()==IJavaElement.METHOD){
					this.matchType="Method reference";
			}
			else{
				this.matchType="Undefined";
			}*/
			/*if(compUnit.getElementName().equals("LocalVariableCycle1.java")){
				int a=0;
				a=a+0;
			}*/
			if(from.getElementType()==IJavaElement.METHOD&&this.matchType.equals("")){
				IMethod method=(IMethod)from;
				
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
					paramSignature+=",";
					if(paramTypes[i].length()>=2){
						paramSignature+=paramTypes[i].substring(0,paramTypes[i].length()-1);
					}
					else{
						paramSignature+=paramTypes[i];
					}
					
				}
				paramSignature+=")";
				
				if(to.getElementType()==IJavaElement.TYPE){
					try {
						IType type=(IType)to;
						
						String[] methodParams=method.getParameterTypes();
						
						for(String methParamType:methodParams){
							String typeName=Signature.createTypeSignature(type.getElementName(), false);
							if(methParamType.equals(typeName)){
							
								this.matchType="Method Signature reference";
								this.fromDescription=compUnit.getParent().getElementName()+"."+compUnit.getElementName().substring(0, compUnit.getElementName().length()-5)+"."+method.getElementName()+paramSignature;
								this.toDescription=type.getFullyQualifiedName('.');
							}
						}

						if(method.getReturnType().equals(Signature.createTypeSignature(type.getElementName(), false))){
							this.matchType="Method Signature reference";
							this.fromDescription=compUnit.getParent().getElementName()+"."+compUnit.getElementName().substring(0, compUnit.getElementName().length()-5)+"."+method.getElementName()+paramSignature;
							this.toDescription=type.getFullyQualifiedName('.');
						}
						
						if(this.matchType.equals("")){
							this.matchType="Local Type reference";
							this.fromDescription=compUnit.getParent().getElementName()+"."+compUnit.getElementName().substring(0, compUnit.getElementName().length()-5)+"."+method.getElementName()+paramSignature;
							this.toDescription=type.getFullyQualifiedName('.');
						}
					} catch (JavaModelException e) {
						e.printStackTrace();
					}
				}
				else if(to.getElementType()==IJavaElement.METHOD){
					IMethod methodTo=(IMethod)to;
					
					String paramSignatureTo=new String();
					paramSignatureTo+="(";
					String[] paramTypesTo=method.getParameterTypes();
					if(paramTypesTo.length>=1){
						if(paramTypesTo[0].length()>=2){
							paramSignatureTo+=paramTypesTo[0].substring(0,paramTypesTo[0].length()-1);
						}
						else{
							paramSignatureTo+=paramTypesTo[0];
						}
					}
					for(int i=1;i<paramTypesTo.length;i++){
						if(paramTypesTo[i].length()>=2){
							paramSignature+=",";
							paramSignatureTo+=paramTypesTo[i].substring(0,paramTypesTo[i].length()-1);
						}
						else{
							paramSignatureTo+=paramTypesTo[i];
						}
						
					}
					paramSignatureTo+=")";
					
					
					this.matchType="Method reference";
					this.fromDescription=compUnit.getParent().getElementName()+"."+compUnit.getElementName().substring(0, compUnit.getElementName().length()-5)+"."+method.getElementName()+paramSignature;
					this.toDescription=methodTo.getCompilationUnit().getParent().getElementName()+"."+methodTo.getCompilationUnit().getElementName().substring(0, methodTo.getCompilationUnit().getElementName().length()-5)+"."+methodTo.getElementName()+paramSignatureTo;
				}
			}
			if(from.getElementType()==IJavaElement.TYPE&&to.getElementType()==IJavaElement.TYPE&&this.matchType.equals("")){
				try {
					IType typeFrom=(IType)from;
					IType typeTo=(IType)to;
					
					String[] superNames=new String[0];
					superNames = typeFrom.getSuperInterfaceNames();
						
					for(String name:superNames){
						if(name.equals(typeTo.getElementName())){
							this.matchType="Inheritance reference";
							this.fromDescription=typeFrom.getFullyQualifiedName('.');
							this.toDescription=typeTo.getFullyQualifiedName('.');
						}
					}
					if(typeFrom.getSuperclassName().equals(typeTo.getElementName())){
						this.matchType="Inheritance reference";
						this.fromDescription=typeFrom.getFullyQualifiedName('.');
						this.toDescription=typeTo.getFullyQualifiedName('.');
					}
					
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
			}
			if(from.getElementType()==IJavaElement.FIELD&&this.matchType.equals("")){
				IField field=(IField)from;
				IType type=(IType)to;
				
				this.matchType="Field reference";
				this.fromDescription=compUnit.getParent().getElementName()+"."+compUnit.getElementName().substring(0, compUnit.getElementName().length()-5)+"."+field.getElementName();
				this.toDescription=type.getFullyQualifiedName('.');
			}
			if(from.getElementType()==IJavaElement.IMPORT_DECLARATION&&this.matchType.equals("")){
				IImportDeclaration importDec=(IImportDeclaration)from;
				IType type=(IType)to;
				
				this.matchType="Import declaration reference";
				this.fromDescription=compUnit.getParent().getElementName()+"."+compUnit.getElementName().substring(0, compUnit.getElementName().length()-5)+"."+"imports";
				this.toDescription=type.getFullyQualifiedName('.');	
			}
			/*if(from.getElementType()==IJavaElement.TYPE&&this.matchType.equals("")){
				this.matchType="Type reference";
			}*/
			
			
			/*
			else if(from.getElementType()==IJavaElement.TYPE){
				this.matchType="Type reference";
			}
			
			else{
				this.matchType="Undefined";
			}*/
			
			/*if(match instanceof LocalVariableDeclarationMatch) {
				this.matchType="Local Variable Type dependency";
			}*/
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


