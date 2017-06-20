package modeladequacy.util;

import java.util.List;

import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;

//public class getTimeMaxLineages implements TreeSummaryStatistic{
//	null
//}


Node nodes[] = origTree.getNodesAsArray();
System.out.println(origTree.getNodeCount());
Double[][] nodeHeightList = new Double[2][origTree.getNodeCount()];
Double[] allNodeHeights = new Double[origTree.getNodeCount()];
for(int n = 0; n < origTree.getNodeCount(); n++){
	allNodeHeights[n] = nodes[n].getHeight();
}
// Need to sort node Heights


for(int j=0; j < origTree.getNodeCount(); j++){
	Node n = nodes[j];
	List<Node> children = n.getChildren();
	if(children.size() > 0 && n.isRoot() == false){
		Double childHeight = n.getChild(0).getHeight();
		Double parentHeight = n.getParent().getHeight();
		nodeHeightList[0][j]  = childHeight;
		nodeHeightList[1][j] = parentHeight;
	}
}
System.out.println("test");

int lineagesOverTime[] = new int[nodeHeightList[0].length];
//Double intervalTimes[] = new Double[allNodeHeights.length * 2];
// Lineages through time
for(int k=1; k < allNodeHeights.length; k++){
	//System.out.println("I am here");
	Double startTime = allNodeHeights[k-1];
	Double endTime = allNodeHeights[k];
	int nLineages = 0;
	for(int p = 0; p < nodeHeightList[0].length; p++){
		Double branchStart = nodeHeightList[0][p];
		Double branchEnd = nodeHeightList[1][p];
		//System.out.println("branch start");
		//System.out.println(branchStart);
		//System.out.println("end time");
		//System.out.println(endTime);
		if((branchStart != null && endTime != null) && (branchEnd != null && endTime != null)){
			if(branchStart < startTime && branchEnd > endTime){
				nLineages++;
			}if((branchStart > startTime && branchStart < endTime) || (branchEnd < endTime && branchEnd > startTime)){
				nLineages++;
			}
		}
	}
	lineagesOverTime[k-1] = nLineages;
	
	// Go through all elements in nodeHeightList and find which are in the interval
}

for(int i=1; i < lineagesOverTime.length; i++){
	System.out.println(lineagesOverTime[i]);
	System.out.println(nodeHeightList[0][i]);
	System.out.println(nodeHeightList[1][i]);
}