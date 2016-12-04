package org.eclipse.titanium.refactoring.expandvaluelistnotation;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * This class represents the 'Minimize visibility modifiers' refactoring operation.
 * <p>
 * This refactoring operation minimizes all visibility modifiers in the given
 *   files/folders/projects, which are contained in a {@link IStructuredSelection} object.
 * The operation can be executed using the mechanisms in the superclass, through a wizard for example
 *
 * @author Zsolt Tabi
 */
public class ExpandFieldNamesRefactoring extends Refactoring {
	public static final String PROJECTCONTAINSERRORS = "The project `{0}'' contains errors, which might corrupt the result of the refactoring";
	public static final String PROJECTCONTAINSTTCNPPFILES = "The project `{0}'' contains .ttcnpp files, which might corrupt the result of the refactoring";
	private static final String ONTHEFLYANALAYSISDISABLED = "The on-the-fly analysis is disabled, there is semantic information present to work on";
	private static final String MINIMISEWARNING = "Minimise memory usage is enabled, which can cause unexpected behaviour in the refactoring process!\n"
			+ "Refactoring is not supported with the memory minimise option turned on, "
			+ "we do not take any responsibility for it.";

	private final IStructuredSelection selection;
	private final Set<IProject> projects = new HashSet<IProject>();

	private Object[] affectedObjects;		//the list of objects affected by the change

	/*
	 * TODO dev:
	 *
	 *
	 * TODO fix:
	 *
	 * */

	public ExpandFieldNamesRefactoring(final IStructuredSelection selection) {
		this.selection = selection;

		final Iterator<?> it = selection.iterator();
		while (it.hasNext()) {
			final Object o = it.next();
			if (o instanceof IResource) {
				final IProject temp = ((IResource) o).getProject();
				projects.add(temp);
			}
		}
	}

	public Object[] getAffectedObjects() {
		return affectedObjects;
	}

	//METHODS FROM REFACTORING

	@Override
	public String getName() {
		return "Expand record field names";
	}

	@Override
	public RefactoringStatus checkInitialConditions(final IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		final RefactoringStatus result = new RefactoringStatus();
		try {
			pm.beginTask("Checking preconditions...", 3);

			final IPreferencesService prefs = Platform.getPreferencesService();//PreferenceConstants.USEONTHEFLYPARSING
			if (! prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.USEONTHEFLYPARSING, false, null)) {
				result.addError(ONTHEFLYANALAYSISDISABLED);
			}

			// check that there are no ttcnpp files in the
			// project
			for (IProject project : projects) {
				if (hasTtcnppFiles(project)) {//FIXME actually all referencing and referenced projects need to be checked too !
					result.addError(MessageFormat.format(PROJECTCONTAINSTTCNPPFILES, project));
				}
			}

			pm.worked(1);
			// check that there are no error markers in the
			// project
			for (IProject project : projects) {
				final IMarker[] markers = project.findMarkers(null, true, IResource.DEPTH_INFINITE);
				for (IMarker marker : markers) {
					if (IMarker.SEVERITY_ERROR == marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR)) {
						result.addError(MessageFormat.format(PROJECTCONTAINSERRORS, project));
						break;
					}
				}
			}
			pm.worked(1);


			if (prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.MINIMISEMEMORYUSAGE, false, null)) {
				result.addError(MINIMISEWARNING);
			}
			pm.worked(1);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			result.addFatalError(e.getMessage());
		} finally {
			pm.done();
		}
		return result;
	}

	@Override
	public RefactoringStatus checkFinalConditions(final IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (selection == null) {
			return null;
		}

		final CompositeChange cchange = new CompositeChange("ExpandFieldNamesRefactoring");
		final Iterator<?> it = selection.iterator();
		while (it.hasNext()) {
			final Object o = it.next();
			if (!(o instanceof IResource)) {
				continue;
			}

			final IResource res = (IResource)o;
			final ResourceVisitor vis = new ResourceVisitor();
			res.accept(vis);
			cchange.add(vis.getChange());
		}
		affectedObjects = cchange.getAffectedObjects();
		return cchange;
	}

	public static boolean hasTtcnppFiles(final IResource resource) throws CoreException {
		if (resource instanceof IProject || resource instanceof IFolder) {
			final IResource[] children = resource instanceof IFolder ? ((IFolder) resource).members() : ((IProject) resource).members();
			for (IResource res : children) {
				if (hasTtcnppFiles(res)) {
					return true;
				}
			}
		} else if (resource instanceof IFile) {
			final IFile file = (IFile) resource;
			return "ttcnpp".equals(file.getFileExtension());
		}
		return false;
	}
	//METHODS FROM REFACTORING END



	/**
	 * Visits all the files of a folder or project (any {@link IResource}).
	 * Creates the {@link Change} for all files and then merges them into a single
	 * {@link CompositeChange}.
	 * <p>
	 * Call on any {@link IResource} object.
	 *  */
	private class ResourceVisitor implements IResourceVisitor {

		private final CompositeChange change;

		public ResourceVisitor() {
			this.change = new CompositeChange("ExpandFieldNamesRefactoring");
		}

		private CompositeChange getChange() {
			return change;
		}

		@Override
		public boolean visit(final IResource resource) throws CoreException {
			if (resource instanceof IFile) {
				final ChangeCreator chCreator = new ChangeCreator((IFile)resource);
				chCreator.perform();
				final Change ch = chCreator.getChange();
				if (ch != null) {
					change.add(ch);
				}
				//SKIP
				return false;
			}
			//CONTINUE
			return true;
		}

	}

}
