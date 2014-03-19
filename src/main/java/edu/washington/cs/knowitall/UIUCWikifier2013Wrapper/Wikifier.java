package edu.washington.cs.knowitall.UIUCWikifier2013Wrapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;

import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.wikifier.common.GlobalParameters;
import edu.illinois.cs.cogcomp.wikifier.inference.InferenceEngine;
import edu.illinois.cs.cogcomp.wikifier.models.LinkingProblem;
import edu.illinois.cs.cogcomp.wikifier.models.ReferenceInstance;

public class Wikifier {
	
	private InferenceEngine inference;

	public Wikifier(String pathToWikifierResources, String configName) throws Exception{
		if(configName.equals(WikificationConfiguration.STAND_ALONE_GUROBI.toString())){
			 WikificationConfigurationSettings.standAloneGurobiSettings(pathToWikifierResources);
		}
		else if(configName.equals(WikificationConfiguration.STAND_ALONE_NO_INFERENCE.toString())){
			WikificationConfigurationSettings.standAloneNoInferenceSettings(pathToWikifierResources);
		}
		else if(configName.equals(WikificationConfiguration.FULL.toString())){
			WikificationConfigurationSettings.fullSettings(pathToWikifierResources);
		}
		
        inference = new InferenceEngine(false);
	}
	
	public WikifiedDocument wikify(String documentString, String documentName) throws Exception{
		TextAnnotation ta = GlobalParameters.curator.getTextAnnotation(documentString); 
		LinkingProblem problem=new LinkingProblem(documentName, ta, new ArrayList<ReferenceInstance>());
		inference.annotate(problem, null, false, false, 0);
		WikifiedDocument wd  = new WikifiedDocument(documentName,documentString,problem);
		return wd;
	}
	
	public WikifiedDocument wikifyFile(String filePath, String documentName) throws Exception{
		String docString = IOUtils.toString(new FileReader(new File(filePath)));
		return wikify(docString,documentName);		
	}
	
	
	/**
	 * args[0] is path to Wikifier Resource Dir
	 * args[1] is configuration setting that must match a WikificationConfiguration
	 * args[2] is the input directory, all files at the first level will be processed
	 * args[3] is the output directory
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{		
		String pathToWikifierDir = args[0];
		String confString = args[1];
		boolean validConfigString = false;
		for(WikificationConfiguration wc : WikificationConfiguration.values()){
			if(confString.equals(wc.toString())){
				validConfigString = true;
			}
		}
		if(!validConfigString){
			throw new IllegalArgumentException("Invalid Conf string.");
		}
		String pathToInputFiles = args[2];
		String pathToOutputFiles = args[3];
		
		Wikifier wikifier = new Wikifier(pathToWikifierDir,confString);
		
		File inputDir = new File(pathToInputFiles);
		for(File f : inputDir.listFiles()){
			WikifiedDocument wd = wikifier.wikifyFile(f.getAbsolutePath(),f.getName());
			File outputFile = new File(pathToOutputFiles+"/"+f.getName()+".out");
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			bw.write(wd.getWikifiedDocumentString());
			bw.close();
		}
	}
	
	
	public static enum WikificationConfiguration{
		STAND_ALONE_GUROBI,STAND_ALONE_NO_INFERENCE,FULL
	}

}
