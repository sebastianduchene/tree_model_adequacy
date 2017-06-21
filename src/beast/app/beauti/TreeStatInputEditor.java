package beast.app.beauti;

import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import beast.app.draw.ListInputEditor;
import beast.app.treestat.StatisticsPanel;
import beast.app.treestat.TreeStatApp;
import beast.app.treestat.TreeStatData;
import beast.app.treestat.TreeStatFrame;
import beast.app.treestat.statistics.AbstractTreeSummaryStatistic;
import beast.app.treestat.statistics.TreeSummaryStatistic;
import beast.core.BEASTInterface;
import beast.core.Input;

public class TreeStatInputEditor extends ListInputEditor {
	private static final long serialVersionUID = 1L;

	public TreeStatInputEditor(BeautiDoc doc) {
		super(doc);
	}

	@Override
    public Class<?> type() {
        return List.class;
    }

    @Override
    public Class<?> baseType() {
    	return TreeSummaryStatistic.class;
    }

    @Override
    public void init(Input<?> input, BEASTInterface beastObject, int itemNr, ExpandOption isExpandOption,
    		boolean addButtons) {
    	this.m_input = input;
    	this.m_beastObject = beastObject;
    	
    	JButton button = new JButton("Edit statistics");
    	button.addActionListener(e -> {
    		try {
    		List<TreeSummaryStatistic<?>> stats = (List<TreeSummaryStatistic<?>>) input.get();
            TreeStatData treeStatData = new TreeStatData();
            treeStatData.statistics.addAll(stats);
            
            StatisticsPanel panel = new StatisticsPanel(null, treeStatData); 
            int result = JOptionPane.showConfirmDialog(null, panel, "Pick your Tree Statistics", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
	            stats.clear();
	            for (TreeSummaryStatistic stat : treeStatData.statistics) {
	            	stats.add(stat);
	            }
            }

        } catch (Exception ee) {
            JOptionPane.showMessageDialog(new JFrame(), "Fatal exception: " + ee,
                    "Please report this to the authors",
                    JOptionPane.ERROR_MESSAGE);
            ee.printStackTrace();
        }
    	});
    	add(button);
    }
    
    
}
