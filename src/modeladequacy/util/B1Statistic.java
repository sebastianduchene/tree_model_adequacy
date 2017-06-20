/*
 * B1Statistic.java
 *
 * Copyright (C) 2002-2006 Alexei Drummond and Andrew Rambaut
 *
 * This file is part of BEAST.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * BEAST is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 *  BEAST is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BEAST; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package modeladequacy.util;

import beast.core.Citation;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;

/**
 *
 * @version $Id: B1Statistic.java,v 1.2 2005/09/28 13:50:56 rambaut Exp $
 *
 * @author Alexei Drummond
 */
@Citation(value="Shao & Sokal (1990) 'Tree balance.' Syst Zool 39:266-276", DOI="10.2307/2992186")
@SummaryStatisticDescription(
        name="B1",
        description="The sum of the reciprocals of the maximum number of nodes between each interior node and a tip (Mi) for all internal nodes except the root.",
        category=SummaryStatisticDescription.Category.TREE_SHAPE,
        allowsNonultrametricTrees = true,
        allowsPolytomies = false,
        allowsUnrootedTrees = false
)
public class B1Statistic extends AbstractTreeSummaryStatistic<Double> {

	public B1Statistic() { }

	public Double[] getSummaryStatistic(Tree tree) {
		double B1 = 0.0;
		for (Node node : tree.getInternalNodes()) {
			if (!node.isRoot()) {
				B1 += 1.0/getMi(node);
			}
		}
		return new Double[] { B1 };
	}

	/**
	 * Assumes strictly bifurcating tree
	 */
	private static int getMi(Node node) {
		int childCount = node.getChildCount();
		if (childCount == 0) return 0;
		int Mi = 0;
		for (int i =0; i < childCount; i++) {
			int mi = getMi(node.getChild(i));
			if (mi > Mi) Mi = mi;
		}
		Mi += 1;
		return Mi;
	}
}
