package modeladequacy.util;


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import beast.app.treestat.statistics.AbstractTreeSummaryStatistic;
import beast.app.util.Application;
import beast.app.util.TreeFile;
import beast.core.Description;
import beast.core.Input;
import beast.core.Runnable;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.NexusParser;
import beast.evolution.tree.TreeUtils;



@Description("Analyse set of trees created through Tree Model Adequacy")
public class TreeModelAdequacyAnalyser extends Runnable {
	// assume the root dir has subdirs called run0, run1, run2,...
	public Input<String> rootDirInput = new Input<>("rootdir", "root directory for storing individual tree files (default /tmp)", "/tmp");
	public Input<Integer> treeCountInput = new Input<>("nrOfTrees", "the number of trees to use, default 100", 100);
	public Input<TreeFile> treeFileInput = new Input<>("treefile", "file with original tree to test adequacy for");
	public Input<Tree> treeInput = new Input<>("tree", "original tree to test adequacy for");
	public Input<List<AbstractTreeSummaryStatistic<?>>> statsInput = new Input<>("statistic", "set of statistics that need to be produced", new ArrayList<>());
	
	
	
	List<AbstractTreeSummaryStatistic<?>> stats;
	
	@Override
	public void initAndValidate() {
		stats = statsInput.get();
	}

	
	
	@Override
	public void run() throws Exception {
		
		Tree origTree = getOriginalTree();
		
		// init stats
		Map<String, Object> origStats = new LinkedHashMap<>();
		for (AbstractTreeSummaryStatistic<?> stat : stats) {
			Map<String,?> map = stat.getStatistics(origTree);
			for (String name : map.keySet()) {
				origStats.put(name, map.get(name));
			}
		}
		
		
		String rootDir = rootDirInput.get();
		int treeCount = treeCountInput.get();
		Map<String, Object>[] treeStats = new LinkedHashMap[treeCount];

		for (int i = 0; i < treeCount; i++) {
			treeStats[i] = new LinkedHashMap<>();
			NexusParser parser = new NexusParser();
			File file = new File(rootDir + "/run" + i + "/output.tree");
			parser.parseFile(file);
			Tree tree = parser.trees.get(0);
			for (AbstractTreeSummaryStatistic<?> stat : stats) {
				Map<String,?> map = stat.getStatistics(tree);
				for (String name : map.keySet()) {
					treeStats[i].put(name, map.get(name));
				}
			}
		}
		
		// report stats
		logStats(System.out, origStats, treeStats);
		
	}

	

	private void logStats(PrintStream out, Map<String, Object> origStats, Map<String, Object>[] treeStats) {
		List<String> labels = new ArrayList<>();
		for (String label : origStats.keySet()) {
			labels.add(label);
		}
		
		out.print("state\t");
		for (String label : labels) {
			out.print(label+"\t");
		}
		out.println();
		
		// first line contains stats for original tree
		out.print("0\t");
		for (String label : labels) {
			Object o = origStats.get(label);
			out.print(o.toString() + "\t");
		}
		out.println();
		
		// print out stats for sampled trees
		for (int i = 0; i < treeStats.length; i++) {
			out.print((i+1) + "\t");
			Map<String, Object> stats = treeStats[i];
			for (String label : labels) {
				Object o = stats.get(label);
				out.print(o.toString() + "\t");
			}
			out.println();			
		}		
	}



	private Tree getOriginalTree() throws IOException {
		if (treeInput.get() != null) {
			return treeInput.get();
		}
		NexusParser parser = new NexusParser();
		System.out.println("get original tree");
		parser.parseFile(treeFileInput.get());
		Tree tree = parser.trees.get(0);
		return tree;
	}

	public static void main(String[] args) throws Exception {
		new Application(new TreeModelAdequacyAnalyser(), "Tree Model Adeqaucy Analyser", args);
	}
}
