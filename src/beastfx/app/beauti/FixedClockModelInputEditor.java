package beastfx.app.beauti;


import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.InputEditor;
import beastfx.app.util.FXUtils;
import beast.base.core.BEASTInterface;
import beast.base.core.Input;
import beast.base.evolution.branchratemodel.FixedClockModel;

public class FixedClockModelInputEditor extends InputEditor.Base {

	public FixedClockModelInputEditor(BeautiDoc doc) {
		super(doc);
	}

	@Override
	public Class<?> type() {
		return FixedClockModel.class;
	}

	@Override
	public void init(Input<?> input, BEASTInterface beastObject, int itemNr, ExpandOption isExpandOption,
					 boolean addButtons) {
        pane = FXUtils.newHBox();
		super.init(input, beastObject, itemNr, isExpandOption, addButtons);
		m_input = input;
		m_beastObject = beastObject;
		this.itemNr= itemNr;

		m_bAddButtons = true;
		addInputLabel("Fixed tree model. Do not change.", "<html>Fixed tree model.<br>Do not change.</html>");

		m_bAddButtons = addButtons;

		//add(m_entry);
        // add(Box.createHorizontalGlue());
        addValidationLabel();
        getChildren().add(pane);
	}

}