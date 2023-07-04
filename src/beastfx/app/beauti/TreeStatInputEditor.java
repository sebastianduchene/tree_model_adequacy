package beastfx.app.beauti;

import java.util.List;

import javax.swing.*;

import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.ListInputEditor;
import treestat2.StatisticsPanel;
import treestat2.TreeStatApp;
import treestat2.TreeStatData;
import treestat2.TreeStatFrame;
import treestat2.statistics.AbstractTreeSummaryStatistic;
import treestat2.statistics.TreeSummaryStatistic;
import beast.base.core.BEASTInterface;
import beast.base.core.Input;

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
        Box box = Box.createHorizontalBox();
        box.add(Box.createGlue());
        box.add(button);
    }


}