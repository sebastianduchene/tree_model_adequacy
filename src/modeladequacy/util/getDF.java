package modeladequacy.util;

import beast.evolution.tree.Tree;
import beast.evolution.tree.TreeUtils;

public class getDF implements TreeSummaryStatistic {

	@Override
	public double getTreeStatistic(Tree tr) {
		return TreeUtils.getExternalLength(tr) / TreeUtils.getTreeLength(tr, tr.getRoot());
	}

}
