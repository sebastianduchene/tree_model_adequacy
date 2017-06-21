package beast.evolution.sitemodel;

import beast.core.Input.Validate;

public class FixedSiteModel extends SiteModel {

	public FixedSiteModel() {
		substModelInput.setRule(Validate.OPTIONAL);
	}
}
