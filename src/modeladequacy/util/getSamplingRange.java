package modeladequacy.util;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;

public class getSamplingRange implements TreeSummaryStatistic{
	
	@Override
	public double getTreeStatistic(Tree tr){
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
}
