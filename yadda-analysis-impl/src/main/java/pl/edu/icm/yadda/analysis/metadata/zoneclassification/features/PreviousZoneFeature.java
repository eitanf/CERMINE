package pl.edu.icm.yadda.analysis.metadata.zoneclassification.features;

import pl.edu.icm.yadda.analysis.classification.features.FeatureCalculator;
import pl.edu.icm.yadda.analysis.textr.model.BxPage;
import pl.edu.icm.yadda.analysis.textr.model.BxZone;

public class PreviousZoneFeature implements FeatureCalculator<BxZone, BxPage>
{
	private static String featureName = "PreviousZoneFeature";
	
	@Override
	public String getFeatureName() {
		return featureName;
	}

	@Override
	public double calculateFeatureValue(BxZone object, BxPage context) {
		for(BxZone zone: context.getZones()) {
			if(zone == object) {
				if(zone.hasPrev()) {
					return (double) zone.getPrev().getLabel().ordinal();
				} else
					return -1.0;
			}
		}
		return -1.0;
	}
}