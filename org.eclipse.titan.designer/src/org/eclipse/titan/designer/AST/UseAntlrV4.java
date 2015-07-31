package org.eclipse.titan.designer.AST;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
//TODO: get rid of it after migrating to antlr4 from antlr2 
public final class UseAntlrV4 {
public static boolean isAntlrV4 = false;	

	static {
		final IPreferencesService ps = Platform.getPreferencesService();
		if ( ps != null ) {
			isAntlrV4 = ps.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DEBUG_CONSOLE_ANTLR_V4, false, null);
			final Activator activator = Activator.getDefault();
			if (activator != null) {
				activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
					@Override
					public void propertyChange(final PropertyChangeEvent event) {
						final String property = event.getProperty();
						if (PreferenceConstants.DEBUG_CONSOLE_ANTLR_V4.equals(property)) {
							isAntlrV4 = ps.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DEBUG_CONSOLE_ANTLR_V4, false, null);
							// the information is needed to be cleared because the change between antlr2 and antlr4 causes null pointer exception.
							// if the blockV2 and blockV4 are mixed
							GlobalParser.clearAllInformation();
						}
					}
				});
			};
		}
	}
}
