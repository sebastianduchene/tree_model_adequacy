package modeladequacy.util;

import beast.evolution.tree.Tree;

public class getNumTips implements TreeSummaryStatistic {

	@Override
	public double getTreeStatistic(Tree tr) {
		return tr.getLeafNodeCount();
	}

}
