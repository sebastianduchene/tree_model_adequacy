package modeladequacy.util;

import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;
import beast.base.evolution.tree.TreeUtils;

public class getTreeSummaryStats {

	
	static Double getTipAgeRange(Tree tr){
		int nTips = tr.getLeafNodeCount();
		Double oldestTipAge = 0.0;
		Node [] nodes = tr.getNodesAsArray();
		for (int i = 0; i <  nTips; i++){
			Double newTipAge = nodes[i].getHeight();
			if (newTipAge > oldestTipAge){
				oldestTipAge = newTipAge;
			}
		}
		return oldestTipAge;
	}
	
	static Double getDf(Tree tr){
		return TreeUtils.getExternalLength(tr) / TreeUtils.getTreeLength(tr, tr.getRoot());
	}

	static int getNtips(Tree tr){
		return tr.getLeafNodeCount();
	}
	
	static Double getRootHeight(Tree tr){
		return tr.getRoot().getHeight();
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
