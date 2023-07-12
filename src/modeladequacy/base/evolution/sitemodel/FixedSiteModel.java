package modeladequacy.base.evolution.sitemodel;

import beast.base.core.Input.Validate;
import beast.base.evolution.sitemodel.SiteModel;

public class FixedSiteModel extends SiteModel {

	public FixedSiteModel() {
		substModelInput.setRule(Validate.OPTIONAL);
	}
}
