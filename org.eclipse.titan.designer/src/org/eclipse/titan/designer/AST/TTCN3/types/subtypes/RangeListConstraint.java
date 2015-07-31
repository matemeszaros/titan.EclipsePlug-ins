/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Adam Delic
 * */
public final class RangeListConstraint extends SubtypeConstraint {
	static final class SweepPoint {
		/** index into the operand's values/intervals vectors or -1 */
		private int aIdx;
		private int bIdx;
		/** is this interval in the set */
		private boolean unionInterval;
		/** is this interval in the set */
		private boolean intersectionInterval;
		/** is this point in the set */
		private boolean intersectionPoint;

		private SweepPoint(final int a, final int b) {
			aIdx = a;
			bIdx = b;
			unionInterval = false;
			intersectionInterval = false;
			intersectionPoint = false;
		}
	}

	static final class RangeLimit {
		private LimitType value;
		private boolean interval;

		private RangeLimit(final LimitType v, final boolean i) {
			value = v;
			interval = i;
		}
	}

	private final LimitType.Type limitType;
	/** range limits */
	private final List<RangeLimit> rlList;

	/** empty set constructor */
	public RangeListConstraint(final LimitType.Type limitType) {
		this.limitType = limitType;
		this.rlList = new ArrayList<RangeLimit>();
	}

	private RangeListConstraint(final LimitType.Type limitType, final List<RangeLimit> rlList) {
		this.limitType = limitType;
		this.rlList = rlList;
	}

	/** single value set */
	public RangeListConstraint(final LimitType l) {
		limitType = l.getType();
		rlList = new ArrayList<RangeLimit>(1);
		rlList.add(new RangeLimit(l, false));
	}

	/** value range set */
	public RangeListConstraint(final LimitType lBegin, final LimitType lEnd) {
		limitType = lBegin.getType();
		if (lBegin.compareTo(lEnd) == 0) {
			rlList = new ArrayList<RangeLimit>(1);
			rlList.add(new RangeLimit(lBegin, false));
		} else {
			rlList = new ArrayList<RangeLimit>(2);
			rlList.add(new RangeLimit(lBegin, true));
			rlList.add(new RangeLimit(lEnd, false));
		}
	}

	public LimitType.Type getLimitType() {
		return limitType;
	}

	@Override
	public RangeListConstraint complement() {
		if (rlList.isEmpty()) {
			return new RangeListConstraint(LimitType.getMinimum(limitType), LimitType.getMaximum(limitType));
		}

		List<RangeLimit> retVal = new ArrayList<RangeLimit>();
		LimitType min = LimitType.getMinimum(limitType);
		RangeLimit rl = rlList.get(0);
		if (rl.value.compareTo(min) != 0) {
			if (min.isAdjacent(rl.value)) {
				retVal.add(new RangeLimit(min, false));
			} else {
				retVal.add(new RangeLimit(min, true));
				retVal.add(new RangeLimit(rl.value.decrement(), false));
			}
		}

		int last = rlList.size() - 1;
		for (int i = 0; i < last; i++) {
			rl = rlList.get(i);
			RangeLimit rl1 = rlList.get(i + 1);
			if (!rl.interval) {
				if (rl.value.increment().compareTo(rl1.value.decrement()) == 0) {
					retVal.add(new RangeLimit(rl.value.increment(), false));
				} else {
					retVal.add(new RangeLimit(rl.value.increment(), true));
					retVal.add(new RangeLimit(rl1.value.decrement(), false));
				}
			}
		}

		rl = rlList.get(last);
		LimitType max = LimitType.getMaximum(limitType);
		if (rl.value.compareTo(max) != 0) {
			if (rl.value.isAdjacent(max)) {
				retVal.add(new RangeLimit(max, false));
			} else {
				retVal.add(new RangeLimit(rl.value.increment(), true));
				retVal.add(new RangeLimit(max, false));
			}
		}
		return new RangeListConstraint(limitType, retVal);
	}

	public RangeListConstraint setOperation(final SubtypeConstraint other, final boolean isUnion) {
		RangeListConstraint o = (RangeListConstraint) other;

		if (rlList.isEmpty()) {
			return isUnion ? o : this;
		}
		if (o.rlList.isEmpty()) {
			return isUnion ? this : o;
		}

		List<SweepPoint> sweepPoints = new ArrayList<SweepPoint>();
		SweepPoint spi = new SweepPoint(0, 0);
		while ((spi.aIdx < rlList.size()) || (spi.bIdx < o.rlList.size())) {
			if (spi.aIdx >= rlList.size()) {
				sweepPoints.add(new SweepPoint(-1, spi.bIdx));
				spi.bIdx++;
			} else if (spi.bIdx >= o.rlList.size()) {
				sweepPoints.add(new SweepPoint(spi.aIdx, -1));
				spi.aIdx++;
			} else {
				int compRv = rlList.get(spi.aIdx).value.compareTo(o.rlList.get(spi.bIdx).value);
				if (compRv < 0) {
					sweepPoints.add(new SweepPoint(spi.aIdx, -1));
					spi.aIdx++;
				} else if (compRv == 0) {
					sweepPoints.add(new SweepPoint(spi.aIdx, spi.bIdx));
					spi.aIdx++;
					spi.bIdx++;
				} else {
					sweepPoints.add(new SweepPoint(-1, spi.bIdx));
					spi.bIdx++;
				}
			}
		}

		boolean inA = false;
		boolean inB = false;
		for (int i = 0; i < sweepPoints.size(); i++) {
			boolean aInterval = inA;
			boolean aPoint = false;
			spi = sweepPoints.get(i);
			if (spi.aIdx != -1) {
				aPoint = true;
				if (rlList.get(spi.aIdx).interval) {
					aInterval = true;
					inA = true;
				} else {
					aInterval = false;
					inA = false;
				}
			}
			boolean bInterval = inB;
			boolean bPoint = false;
			if (spi.bIdx != -1) {
				bPoint = true;
				if (o.rlList.get(spi.bIdx).interval) {
					bInterval = true;
					inB = true;
				} else {
					bInterval = false;
					inB = false;
				}
			}
			spi.unionInterval = aInterval || bInterval;
			spi.intersectionPoint = (aPoint || inA) && (bPoint || inB);
			spi.intersectionInterval = aInterval && bInterval;
		}

		if (isUnion) {
			for (int i = 1; i < sweepPoints.size(); i++) {
				SweepPoint spFirst = sweepPoints.get(i - 1);
				SweepPoint spSecond = sweepPoints.get(i);
				LimitType first, second;
				if (spFirst.aIdx != -1) {
					first = rlList.get(spFirst.aIdx).value;
				} else {
					first = o.rlList.get(spFirst.bIdx).value;
				}
				if (spSecond.aIdx != -1) {
					second = rlList.get(spSecond.aIdx).value;
				} else {
					second = o.rlList.get(spSecond.bIdx).value;
				}
				if (first.isAdjacent(second)) {
					spFirst.unionInterval = true;
					spFirst.intersectionInterval = spFirst.intersectionPoint && spSecond.intersectionPoint;
				}
			}
		}

		List<RangeLimit> retVal = new ArrayList<RangeLimit>();
		for (int i = 0; i < sweepPoints.size(); i++) {
			spi = sweepPoints.get(i);
			if (isUnion) {
				if ((i == 0) || !sweepPoints.get(i - 1).unionInterval || !spi.unionInterval) {
					LimitType l;
					if (spi.aIdx != -1) {
						l = rlList.get(spi.aIdx).value;
					} else {
						l = o.rlList.get(spi.bIdx).value;
					}
					retVal.add(new RangeLimit(l, spi.unionInterval));
				}
			} else {
				if (spi.intersectionPoint) {
					if ((i == 0) || !sweepPoints.get(i - 1).intersectionInterval || !spi.intersectionInterval) {
						LimitType l;
						if (spi.aIdx != -1) {
							l = rlList.get(spi.aIdx).value;
						} else {
							l = o.rlList.get(spi.bIdx).value;
						}
						retVal.add(new RangeLimit(l, spi.intersectionInterval));
					}
				}
			}
		}

		return new RangeListConstraint(limitType, retVal);
	}

	@Override
	public RangeListConstraint intersection(final SubtypeConstraint other) {
		return setOperation(other, false);
	}

	@Override
	public RangeListConstraint union(final SubtypeConstraint other) {
		return setOperation(other, true);
	}

	@Override
	public boolean isElement(final Object o) {
		if (rlList.isEmpty()) {
			return false;
		}
		// binary search
		LimitType l = (LimitType) o;
		int lowerIdx = 0;
		int upperIdx = rlList.size() - 1;
		while (upperIdx > lowerIdx + 1) {
			int middleIndex = lowerIdx + (upperIdx - lowerIdx) / 2;
			if (rlList.get(middleIndex).value.compareTo(l) < 0) {
				lowerIdx = middleIndex;
			} else {
				upperIdx = middleIndex;
			}
		}

		if (lowerIdx == upperIdx) {
			int compRv = rlList.get(lowerIdx).value.compareTo(l);
			if (compRv == 0) {
				return true;
			} else if (compRv < 0) {
				return rlList.get(lowerIdx).interval;
			} else {
				return ((lowerIdx > 0) ? rlList.get(lowerIdx - 1).interval : false);
			}
		}

		int compRv = rlList.get(lowerIdx).value.compareTo(l);
		if (compRv > 0) {
			return ((lowerIdx > 0) ? rlList.get(lowerIdx - 1).interval : false);
		} else if (compRv == 0) {
			return true;
		} else {
			compRv = rlList.get(upperIdx).value.compareTo(l);
			if (compRv > 0) {
				return rlList.get(upperIdx - 1).interval;
			} else if (compRv == 0) {
				return true;
			} else {
				return rlList.get(upperIdx).interval;
			}
		}
	}

	@Override
	public TernaryBool isEmpty() {
		return TernaryBool.fromBool(rlList.isEmpty());
	}

	@Override
	public TernaryBool isEqual(final SubtypeConstraint other) {
		RangeListConstraint rlc = (RangeListConstraint) other;
		if (rlList.size() != rlc.rlList.size()) {
			return TernaryBool.TFALSE;
		}
		for (int i = 0; i < rlList.size(); i++) {
			if (rlList.get(i).value.compareTo(rlc.rlList.get(i).value) != 0) {
				return TernaryBool.TFALSE;
			}
			if (rlList.get(i).interval != rlc.rlList.get(i).interval) {
				return TernaryBool.TFALSE;
			}
		}
		return TernaryBool.TTRUE;
	}

	@Override
	public TernaryBool isFull() {
		if (rlList.size() != 2) {
			return TernaryBool.TFALSE;
		}
		if (!rlList.get(0).interval) {
			return TernaryBool.TFALSE;
		}
		LimitType l = rlList.get(0).value;
		if (l.compareTo(LimitType.getMinimum(limitType)) != 0) {
			return TernaryBool.TFALSE;
		}
		l = rlList.get(1).value;
		if (l.compareTo(LimitType.getMaximum(limitType)) != 0) {
			return TernaryBool.TFALSE;
		}
		return TernaryBool.TTRUE;
	}

	/**
	 * return the minimum value contained by this set or null of the set is
	 * empty
	 */
	public LimitType getMinimal() {
		return (!rlList.isEmpty()) ? rlList.get(0).value : null;
	}

	/**
	 * return the maximum value contained by this set or null of the set is
	 * empty
	 */
	public LimitType getMaximal() {
		return (!rlList.isEmpty()) ? rlList.get(rlList.size() - 1).value : null;
	}

	public void toString(final StringBuilder sb, final boolean addBrackets) {
		if (addBrackets) {
			sb.append('(');
		}
		int last = rlList.size() - 1;
		for (int i = 0; i <= last; i++) {
			RangeLimit rl = rlList.get(i);
			rl.value.toString(sb);
			if (rl.interval) {
				sb.append("..");
			} else if (i < last) {
				sb.append(", ");
			}
		}
		if (addBrackets) {
			sb.append(')');
		}
	}

	@Override
	public void toString(final StringBuilder sb) {
		toString(sb, true);
	}

}
