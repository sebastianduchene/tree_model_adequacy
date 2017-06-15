package beast.app.beauti;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import beast.app.util.Utils;
import beast.core.BEASTInterface;
import beast.core.State;
import beast.evolution.alignment.Alignment;
import beast.evolution.alignment.Sequence;
import beast.evolution.likelihood.FixedTreeLikelihood;
import beast.evolution.tree.Tree;
import beast.util.NexusParser;
import beast.util.TreeParser;

public class BeautiFixedTreeAlignmentProvider extends BeautiAlignmentProvider {

	@Override
	protected List<BEASTInterface> getAlignments(BeautiDoc doc) {
		try {
            File file = Utils.getLoadFile("Open tree file with fixed tree");
            if (file != null) {
            	NexusParser parser = new NexusParser();
            	parser.parseFile(file);
            	if (parser.trees == null || parser.trees.size() == 0) {
            		JOptionPane.showMessageDialog(null, "Did not find any tree in the file -- giving up.");
            		return null;
            	}
            	if (parser.trees.size() > 1) {
            		JOptionPane.showMessageDialog(null, "Found more than one tree in the file -- expected only 1!");
            		return null;
            	}
            	Tree tree = parser.trees.get(0);
            	
            	// create dummy alignment
            	Alignment data = new Alignment();
            	List<Sequence> seqs = new ArrayList<>();
            	for (String name : tree.getTaxaNames()) {
            		seqs.add(new Sequence(name,"?"));
            	}
            	data.initByName("sequence", seqs);
            	String id = file.getName();
            	if (id.lastIndexOf('.') > -1) {
            		id = id.substring(0, id.lastIndexOf('.'));
            	}
            	data.setID(id);

            	TreeParser treeParser = new TreeParser();
            	treeParser.initByName("newick", tree.getRoot().toNewick(),
            			"IsLabelledNewick", true,
            			"taxa", data,
            			"adjustTipHeights", false,
            			"estimate", false
            			);
            	treeParser.setID("Tree.t:" + id);
            	doc.registerPlugin(treeParser);
            	
            	// park the tree in the state
                State state = (State) doc.pluginmap.get("state");
                state.stateNodeInput.get().add(treeParser);
            
            	List<BEASTInterface> list = new ArrayList<>();
            	list.add(data);
            	addAlignments(doc, list);
            	return list;
            }
        } catch (Exception e) {
        	e.printStackTrace();
        	JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
		return null;
	}

	@Override
	protected int matches(Alignment alignment) {
		for (BEASTInterface output : alignment.getOutputs()) {
			if (output instanceof FixedTreeLikelihood) {
				return 10;
			}
		}
		return 0;
	}
	
	@Override
	void editAlignment(Alignment alignment, BeautiDoc doc) {
		
		FixedTreeLikelihood likelihood = null;
		for (BEASTInterface output : alignment.getOutputs()) {
			if (output instanceof FixedTreeLikelihood) {
				likelihood = (FixedTreeLikelihood) output;
				TreeParser parser = (TreeParser) likelihood.treeInput.get();
				
				Object result = JOptionPane.showInputDialog("Edit Newick tree:", parser.newickInput.get());
				if (result != null) {
					// TODO: check taxon set has not changed.
					parser.newickInput.setValue(result, parser);
				}
				return;
			}
		}
	}

}
