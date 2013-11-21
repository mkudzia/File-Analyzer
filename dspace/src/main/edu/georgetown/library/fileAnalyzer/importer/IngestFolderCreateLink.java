package edu.georgetown.library.fileAnalyzer.importer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import edu.georgetown.library.fileAnalyzer.importer.IngestFolderCreate.IngestStatsItems;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;

/**
 * @author TBrady
 *
 */
public class IngestFolderCreateLink extends IngestFolderCreate {
	public static enum Generator implements StatsGenerator {
		INSTANCE;
		public Stats create(String key) {return new Stats(details, key);}
	}
	static StatsItemConfig details = StatsItemConfig.create(IngestStatsItems.class);
	
	public IngestFolderCreateLink(FTDriver dt) {
		super(dt);
	}

	public String toString() {
		return "Ingest: Create Ingest Folders - Link";
	}
	void prepFile(Stats stats, IngestStatsItems sienum, MODE mode, File selectedFile, String folder, String srcname, String destname)  {
		try {
			File dir = new File(currentIngestDir, folder);
			dir.mkdirs();

			File dest = new File(dir, new File(destname).getName());
			if (dest.exists()) {
				stats.setVal(sienum, FileStats.ALREADY_EXISTS);
				return;
			} 
			
			File source = getSourceFile(selectedFile, srcname);
			Files.createSymbolicLink(
				FileSystems.getDefault().getPath(dest.getAbsolutePath()),
				FileSystems.getDefault().getPath(source.getAbsolutePath())
			);
			stats.setVal(sienum, FileStats.LINKED_TO_INGEST);

			if (!dest.exists()) {
				stats.setVal(IngestStatsItems.Message, "File ["+dest+"] does not exist in ingest folder");
				stats.setVal(sienum, FileStats.ERROR);
			}
		} catch (CreateException e) {
			stats.setVal(IngestStatsItems.Message, e.getMessage());
			stats.setVal(sienum, FileStats.NOT_FOUND);
			stats.setVal(IngestStatsItems.Status, status.FAIL);
		} catch (SecurityException e) {
			stats.setVal(IngestStatsItems.Message, "Cannot create ingest dir");
			stats.setVal(sienum, FileStats.ERROR);
			stats.setVal(IngestStatsItems.Status, status.FAIL);
		} catch (FileNotFoundException e) {
			stats.setVal(IngestStatsItems.Message, "File ["+srcname+"] does not exist");
			stats.setVal(sienum, FileStats.ERROR);
			stats.setVal(IngestStatsItems.Status, status.FAIL);
		} catch (IOException e) {
			stats.setVal(IngestStatsItems.Message, "File ["+srcname+"] cannot be copied");
			stats.setVal(sienum, FileStats.ERROR);
			stats.setVal(IngestStatsItems.Status, status.FAIL);
		}
	}

}
