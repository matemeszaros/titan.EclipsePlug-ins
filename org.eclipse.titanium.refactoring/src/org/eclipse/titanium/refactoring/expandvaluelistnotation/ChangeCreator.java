package org.eclipse.titanium.refactoring.expandvaluelistnotation;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValue;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Sequence_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;

/**
 * This class is only instantiated by the {@link ExpandFieldNamesRefactoring} once per each refactoring operation.
 * <p>
 * By passing the selection through the constructor and calling {@link ChangeCreator#perform()}, this class
 *  creates a {@link Change} object, which can be returned by the standard
 *  {@link Refactoring#createChange(IProgressMonitor)} method in the refactoring class.
 * 
 * @author Zsolt Tabi
 */
class ChangeCreator {

	//in
	private final IFile selectedFile;
	
	//out
	private Change change;
	
	ChangeCreator(final IFile selectedFile) {
		this.selectedFile = selectedFile;
	}
	
	Change getChange() {
		return change;
	}
	
	/** 
	 * Creates the {@link #change} object, which contains all the inserted and edited visibility modifiers
	 * in the selected resources.
	 * */
	void perform() {
		if (selectedFile == null) {
			return;
		}
		change = createFileChange(selectedFile);
	}
	
	private Change createFileChange(final IFile toVisit) {
		if (toVisit == null) {
			return null;
		}
		ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(toVisit.getProject());
		Module module = sourceParser.containedModule(toVisit);
		if(module == null) {
			return null;
		}
		//find all locations in the module that should be edited
		DefinitionVisitor vis = new DefinitionVisitor();
		module.accept(vis);
		NavigableSet<ILocateableNode> nodes = vis.getLocations();
		
		if (nodes.isEmpty()) {
			return null;
		}

		//create a change for each edit location
		TextFileChange tfc = new TextFileChange(toVisit.getName(), toVisit);
		MultiTextEdit rootEdit = new MultiTextEdit();
		tfc.setEdit(rootEdit);

		for (ILocateableNode node : nodes) {
			SequenceOf_Value seqOfValues = (SequenceOf_Value) node;
			if (seqOfValues.getMyGovernor() == null) {
				continue;
			}

			Sequence_Value converted = Sequence_Value.convert(CompilationTimeStamp.getBaseTimestamp(), seqOfValues);
			if (seqOfValues.getIsErroneous(CompilationTimeStamp.getBaseTimestamp())) {
				continue;
			}
			for (int i = 0; i < converted.getNofComponents(); ++i) {
				NamedValue namedValue = converted.getSeqValueByIndex(i);
				if (namedValue == null) { // record with unnamed fields
					break;
				}
				rootEdit.addChild(new InsertEdit(namedValue.getValue().getLocation().getOffset(), 
						namedValue.getName().getTtcnName() + " := "));
			}
		}

		if (!rootEdit.hasChildren()) {
			return null;
		}

		return tfc;
	}
	
	/**
	 * Collects the locations of all the definitions in a module where the visibility modifier
	 *  is not yet minimal.
	 * <p>
	 * Call on modules.
	 * */
	private static class DefinitionVisitor extends ASTVisitor {
		
		private final NavigableSet<ILocateableNode> locations;
		
		DefinitionVisitor() {
			locations = new TreeSet<ILocateableNode>(new LocationComparator());
		}
		
		NavigableSet<ILocateableNode> getLocations() {
			return locations;
		}
		
		@Override
		public int visit(final IVisitableNode node) {
			
			if (node instanceof SequenceOf_Value) { 
				locations.add((SequenceOf_Value) node);
			}

			return V_CONTINUE;
		}
	}

	/**
	 * Compares {@link ILocateableNode}s by comparing the file paths as strings.
	 * If the paths are equal, the two offset integers are compared.
	 * */
	private static class LocationComparator implements Comparator<ILocateableNode> {

		@Override
		public int compare(final ILocateableNode arg0, final ILocateableNode arg1) {
			IResource f0 = arg0.getLocation().getFile();
			IResource f1 = arg1.getLocation().getFile();
			if (!f0.equals(f1)) {
				return f0.getFullPath().toString().compareTo(f1.getFullPath().toString());
			}
			int o0 = arg0.getLocation().getOffset();
			int o1 = arg1.getLocation().getOffset();
			return (o0 < o1) ? -1 : ((o0 == o1) ? 0 : 1);//TODO update with Java 1.7 to Integer.compare
		}
		
	}
	
}
