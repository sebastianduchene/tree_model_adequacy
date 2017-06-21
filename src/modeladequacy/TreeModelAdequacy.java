package modeladequacy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import beast.app.BeastMCMC;
import beast.app.treestat.statistics.TreeSummaryStatistic;
import beast.app.util.OutFile;
import beast.app.util.Utils;
import beast.app.util.XMLFile;
import beast.core.Description;
import beast.core.Distribution;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.MCMC;
import beast.core.Logger;
import beast.core.util.CompoundDistribution;
import beast.core.util.Log;
import beast.evolution.likelihood.GenericTreeLikelihood;
import beast.evolution.tree.Node;
import beast.evolution.tree.TreeInterface;
import beast.util.LogAnalyser;
import beast.util.Randomizer;
import modeladequacy.util.TreeModelAdequacyAnalyser;

@Description("Run a complete tree model adequacy analysis")
public class TreeModelAdequacy extends MCMC {
	public Input<Integer> treeCountInput = new Input<>("nrOfTrees", "the number of trees to use, default 100", 100);
	public Input<String> rootDirInput = new Input<>("rootdir", "root directory for storing individual tree files (default /tmp)", "/tmp");
	public Input<String> scriptInput = new Input<String>("value", "script for launching a job. " +
			"$(dir) is replaced by the directory associated with the particle " +
			"$(java.class.path) is replaced by a java class path used to launch this application " +
			"$(java.library.path) is replaced by a java library path used to launch this application " +
			"$(seed) is replaced by a random number seed that differs with every launch " +
			"$(host) is replaced by a host from the list of hosts");
	public Input<String> hostsInput = new Input<String>("hosts", "comma separated list of hosts. " +
			"If there are k hosts in the list, for particle i the term $(host) in the script will be replaced " +
			"by the (i modulo k) host in the list. " +
			"Note that whitespace is removed");
	public Input<Boolean> doNotRun = new Input<Boolean>("doNotRun", "Set up all files but do not run analysis if true. " +
			"This can be useful for setting up an analysis on a cluster", false);

	public Input<Integer> burnInPercentageInput = new Input<Integer>("burnInPercentage", "burn-In Percentage used for analysing log files", 50);
	public Input<XMLFile> masterInput = new Input<>("master", "master template used for generating XML", Validate.REQUIRED);
	public Input<List<TreeSummaryStatistic<?>>> statsInput = new Input<>("statistic", "set of statistics that need to be produced", new ArrayList<>());
	public Input<OutFile> outFileInput = new Input<>("out","Output file, where logs are stored. If not specified, use stdout"); 
	
	
	String m_sScript;
	String [] m_sHosts;
	CountDownLatch countDown;
	DecimalFormat formatter;


	@Override
	public void run() throws IOException, SAXException, ParserConfigurationException  {
		// sanity check: make sure master file exists:
		getMasterTemplat();
		
		// first, do an MCMC run, which gives us a log file 
		super.run();
		
		// next, set up simulations
		generateSimulations();
		generateRunFiles();
		if (!doNotRun.get()) {
			try {
				runSimulations();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// do the post-analysis
			postAnalysis();
		}
		
	}


	private void runSimulations() throws IOException, InterruptedException {
    	long startTime = System.currentTimeMillis();

    	int runCount = treeCountInput.get();
		for (int i = 0; i < runCount; i++) {
	    	if (BeastMCMC.m_nThreads > 1) {
	    		int nSteps = Math.min(BeastMCMC.m_nThreads, runCount - i);
	    		countDown = new CountDownLatch(nSteps);
	    		for (int j = 0; j < nSteps; j++) {
	    			new StepThread(i).start();
	    			i++;
	    		}
	    		countDown.await();	    		
	    		i--;
	    	} else {
				File stepDir = new File(getRunDir(i));
				if (!stepDir.exists()) {
					throw new IOException("Failed to find directory " + stepDir.getName());
				}
	        	String cmd = 
	        			(beast.app.util.Utils.isWindows()?
	        					stepDir.getAbsoluteFile() + "\\run.bat":
	        					stepDir.getAbsoluteFile() + "/run.sh");
	        	
	        	
					
					System.out.println(cmd);
					
					ProcessBuilder pb = new ProcessBuilder(cmd);
					pb.redirectErrorStream(true); // merge stdout and stderr
					Process p = pb.start();
					
					BufferedReader pout = new BufferedReader((new InputStreamReader(p.getInputStream())));
					String line;
					while ((line = pout.readLine()) != null) {
						System.out.println(line);
					}
					pout.close();
					p.waitFor();
	    	}
		}
    	long endTime = System.currentTimeMillis();

    	System.out.println("\n\nTotal wall time: " + (endTime-startTime)/1000 + " seconds\nDone");		
	}


	private void generateRunFiles() throws IOException {
		// set up directories with beast.xml files in each of them
    	int runCount = treeCountInput.get();
		String sFormat = "";
		for (int i = runCount; i > 0; i /= 10) {
			sFormat += "#";
		}
		formatter = new DecimalFormat(sFormat);

		PrintStream [] cmdFiles = new PrintStream[BeastMCMC.m_nThreads];
    	for (int i = 0; i < BeastMCMC.m_nThreads; i++) {
    		FileOutputStream outStream = (beast.app.util.Utils.isWindows()?
    					new FileOutputStream(rootDirInput.get() + "/run" + i +".bat"):
    					new FileOutputStream(rootDirInput.get() + "/run" + i +".sh"));
    		 cmdFiles[i] = new PrintStream(outStream);
    	}

		
		for (int i = 0; i < runCount; i++) {
			File stepDir = new File(getRunDir(i));
			if (!stepDir.exists() && !stepDir.mkdir()) {
				throw new IOException("Failed to make directory " + stepDir.getName());
			}
			stepDir.setWritable(true, false);
			
			String cmd = getCommand(stepDir.getAbsolutePath(), i);
        	FileOutputStream cmdFile = 
        			(beast.app.util.Utils.isWindows()?
        					new FileOutputStream(stepDir.getAbsoluteFile() + "/run.bat"):
        					new FileOutputStream(stepDir.getAbsoluteFile() + "/run.sh"));
        	PrintStream out2 = new PrintStream(cmdFile);
            out2.println(cmd);
			out2.close();

        	cmdFile = 
        			(beast.app.util.Utils.isWindows()?
        					new FileOutputStream(stepDir.getAbsoluteFile() + "/resume.bat"):
        					new FileOutputStream(stepDir.getAbsoluteFile() + "/resume.sh"));
        	cmd = cmd.replace("-overwrite", "-resume");
        	out2 = new PrintStream(cmdFile);
            out2.println(cmd);
			out2.close();

			if (i / BeastMCMC.m_nThreads == 0) {
				cmd = cmd.replace("-resume", "-overwrite");
			}
			cmdFiles[i % BeastMCMC.m_nThreads].println(cmd);
			File script = new File(stepDir.getAbsoluteFile() + 
					(beast.app.util.Utils.isWindows()? "/run.bat": "/run.sh"));
			script.setExecutable(true);
		}
		
    	for (int k = 0; k < BeastMCMC.m_nThreads; k++) {
    		cmdFiles[k].close();
    	}		
	}

	String getRunDir(int iParticle) {
		File f = new File(rootDirInput.get());
		return f.getAbsolutePath() + "/run" + formatter.format(iParticle);
	}


	private void generateSimulations() throws IOException {
		// grab info from inputs
		m_sScript = scriptInput.get();
		if (m_sScript == null) {
			m_sScript = "cd $(dir)\n" +
					"java -cp $(java.class.path) beast.app.beastapp.BeastMain $(resume/overwrite) -java -seed $(seed) beast.xml\n";
		}
		if (hostsInput.get() != null) {
			m_sHosts = hostsInput.get().split(",");
			// remove whitespace
			for (int i = 0; i < m_sHosts.length; i++) {
				m_sHosts[i] = m_sHosts[i].replaceAll("\\s", "");
			}
		}
		
		String masterTemplate = getMasterTemplat();
		LogAnalyser trace = getTrace();
		
		File rootdir = new File(rootDirInput.get());
		if (!rootdir.exists()) {
			if (!rootdir.mkdirs()) {
				throw new IllegalArgumentException("Could not create directory " + rootdir.getPath());
			}
		}
		
		
		// <!-- SEQ_DATA -->
        TreeInterface tree = getTree();
		StringBuilder tmp = new StringBuilder();
		for (int i = 0; i < tree.getLeafNodeCount(); i++) {
			tmp.append("<sequence taxon=\"" + tree.getNode(i).getID() + "\">???</sequence>\n");
		}
	    String seqData = tmp.toString();

	    // <!-- DATE_DATA -->
		tmp = new StringBuilder();
		for (int i = 0; i < tree.getLeafNodeCount(); i++) {
			Node node = tree.getNode(i);
			tmp.append(node.getID() + " = " + node.getHeight() + ",");
		}
		tmp.deleteCharAt(tmp.length() - 1);
		String dateData = tmp.toString();  
		
		
		for (int i = 0; i < treeCountInput.get(); i++) {
			String xml = merge(masterTemplate, trace, i, seqData, dateData);
			File stepDir = new File(rootdir.getPath() + "/run" + i);
			if (!stepDir.exists()) {
				if (!stepDir.mkdirs()) {
					throw new IllegalArgumentException("Could not create directory " + stepDir.getPath());
				}
			}
	        FileWriter outfile = new FileWriter(new File(stepDir.getPath() + "/beast.xml"));
	        outfile.write(xml);
	        outfile.close();
		}
		
	}

	// merges entry index from trace into master template
	private String merge(String masterTemplate, LogAnalyser trace, int index, String seqData, String dateData) {
		StringBuilder str = new StringBuilder();
		List<String> labels = trace.getLabels();
		
		masterTemplate = masterTemplate.replace("<!-- SEQ_DATA -->", seqData);
		masterTemplate = masterTemplate.replace("<!-- DATE_DATA -->", dateData);
		
		for (int i = 0; i < masterTemplate.length(); i++) {
			char c = masterTemplate.charAt(i);
			if (c == '$' && masterTemplate.charAt(i+1) == '(') {
				int end = masterTemplate.indexOf(')', i);
				String label = masterTemplate.substring(i+2, end - 1);
				Double [] log = null;
				for (int labelIndex = 0; labelIndex < labels.size(); labelIndex++) {
					String candidate = labels.get(labelIndex);
					if (candidate.startsWith(label)) {
						log = trace.getTrace(labelIndex + 1);						
						break;
					}
				}
				if (log == null) {
					throw new IllegalArgumentException("Could not find entry for " + label + " in tracelog");
				}
				str.append(log[index] + "");
				i = end;
			} else {
				str.append(c);
			}
		}
		return str.toString();
	}


	// get tracelog generated when runnning BEAST on this XML
	private LogAnalyser getTrace() throws IOException {
		for (Logger logger : loggers) {
			if (logger.getID() != null && logger.getID().equals("tracelog")) {
				String file = logger.fileNameInput.get();
				LogAnalyser trace = new LogAnalyser(file, burnInPercentageInput.get(), false, false);
				if (trace.getTrace(0).length < treeCountInput.get()) {
					throw new IllegalArgumentException("tracelog has only " + trace.getTrace(0).length + " entries, but " + treeCountInput.get() + "  are needed. "
							+ "Resume this run to produce more log entries.");
				}
				return trace;
			}
		}
		throw new IllegalArgumentException("Could not find tracelog");
	}


	private String getMasterTemplat() throws IOException {
		File file = masterInput.get();
		file = findMasterFile(file);
		if (!file.exists()) {
			file = masterInput.get();
			file = new File(file.getName());
			file = findMasterFile(file);
		}
		if (!file.exists()) {
			throw new IllegalArgumentException("Could not find master file associated with: " + masterInput.get().getPath());
		}		
		
		// read file from disk
        BufferedReader fin = new BufferedReader(new FileReader(file));
        StringBuffer buf = new StringBuffer();
        String str = null;
        while (fin.ready()) {
            str = fin.readLine();
            buf.append(str);
            buf.append('\n');
        }
        fin.close();
        return buf.toString();
	}


	private File findMasterFile(File file) {
		String FILESEP = "/";
		if (Utils.isWindows()) {
			FILESEP = "\\\\";
		}

		// determine location of file
		if (!file.exists()) {
			// try the master directory
			file = new File("master" + FILESEP + masterInput.get());
			if (!file.exists()) {
				// try  the master directory in the package directory
				String classpath = System.getProperty("java.class.path");
				String[] classpathEntries = classpath.split(File.pathSeparator);
				for (String pathEntry : classpathEntries) {
					if (new File(pathEntry).getName().toLowerCase().equals("TMA.addon.jar")) {
						Log.debug.println("Got it!");
						File parentFile = (new File(pathEntry)).getParentFile().getParentFile();
						String parent = parentFile.getPath();
						file = new File(parent + FILESEP + "master" + FILESEP + masterInput.get());
					}
				}
			}
			if (!file.exists()) {
				// try  the master directory in the user directory
				file = new File(System.getProperty("user.dir") + FILESEP + "master" + FILESEP+ masterInput.get());
			}
		}
		return file;
	}


	private String getCommand(String sStepDir, int iStep) {
		sStepDir = sStepDir.replace("\\", "\\\\");
		String sCommand = m_sScript.replaceAll("\\$\\(dir\\)", "\"" + sStepDir + "\"");
		//while (sCommand.matches("$(seed)")) {
			sCommand = sCommand.replaceAll("\\$\\(seed\\)", Math.abs(Randomizer.nextInt())+"");
		//}
		sCommand = sCommand.replaceAll("\\$\\(java.library.path\\)",  "\"" + sanitise(System.getProperty("java.library.path")) + "\"");
		sCommand = sCommand.replaceAll("\\$\\(java.class.path\\)", "\"" + sanitise(System.getProperty("java.class.path")) + "\"");
		if (m_sHosts != null) {
			sCommand = sCommand.replaceAll("\\$\\(host\\)", m_sHosts[iStep % m_sHosts.length]);
		}
		sCommand = sCommand.replaceAll("\\$\\(resume/overwrite\\)", "-overwrite");
		sCommand = sCommand.replaceAll("\\$\\(overwrite\\)", "-overwrite");
		return sCommand;
	}

	private String sanitise(String property) {
		// make absolute paths from relative paths
		String pathSeparator = System.getProperty("path.separator");
		String [] paths = property.split(pathSeparator);
		StringBuilder b = new StringBuilder();
		for (String path : paths) {
			File f = new File(path);
			b.append(f.getAbsolutePath());
			b.append(pathSeparator);
		}
		// chop off last pathSeparator
		property = b.substring(0, b.length() - 1);
		
		// sanitise for windows
		if (beast.app.util.Utils.isWindows()) {
			String cwd = System.getProperty("user.dir");
			cwd = cwd.replace("\\", "/");
			property = property.replaceAll(";\\.", ";" +  cwd + ".");
			property = property.replace("\\", "/");
		}
		return property;
	}

	
	private void postAnalysis() {
        TreeInterface tree = getTree();
        
		TreeModelAdequacyAnalyser analyser = new TreeModelAdequacyAnalyser();
		try {
			analyser.initByName("nrOfTrees", treeCountInput.get(),
					"rootdir", rootDirInput.get(),
					"tree", tree,
					"statistic", statsInput.get(),
					"out", outFileInput.get());
			analyser.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private TreeInterface getTree() {
		// get Tree from TreeLikelihood
		TreeInterface tree = null;
		Distribution posterior = posteriorInput.get();
		// expect compound distribution with likelihood and prior
		if (!(posterior instanceof CompoundDistribution)) {
			throw new IllegalArgumentException("Expected posterior being a CompoundDistribution");
		}
		CompoundDistribution d = (CompoundDistribution) posterior;
        for (Distribution pDist: d.pDistributions.get()) {
            String distID = pDist.getID().toLowerCase();
            if (distID.startsWith("likelihood")) {
            	CompoundDistribution d2 = (CompoundDistribution) pDist;
                for (Distribution pDist2: d2.pDistributions.get()) {
                    if (pDist2 instanceof GenericTreeLikelihood) {
                    	tree = ((GenericTreeLikelihood) pDist2).treeInput.get();
                    }
                }
            }
        }
		return tree;
	}

	class StepThread extends Thread {
		int stepNr;
		
		StepThread(int stepNr) {
			this.stepNr = stepNr;
		}
		
		@Override
		public void run() {
			try {
				System.err.println("Starting step " + stepNr);
				File stepDir = new File(getRunDir(stepNr));
				if (!stepDir.exists()) {
					throw new Exception("Failed to find directory " + stepDir.getName());
				}
	        	String cmd = 
        			(beast.app.util.Utils.isWindows()?
        					stepDir.getAbsoluteFile() + "/run.bat":
        					stepDir.getAbsoluteFile() + "/run.sh");
	        	
				ProcessBuilder pb = new ProcessBuilder(cmd);
				pb.redirectErrorStream(true); // merge stdout and stderr
				Process p = pb.start();
//	        	
//				Process p = Runtime.getRuntime().exec(cmd);
				BufferedReader pout = new BufferedReader((new InputStreamReader(p.getInputStream())));
				String line;
				while ((line = pout.readLine()) != null) {
					//System.out.println(line);
				}
				pout.close();
				
				p.waitFor();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.err.println("Finished step " + stepNr);
			countDown.countDown();
		}
	}

}
