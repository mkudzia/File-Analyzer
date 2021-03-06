package edu.georgetown.library.fileAnalyzer.filetest;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.transformer.impl.DefaultCompleter;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.File;
import java.io.IOException;

/**
 * Extract all metadata fields from a TIF or JPG using categorized tag defintions.
 * @author TBrady
 *
 */
class CreateBag extends DefaultFileTest { 
	public enum STAT {
		VALID,
		INVALID, 
		ERROR
	}
	
	private static enum BagStatsItems implements StatsItemEnum {
		Key(StatsItem.makeStringStatsItem("Source", 200)),
		Bag(StatsItem.makeStringStatsItem("Bag", 200)),
		Stat(StatsItem.makeEnumStatsItem(STAT.class, "Bag Status")),
		Count(StatsItem.makeIntStatsItem("Item Count")),
		Message(StatsItem.makeStringStatsItem("Message", 200)),
		;
		StatsItem si;
		BagStatsItems(StatsItem si) {this.si=si;}
		public StatsItem si() {return si;}
	}

	public static enum Generator implements StatsGenerator {
		INSTANCE;
		class BagStats extends Stats {
			public BagStats(String key) {
				super(details, key);
			}

		}
		public BagStats create(String key) {return new BagStats(key);}
	}
	public static StatsItemConfig details = StatsItemConfig.create(BagStatsItems.class);

	long counter = 1000000;
	public CreateBag(FTDriver dt) {
		super(dt);
	}

	public String toString() {
		return "Create Bag";
	}
	public String getKey(File f) {
		return f.getName();
	}
	
    public String getShortName(){return "Bag";}

    
	public Object fileTest(File f) {
		Stats s = getStats(f);
		File newBag = new File(f.getParentFile(), f.getName() + "_bag");
		//exists? 
		s.setVal(BagStatsItems.Bag, newBag.getAbsolutePath());
		BagFactory bf = new BagFactory();
		Bag bag = bf.createBag();
		bag.addFileToPayload(f);
		try {
			DefaultCompleter comp = new DefaultCompleter(bf);
			comp.setGenerateBagInfoTxt(true);
			comp.setUpdateBaggingDate(true);
			comp.setUpdateBagSize(true);
			comp.setUpdatePayloadOxum(true);
			comp.setGenerateTagManifest(true);
			bag = comp.complete(bag);
			bag.write(new FileSystemWriter(bf), newBag);
			bag.close();
			s.setVal(BagStatsItems.Stat, STAT.VALID);
			s.setVal(BagStatsItems.Count, bag.getPayload().size());
		} catch (IOException e) {
			s.setVal(BagStatsItems.Stat, STAT.ERROR);
			s.setVal(BagStatsItems.Message, e.getMessage());
		}
		return s.getVal(BagStatsItems.Count);
	}
    public Stats createStats(String key){ 
    	return Generator.INSTANCE.create(key);
    }
    public StatsItemConfig getStatsDetails() {
    	return details; 
    }

	public String getDescription() {
		return "This rule will bag the contents of the selected directory 'X' and create a new bag directory name 'X_bag'.\n" +
				"Some thought is still needed on how to iteratively bag up subfolders or files into independent bags.";
	}
	
	@Override public boolean isTestDirectory() {
		return true;
	}

	@Override public boolean isTestable(File f) {
		return f.equals(getRoot());
	}

	@Override public boolean isTestFiles() {
		return false; 
	}
	
	@Override public boolean processRoot() {
		return true;
	}
	
}
