package modeladequacy.util;

import beast.evolution.tree.Tree;


public class getRootHeight implements TreeSummaryStatistic {

	@Override
	public double getTreeStatistic(Tree tr) {
		return tr.getRoot().getHeight();
	}

}
