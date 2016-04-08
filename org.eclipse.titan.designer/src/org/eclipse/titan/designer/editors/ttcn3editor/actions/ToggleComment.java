package org.eclipse.titan.designer.editors.ttcn3editor.actions;

import java.util.ResourceBundle;

import org.eclipse.titan.designer.editors.ttcn3editor.Reconciler;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.ui.texteditor.ITextEditor;

public class ToggleComment extends org.eclipse.titan.designer.editors.actions.ToggleComment{

	/**
	 * @see org.eclipse.ui.texteditor.TextEditorAction#TextEditorAction(ResourceBundle,
	 *      String, ITextEditor)
	 */
	public ToggleComment(final ResourceBundle bundle, final String prefix, final ITextEditor editor) {
		super(bundle, prefix, editor);
	}

	@Override
	protected void doOperation(final int operationCode) {
		final ITextEditor editor = getTextEditor();

		final Reconciler reconciler = ((TTCN3Editor)editor).getReconciler();
		reconciler.allowIncrementalReconciler(false);

		operationTarget.doOperation(operationCode);

		reconciler.allowIncrementalReconciler(true);
	}

}
