/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.models;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.eclipse.titan.common.utils.ObjectUtils;
import org.eclipse.titan.log.viewer.parsers.data.LogRecord;

public class FilterPattern {
	public enum Field {
		SOURCE_INFO, MESSAGE
	}
	
	private SortedMap<String, Boolean> eventsToFilter = null;
	private boolean inclusive = true;
	
	private Map<Field, Boolean> fieldsToFilter = null;
	private String filterExpression = null;
	private boolean caseSensitive = false;
	private boolean regularExpression = false;
	private TimeInterval timeInterval = null;
	private Pattern filterExpressionCompiled = null;
	private boolean containsSilentEvents = false;

	public FilterPattern(final Map<String, Boolean> eventsToFilter, final boolean inclusive,
						final boolean containsSilentEvents) {
		setEventsToFilter(eventsToFilter, inclusive, containsSilentEvents);
	}
	
	public FilterPattern(final String filterExpression, final Map<Field, Boolean> fieldsToFilter,
			final boolean caseSensitive, final boolean regularExpression) {
		setFilterExpression(filterExpression, fieldsToFilter, caseSensitive, regularExpression);
	}
	
	public FilterPattern(final TimeInterval timeInterval) {
		setTimeInterval(timeInterval);
	}
	
	public FilterPattern(final FilterPattern otherPattern) {
		setEventsToFilter(otherPattern.eventsToFilter, otherPattern.inclusive, otherPattern.containsSilentEvents);
		setFilterExpression(otherPattern.getFilterExpression(), otherPattern.getFieldsToFilter(), 
				otherPattern.isCaseSensitive(), otherPattern.isRegularExpression());
		setTimeInterval(otherPattern.getTimeInterval());
	}
	
	public SortedMap<String, Boolean> getEventsToFilter() {
		return eventsToFilter;
	}
	
	public boolean isInclusive() {
		return inclusive;
	}

	public void setInclusive(final boolean inclusive) {
		this.inclusive = inclusive;
	}
	
	public Map<Field, Boolean> getFieldsToFilter() {
		return fieldsToFilter;
	}
	
	public String getFilterExpression() {
		return filterExpression != null ? filterExpression : "";
	}
	
	public TimeInterval getTimeInterval() {
		return timeInterval;
	}
	
	public void setCaseSensitive(final boolean isCaseSensitive) {
		if (this.caseSensitive == isCaseSensitive) {
			return;
		}
		this.caseSensitive = isCaseSensitive;
		compileFilterExpression();
	}
	
	public boolean isCaseSensitive() {
		return caseSensitive;
	}
	
	public void setRegularExpression(final boolean isRegularExpression) {
		if (this.regularExpression == isRegularExpression) {
			return;
		}
		this.regularExpression = isRegularExpression;
		compileFilterExpression();
	}
	
	public boolean isRegularExpression() {
		return regularExpression;
	}
	
	public boolean containsSilentEvents() {
		return containsSilentEvents;
	}
	
	public void setTimeInterval(final TimeInterval timeInterval) {
		if (timeInterval != null) {
			this.timeInterval = new TimeInterval(timeInterval);
			return;
		}
		this.timeInterval = null;
	}
	
	public void setEventsToFilter(final Map<String, Boolean> eventsToFilter, final boolean inclusive,
							final boolean containsSilentEvents) {
		this.inclusive = inclusive;
		this.containsSilentEvents = containsSilentEvents;
		if (eventsToFilter != null) {
			this.eventsToFilter = new TreeMap<String, Boolean>();
			for (Map.Entry<String, Boolean> entry : eventsToFilter.entrySet()) {
				this.eventsToFilter.put(entry.getKey(), entry.getValue());
			}
			return;
		}
		this.eventsToFilter = null;
	}
	
	public boolean isEventIgnored(final String event) {
		if (inclusive) {
			return !eventsToFilter.get(event);
		}

		return eventsToFilter.get(event);
	}
	
	public void setFilterExpression(final String filterExpression, final Map<Field, Boolean> fieldsToFilter,
										final boolean isCaseSensitive, final boolean isRegularExpression) {
		if (filterExpression != null && fieldsToFilter != null) {
			this.filterExpression = filterExpression;
			this.caseSensitive = isCaseSensitive;
			this.regularExpression = isRegularExpression;
			this.fieldsToFilter = new HashMap<Field, Boolean>(fieldsToFilter.size());
			for (Map.Entry<Field, Boolean> entry : fieldsToFilter.entrySet()) {
				this.fieldsToFilter.put(entry.getKey(), entry.getValue());
			}
			
			compileFilterExpression();
		} else {
			this.filterExpression = null;
			this.fieldsToFilter = null;
		}
	}
	
	public void compileFilterExpression() {
		String regexp = isRegularExpression() ? filterExpression : convertToRegex(filterExpression);
		
		if (caseSensitive) {
			this.filterExpressionCompiled = Pattern.compile(regexp, Pattern.DOTALL);
		} else {
			this.filterExpressionCompiled = Pattern.compile(regexp, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		}
	}
	
	public String convertToRegex(final String pattern) {
		if (pattern == null || pattern.length() == 0) {
			return ".*";
		}
		
		StringBuilder builder = new StringBuilder();
		int i = 0;
		if (pattern.charAt(0) == '*') {
			++i;
		}
		builder.append(".*");
		for ( ; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			switch(c) {
				case '*':
					builder.append(".*");
					break;
				case '?':
					builder.append(".");
					break;
				case '\\':
					if (i < pattern.length() - 1 && (pattern.charAt(i + 1) == '*' || pattern.charAt(i + 1) == '?')) {
						builder.append("\\" + pattern.charAt(i + 1));
						i++;
						break;
					}
				//$FALL-THROUGH$
			case '{': case '}': case '[': case ']': case '(': case ')':  
				case '^': case '$': case '.': case '|':
					builder.append("\\");
					builder.append(c);
					break;
				default:
					builder.append(c);
					break;
			}
		}
		if (builder.charAt(builder.length() - 2) != '.'
			|| builder.charAt(builder.length() - 1) != '*') {
			builder.append(".*");
		}
		return builder.toString();
	}
	
	public boolean isMessageMatching(final String message) {
		return filterExpressionCompiled.matcher(message).matches();
	}
	
	public boolean match(final LogRecord record) {
		if (eventsToFilter != null && !eventsToFilter.isEmpty()
			&& inclusive != eventsToFilter.get(record.getEventType())) { // xor
				return false;
		}
		
		if (timeInterval != null && !timeInterval.contains(record.getTimestamp())) {
			return false;
		}
		
		if (filterExpression.length() > 0 && fieldsToFilter != null && !fieldsToFilter.isEmpty()) {
			boolean b = false;
			for (Map.Entry<Field, Boolean> entry : fieldsToFilter.entrySet()) {
				if (entry.getValue()) {
					switch(entry.getKey()) {
					case MESSAGE:
						if (filterExpressionCompiled.matcher(record.getMessage()).matches()) {
							b = true;
						}
						break;
					case SOURCE_INFO:
						if (filterExpressionCompiled.matcher(record.getSourceInformation()).matches()) {
							b = true;
						}
						break;
					default: break;
					}
				}
			}
			
			if (!b) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) {
			return true;
		}
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}
		
		FilterPattern rhs = (FilterPattern) o;
		return ObjectUtils.equals(timeInterval, rhs.timeInterval)
				&& ObjectUtils.equals(eventsToFilter, rhs.eventsToFilter)
				&& ObjectUtils.equals(filterExpression, rhs.filterExpression)
				&& ObjectUtils.equals(caseSensitive, rhs.caseSensitive)
				&& ObjectUtils.equals(regularExpression, rhs.regularExpression)
				&& ObjectUtils.equals(inclusive, rhs.inclusive)
				&& ObjectUtils.equals(fieldsToFilter, rhs.fieldsToFilter);
	}
	
	@Override
	public int hashCode() {
		return ObjectUtils.hash(eventsToFilter, fieldsToFilter, filterExpression,
				inclusive, caseSensitive, regularExpression, timeInterval);
	}
}
