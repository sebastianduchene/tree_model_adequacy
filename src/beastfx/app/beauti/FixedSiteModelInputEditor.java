package beastfx.app.beauti;

import beast.base.evolution.sitemodel.FixedSiteModel;
import beastfx.app.inputeditor.BeautiDoc;

public class FixedSiteModelInputEditor extends FixedClockModelInputEditor {

	public FixedSiteModelInputEditor(BeautiDoc doc) {
		super(doc);
	}

	@Override
	public Class<?> type() {
		return FixedSiteModel.class;
	}

}
