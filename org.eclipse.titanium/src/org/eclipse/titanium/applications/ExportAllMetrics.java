package org.eclipse.titanium.applications;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.utils.RiskLevel;
import org.eclipse.titanium.metrics.view.XLSExporter;

/**
 * Prototype application for extracting the contents of the metrics view into
 * an excel file in headless mode. It will analyze every project in the
 * workspace, and save the reports for each project into an excel file with the
 * name <project_name>.xls
 * 
 * It awaits one single parameter, the folder to place to excel files into.
 * */
public class ExportAllMetrics extends InformationExporter {

	@Override
	protected boolean checkParameters(final String[] args) {
		if (args.length == 0 || args.length > 1) {
			System.out.println("This application takes as parameter the location of the resulting .XLS files.");
			return false;
		}

		return true;
	}

	@Override
	protected void exportInformationForProject(String[] args, IProject project, IProgressMonitor monitor) {
		final MetricData data = MetricData.measure(project);
		
		final RiskLevel r = RiskLevel.NO;
		final String fn = args[0] + project.getName() + ".xls";
		final File file = new File(fn);

		final XLSExporter xlsWriter = new XLSExporter(data);
		xlsWriter.setFile(file);
		xlsWriter.write(r);
	}

}
