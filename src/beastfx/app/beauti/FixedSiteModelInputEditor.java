package beastfx.app.beauti;

import beast.base.evolution.sitemodel.FixedSiteModel;
import beastfx.app.inputeditor.BeautiDoc;

public class FixedSiteModelInputEditor extends FixedClockModelInputEditor {

	private static final long serialVersionUID = 1L;

	public FixedSiteModelInputEditor(BeautiDoc doc) {
		super(doc);
	}

	@Override
	public Class<?> type() {
		return FixedSiteModel.class;
	}

}
