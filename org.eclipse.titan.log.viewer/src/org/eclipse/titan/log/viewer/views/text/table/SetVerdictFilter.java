package org.eclipse.titan.log.viewer.views.text.table;

import org.eclipse.titan.log.viewer.preferences.PreferencesHolder;

class SetVerdictFilter {
	boolean setverdictError;
	boolean setverdictFail;
	boolean setverdictInconc;
	boolean setverdictNone;
	boolean setverdictPass;

	public SetVerdictFilter(PreferencesHolder preferences) {
		setverdictError = preferences.getSetverdictError();
		setverdictFail = preferences.getSetverdictFail();
		setverdictInconc = preferences.getSetverdictInconc();
		setverdictNone = preferences.getSetverdictNone();
		setverdictPass = preferences.getSetverdictPass();
	}

	public boolean isSetverdictError() {
		return setverdictError;
	}

	public boolean isSetverdictFail() {
		return setverdictFail;
	}

	public boolean isSetverdictInconc() {
		return setverdictInconc;
	}

	public boolean isSetverdictNone() {
		return setverdictNone;
	}

	public boolean isSetverdictPass() {
		return setverdictPass;
	}
}
