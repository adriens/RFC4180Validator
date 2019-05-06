/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.adriens.rfc4180validator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 3004SAL
 */
public class RFC4180Validator {

    final static Logger logger = LoggerFactory.getLogger(RFC4180Validator.class);

    private String csvDirectory;
    private static Collection<File> csvFiles;

    public final static String[] CSV_SUFFIX = {"csv", "CSV"};
    public final static String ENV_KEY_TARGET_DIR = "CSV_DIR";
    public final static String DEFAULT_DIR = "input";

    public RFC4180Validator() {
        logger.info("Getting target csv directory <" + DEFAULT_DIR + "> to analyze...");
        this.csvDirectory = System.getProperty(ENV_KEY_TARGET_DIR, DEFAULT_DIR);
        logger.info("Got <" + csvDirectory + ">");

        File rootDir = new File(csvDirectory);
        logger.info("Feeding files based on authorized suffixes <" + CSV_SUFFIX.toString() + ">...");
        this.csvFiles = FileUtils.listFiles(rootDir, CSV_SUFFIX, true);
        logger.info("csv : <" + csvFiles.size() + "> files list populated.");

    }

    // Here is why : http://utf8everywhere.org/
    public boolean areCsvFilesUTF8Encoded() throws Exception {
        logger.info("About to scan files to check if encoding is UTF-8");
        boolean out = true;
        logger.info("Checking if csv files are UTF-8 encoded...");
        Iterator<File> filesIter = csvFiles.iterator();
        while (filesIter.hasNext()) {
            File lFile = filesIter.next();

            logger.info("Detecting character encoding for <" + lFile.getPath() + ">");
            //
            byte[] buf = new byte[4096];
            FileInputStream fis = new FileInputStream(lFile);
            UniversalDetector detector = new UniversalDetector();
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            //
            detector.dataEnd();

            //
            String encoding = detector.getDetectedCharset();
            if (encoding != null) {
                logger.info("Detected encoding for file <" + lFile.getPath() + "> : <" + encoding + ">");
                if (!encoding.equalsIgnoreCase("UTF-8")) {
                    logger.error("The file <" + lFile.getAbsolutePath() + "> is NOT UTF-8 encoded but is <" + encoding + "> encoded.");
                }
                Assert.assertEquals("File encoding of <" + lFile.getPath() + "> is not the expected one", "UTF-8", encoding);
            } else {
                logger.warn("No encoding could be detected on <" + lFile.getPath() + "> : do \"something\" if you can dude ;-p");
            }
            detector.reset();

        }
        return out;
    }

    public boolean checkFirstLineIsCsvLike() throws Exception {
        boolean out = true;
        logger.info("Checking that the first line looks like a csv...");
        Iterator<File> filesIter = this.csvFiles.iterator();
        while (filesIter.hasNext()) {
            File lFile = filesIter.next();
            String fileName = FilenameUtils.getName(lFile.getName());
            logger.debug("Found <" + lFile.getName() + ">");
            logger.debug("File path <" + lFile.getPath() + ">");
            Reader in = new FileReader(lFile.getPath());
            Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);

            // only get the first line
            CSVRecord firstRecord = records.iterator().next();
            if (firstRecord != null) {
                if (firstRecord.size() <= 1) {
                    logger.error("According to RFC4180, a single or no record has been detected. Check you are using commas.");
                    Assert.assertTrue("According to RFC4180, a single or no record has been detected. Check you are using commas.", firstRecord.size() > 1);
                    return false;
                } else if (firstRecord.size() > 1) {
                    logger.info("<" + firstRecord.size() + "> columns have been detected.");
                }

            } else {
                logger.warn("No line detected ! The file probably is empty !");
            }

            return false;
        }
        return out;
    }

    public boolean checkEveryRowHasTheSameNumberOfColumns() throws Exception {
        boolean out = true;
        Iterator<File> filesIter = this.csvFiles.iterator();
        while (filesIter.hasNext()) {
            File lFile = filesIter.next();
            String fileName = FilenameUtils.getName(lFile.getName());
            logger.debug("Found <" + lFile.getName() + ">");
            logger.debug("File path <" + lFile.getPath() + ">");

            boolean loadableCsv;
            String exMesg = null;
            Reader in = new FileReader(lFile.getPath());
            Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
            boolean compliant;
            int prevNbColumns = 0;
            int currentNbColumns = 0;

            int i = 0;
            for (CSVRecord record : records) {
                if (i == 0) {
                    //first row of the file
                    currentNbColumns = record.size();
                    prevNbColumns = record.size();
                } else {
                    prevNbColumns = currentNbColumns;
                    currentNbColumns = record.size();
                }
                Assert.assertEquals("All rows (see row <" + (i + 1) + "> of file <" + lFile.getPath() + "> : ) should have the same number of columns. ", prevNbColumns, currentNbColumns);
                Assert.assertTrue("There should be at leat one column in the csv", currentNbColumns > 1);
                i++;
            }

        }
        return out;
    }

    public boolean checkNoEmptyLines() throws Exception {
        boolean out = true;
        logger.info("About to look for empty lines in the files...");
        Iterator<File> filesIter = csvFiles.iterator();
        while (filesIter.hasNext()) {
            File lFile = filesIter.next();
            logger.info("Checking rows of <" + lFile + ">...");
            BufferedReader reader = new BufferedReader(new FileReader(lFile));
            int i = 1;
            for (String line; (line = reader.readLine()) != null;) {
                // process the line.
                if (line != null) {
                    if (line.length() < 1) {
                        logger.error("Empty line detected on <" + lFile.getAbsolutePath() + ":" + i + ">/ Please remove empty lines.");
                        return false;
                    }
                } else {
                    logger.warn("null line detected on <" + lFile.getAbsolutePath() + ">");
                    return false;
                }

                i++;
            }
            return out;
        }
        return out;
    }
    

    public static void main(String[] args) {
        RFC4180Validator validator = new RFC4180Validator();
        try {
            validator.areCsvFilesUTF8Encoded();
            validator.checkFirstLineIsCsvLike();
            //validator.checkEveryRowHasTheSameNumberOfColumns();
            validator.checkNoEmptyLines();
            logger.info("Successfull analysis completed.");
            System.exit(0);
        } catch (Exception ex) {
            logger.error("Could not successfully achieve analysis : " + ex.getMessage());
            System.exit(1);
        }
    }

}
