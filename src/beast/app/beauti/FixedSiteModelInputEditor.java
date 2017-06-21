package beast.app.beauti;

import beast.evolution.sitemodel.FixedSiteModel;

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
