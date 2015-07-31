package org.eclipse.titan.designer.editors.configeditor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.titan.common.parsers.cfg.CfgAnalyzer;

/**
 * ANTLR 4 version
 * @author Arpad Lovassy
 */
public class ConfigEditor_V4 extends ConfigEditor {

	@Override
	public void refresh(final CfgAnalyzer cfgAnalyzer) {
		//TODO: add extra pages if needed
	}

	@Override
	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
		//TODO: add extra pages if needed
	}
	
	@Override
	protected void addPages() {
		createTextEditorPage();
		//TODO: add extra pages if needed
		setPartName(getEditorInput().getName());
	}
	
	@Override
	public void doSave(final IProgressMonitor monitor) {
		editor.doSave(monitor);
		isDirty = false;
		firePropertyChange(PROP_DIRTY);
	}

	@Override
	public void doSaveAs() {
		editor.doSaveAs();
		setPageText(editorPageIndex, editor.getTitle());
		setInput(editor.getEditorInput());

		isDirty = false;
		firePropertyChange(PROP_DIRTY);
	}
}
