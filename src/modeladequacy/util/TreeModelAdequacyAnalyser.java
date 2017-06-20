package modeladequacy.util;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
	
	List<TreeSummaryStatistic> stats;
	
	@Override
	public void initAndValidate() {
		stats = new ArrayList<>();
		stats.add(new getDF());
	}

	
	
	@Override
	public void run() throws Exception {
		
		Tree origTree = getOriginalTree();
		
		List<Double> origStats = new ArrayList<>();
		for (int i = 0; i < stats.size(); i++) {
			double s = stats.get(i).getTreeStatistic(origTree);
			origStats.add(s);
		}
		
		// TODO: init stats
					

		
		//
		
		
		String rootDir = rootDirInput.get();
		int treeCount = treeCountInput.get();
		for (int i = 0; i < treeCount; i++) {
			NexusParser parser = new NexusParser();
			File file = new File(rootDir + "/run" + i + "/output.tree");
			parser.parseFile(file);
			Tree tree = parser.trees.get(0);
			// TODO: update stats
		}
		
		// TODO: report stats
	}

	
	private Double endTime() {
		// TODO Auto-generated method stub
		return null;
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
