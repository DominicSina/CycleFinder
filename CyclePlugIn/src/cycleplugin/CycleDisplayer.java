package cycleplugin;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.internal.console.ConsoleView;
import org.eclipse.ui.part.ViewPart;

public class CycleDisplayer extends ViewPart {

	//private Label label;
	
	private Text text;
	
	
	//private MessageBox messageBox;
	
	public CycleDisplayer() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		//label=new Label(parent, SWT.CENTER);
		//label.setText("Hi");
		
		//IPageLayout layout=(IPageLayout) parent.getLayout();
		//layout.addView(IConsoleConstants.ID_CONSOLE_VIEW, IPageLayout.BOTTOM, .7f, layout.getEditorArea()); 
	
		text=new Text(parent, SWT.MULTI|SWT.V_SCROLL|SWT.H_SCROLL);
		//messageBox=new MessageBox(parent.getShell(),0);
	}

	@Override
	public void setFocus() {
		//label.setFocus();
		text.setFocus();
		//messageBox.getParent().setFocus();
	}
	
	public void display(String toWrite){
		assert toWrite!=null;
		
		text.setText(toWrite);
		//messageBox.setMessage(toWrite);
	}
	
	

}
