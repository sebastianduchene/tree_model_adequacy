package modeladequacy.app.beauti;

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.ListInputEditor;
import beastfx.app.util.Alert;
import javafx.embed.swing.SwingNode;
import javafx.scene.control.Button;
import treestat2.StatisticsPanel;
import treestat2.TreeStatData;
import treestat2.statistics.TreeSummaryStatistic;
import beast.base.core.BEASTInterface;
import beast.base.core.Input;

public class TreeStatInputEditor extends ListInputEditor {

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

		Button button = new Button("Edit statistics");
		button.setOnAction(e -> {
			try {
				List<TreeSummaryStatistic<?>> stats = (List<TreeSummaryStatistic<?>>) input.get();
				TreeStatData treeStatData = new TreeStatData();
				treeStatData.statistics.addAll(stats);

				StatisticsPanel panel = new StatisticsPanel(null, treeStatData);
				SwingNode sn = new SwingNode();
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						int result = JOptionPane.showConfirmDialog(null, panel, "Pick your Tree Statistics",
								JOptionPane.OK_CANCEL_OPTION);
						if (result == JOptionPane.OK_OPTION) {
							stats.clear();
							for (TreeSummaryStatistic stat : treeStatData.statistics) {
								stats.add(stat);
							}
						}
					}
				});

			} catch (Exception ee) {
				Alert.showMessageDialog(null, "Fatal exception: " + ee, "Please report this to the authors",
						Alert.ERROR_MESSAGE);
				ee.printStackTrace();
			}
		});
		getChildren().add(button);
	}

}