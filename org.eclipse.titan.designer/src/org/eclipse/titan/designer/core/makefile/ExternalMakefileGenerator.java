package org.eclipse.titan.designer.core.makefile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.titan.common.path.PathConverter;
import org.eclipse.titan.common.path.PathUtil;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.ProjectBasedBuilder;
import org.eclipse.titan.designer.core.TITANBuilderResourceVisitor;
import org.eclipse.titan.designer.decorators.TITANDecorator;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * This class is used to generate the Makefile used by the build system of Titan.
 * 
 * This variant creates a command that can be used to call the external/commandline makefile generator.
 *
 * @author Kristof Szabados
 * */
public class ExternalMakefileGenerator {
	static final String EMPTY_STRING = "";
	static final String APOSTROPHE = "'";
	static final String BIN_DIRECTORY = "bin";
	static final String MAKEFILEGENERATOR = "ttcn3_makefilegen";
	
	/**
	 * Creates the commandline command to be used to generate the Makefile for the passed project.
	 * 
	 * @param project the project to generate the Makefile for
	 * */
	public static List<String> createMakefilGeneratorCommand(final IProject project) {
		boolean reportDebugInformation = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, false, null);
		
		TITANBuilderResourceVisitor visitor = ProjectBasedBuilder.getProjectBasedBuilder(project).getResourceVisitor();
		
		final Map<String, IFile> files = visitor.getFiles();
		final Map<String, IFile> centralStorageFiles = visitor.getCentralStorageFiles();
		final Map<String, IFile> referencedFiles = ProjectBasedBuilder.getProjectBasedBuilder(project)
				.getFilesofReferencedProjects();

		
		List<String> command = new ArrayList<String>();
		IPreferencesService prefs = Platform.getPreferencesService();
		String pathOfTITAN = prefs.getString(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.TITAN_INSTALLATION_PATH, "",
				null);
		Path makefilegenPath = new Path(pathOfTITAN + File.separatorChar + BIN_DIRECTORY + File.separatorChar + MAKEFILEGENERATOR);
		command.add(PathConverter.convert(makefilegenPath.toOSString(), reportDebugInformation, TITANDebugConsole.getConsole()));

		String decoratorParametersLong = TITANDecorator.propertiesAsParameters(project, true);
		if (!EMPTY_STRING.equals(decoratorParametersLong)) {
			String[] parameters = decoratorParametersLong.split(" ");
			for (int i = 0; i < parameters.length; i++) {
				command.add(parameters[i]);
			}
		}

		for (String path : files.keySet()) {
			command.add(APOSTROPHE + path + APOSTROPHE);
		}

		IPath centralStorageDirectoryPath = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryPath(true);
		String centralStorageDirectory = centralStorageDirectoryPath.toOSString();
		for (String fileName : centralStorageFiles.keySet()) {
			IFile file = centralStorageFiles.get(fileName);
			IProject otherProject = file.getProject();
			IPath referencedCentralStorageDirectoryPath = ProjectBasedBuilder.getProjectBasedBuilder(otherProject).getWorkingDirectoryPath(true);
			String referencedCentralStorageDirectory = referencedCentralStorageDirectoryPath.toOSString();
			String relativePathToDirectory = PathUtil.getRelativePath(centralStorageDirectory, referencedCentralStorageDirectory);
			Path relativePath = new Path(relativePathToDirectory);
			String path = relativePath.append(fileName).toOSString();
			command.add(APOSTROPHE + PathConverter.convert(path, reportDebugInformation, TITANDebugConsole.getConsole())
					+ APOSTROPHE);
		}

		IPath workingDirectoryPath = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryPath(true);
		String workingDirectory = workingDirectoryPath.toOSString();
		for (String fileName : referencedFiles.keySet()) {
			IFile file = referencedFiles.get(fileName);
			IProject otherProject = file.getProject();
			IPath referencedWorkingDirectoryPath = ProjectBasedBuilder.getProjectBasedBuilder(otherProject).getWorkingDirectoryPath(true);
			String referencedWorkingDirectory = referencedWorkingDirectoryPath.toOSString();
			String relativePathToDirectory = PathUtil.getRelativePath(workingDirectory, referencedWorkingDirectory);
			Path relativePath = new Path(relativePathToDirectory);
			String path = relativePath.append(fileName).toOSString();
			command.add(APOSTROPHE + PathConverter.convert(path, reportDebugInformation, TITANDebugConsole.getConsole())
					+ APOSTROPHE);
		}

		return command;
	}
}
