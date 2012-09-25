package pl.edu.icm.coansys.metaextr.metadata.evaluation;

import pl.edu.icm.coansys.metaextr.textr.model.BxZone;
import pl.edu.icm.coansys.metaextr.textr.model.BxPage;
import pl.edu.icm.coansys.metaextr.textr.model.BxZoneLabelCategory;
import pl.edu.icm.coansys.metaextr.textr.model.BxZoneLabel;
import pl.edu.icm.coansys.metaextr.textr.model.BxDocument;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.ParseException;

import pl.edu.icm.coansys.metaextr.classification.features.FeatureVectorBuilder;
import pl.edu.icm.coansys.metaextr.classification.hmm.HMMServiceImpl;
import pl.edu.icm.coansys.metaextr.classification.hmm.HMMZoneClassifier;
import pl.edu.icm.coansys.metaextr.classification.hmm.probability.HMMProbabilityInfo;
import pl.edu.icm.coansys.metaextr.classification.hmm.probability.HMMProbabilityInfoFactory;
import pl.edu.icm.coansys.metaextr.classification.hmm.training.TrainingElement;
import pl.edu.icm.coansys.metaextr.metadata.sampleselection.SampleSelector;
import pl.edu.icm.coansys.metaextr.metadata.zoneclassification.tools.BxDocsToHMMConverter;

/*
 *  @author Pawel Szostek (p.szostek@icm.edu.pl)
 */

public class HMMZoneClassificationEvaluator extends CrossvalidatingZoneClassificationEvaluator
{
	@Override
	protected HMMZoneClassifier getZoneClassifier(List<BxDocument> trainingDocuments)
	{
		System.out.println("HMM");
		FeatureVectorBuilder<BxZone, BxPage> featureVectorBuilder = getFeatureVectorBuilder();
        BxDocsToHMMConverter node = new BxDocsToHMMConverter();
        node.setFeatureVectorBuilder(featureVectorBuilder);
        node.setLabelMap(BxZoneLabel.getLabelToGeneralMap());
        
        List<TrainingElement<BxZoneLabel>> trainingElements;
        try {
        	trainingElements = node.process(trainingDocuments);
        } catch(Exception e) {
			throw new RuntimeException("Unable to process the delivered training documents!");
		}
        
		HMMProbabilityInfo<BxZoneLabel> hmmProbabilities;
		try {
			hmmProbabilities = HMMProbabilityInfoFactory.getFVHMMProbability(trainingElements, featureVectorBuilder);
		} catch(Exception e) {
			throw new RuntimeException("Unable to figure out HMM probability information!");
		}
		
		HMMZoneClassifier zoneClassifier = new HMMZoneClassifier(
				new HMMServiceImpl(),
				hmmProbabilities,
				BxZoneLabel.valuesOfCategory(BxZoneLabelCategory.CAT_GENERAL),
				featureVectorBuilder);
		return zoneClassifier;
	}
	
	public static void main(String[] args) throws ParseException {
		CrossvalidatingZoneClassificationEvaluator.main(args, new HMMZoneClassificationEvaluator());
	}

	@Override
	protected SampleSelector<BxZoneLabel> getSampleFilter() {
		return new SampleSelector<BxZoneLabel>() {
			@Override
			public List<TrainingElement<BxZoneLabel>> pickElements(
					List<TrainingElement<BxZoneLabel>> inputElements) {
				List<TrainingElement<BxZoneLabel>> ret = new ArrayList<TrainingElement<BxZoneLabel>>();
				ret.addAll(inputElements);
				return ret;
			}
		};
	}

	@Override
	protected ClassificationResults compareDocuments(BxDocument expected, BxDocument actual) {
		ClassificationResults ret = newResults();
		for(Integer idx=0; idx < actual.asZones().size(); ++idx) {
			ClassificationResults itemResults = compareItems(expected.asZones().get(idx), actual.asZones().get(idx));
			ret.add(itemResults);
		}
		return ret;
	}

	@Override
	protected void preprocessDocumentForEvaluation(BxDocument doc) {
		for(BxZone zone: doc.asZones())
			zone.setLabel(zone.getLabel().getGeneralLabel());
	}
	
}