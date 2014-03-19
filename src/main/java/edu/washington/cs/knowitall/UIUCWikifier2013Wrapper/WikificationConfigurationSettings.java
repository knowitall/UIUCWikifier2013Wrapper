package edu.washington.cs.knowitall.UIUCWikifier2013Wrapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import edu.illinois.cs.cogcomp.wikifier.common.GlobalParameters;
import edu.illinois.cs.cogcomp.wikifier.common.GlobalPaths;
import edu.illinois.cs.cogcomp.wikifier.common.GlobalParameters.SettingManager;
import edu.illinois.cs.cogcomp.wikifier.common.ParameterPresets;
import edu.illinois.cs.cogcomp.wikifier.common.WikifierParameters;

public class WikificationConfigurationSettings {
	
	private static final String pathToDefaultNERConfigFile = "configs/NER.config";
	private static final String pathToDefaultNEConfigFile = "data/NESimdata/config.txt";
	private static final String pathToDefaultJWNLConfigFile = "configs/jwnl_properties.xml";
	
	private static void setGlobalParameterPaths(String pathToWikifierResources) throws FileNotFoundException, IOException{
		String[]  newConfigPaths = writeNewConfigFiles(pathToWikifierResources);
		GlobalParameters.paths = setGlobalPaths(pathToWikifierResources,newConfigPaths[0],newConfigPaths[1],newConfigPaths[2]);
	}
	
	public static void standAloneGurobiSettings(String pathToWikifierResources) throws Exception{
		setGlobalParameters(pathToWikifierResources,ParameterPresets.STAND_ALONE_GUROBI);
	}
	
	public static void fullSettings(String pathToWikifierResources) throws Exception{
		setGlobalParameters(pathToWikifierResources,ParameterPresets.FULL);
	}

	public static void standAloneNoInferenceSettings(String pathToWikifierResources) throws Exception {
		setGlobalParameters(pathToWikifierResources,ParameterPresets.STAND_ALONE_NO_INFERENCE);
	}
	
	private static void setGlobalParameters(String pathToWikifierResources, ParameterPresets pp) throws Exception{
		//set GlobalParameters.params to STAND_ALONE_NO_INFERENCE via ParameterPresets
		WikifierParameters wp = WikifierParameters.defaultInstance();
		wp.preset = pp;
		wp.preset.apply();
		//override GlobalParameters.paths to custom paths to temporary files so it can
		//run in a different directory
		setGlobalParameterPaths(pathToWikifierResources);
		//create Setting Manager such that all current globalParameters settings are preserved
		SettingManager sm = new SettingManager(new Object());
		GlobalParameters.loadSettings(sm);
	}
	
	private static GlobalPaths setGlobalPaths(String prefix, String nerConfigFile, String neConfigFile, String wordNetConfigFile) {
		GlobalPaths gp = new GlobalPaths();
        gp.compressedRedirects = prefix+"/"+"data/WikiData/Redirects/2013-05-28.redirect";
        gp.protobufferAccessDir = prefix+"/"+"data/Lucene4Index/";
        gp.curatorCache = prefix+"/"+"data/TextAnnotationCache/";
        gp.wikiRelationIndexDir = prefix+"/"+"data/WikiData/Index/WikiRelation/";
        gp.models = prefix+"/"+"data/Models/TitleMatchPlusLexicalPlusCoherence/";
        gp.titleStringIndex = prefix+"/"+"data/WikiData/Index/TitleAndRedirects/";
        gp.wordnetConfig = wordNetConfigFile;
        gp.stopwords = prefix+"/"+"data/OtherData/stopwords_big";
        gp.wordNetDictionaryPath = prefix+"/"+"data/WordNet/";
        gp.nerConfig = nerConfigFile;
        gp.wikiSummary = null;
        gp.neSimPath = neConfigFile;
        
        return gp;
	}
	
	/**
	 * Read in NER and NE default config files, write out new config files
	 * with appropriate paths then save config file and return its location
	 * @param pathToWikifierFiles
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private static String[] writeNewConfigFiles(String pathToWikifierFiles) throws FileNotFoundException, IOException {
		String[] configFiles = new String[3];
		
		//read in old ner config parameters and change
		List<String> nerConfigLines = IOUtils.readLines(new FileInputStream(new File(pathToWikifierFiles+ "/" +pathToDefaultNERConfigFile)));
		List<String> newNERConfigLines = new ArrayList<String>();
		for(String l : nerConfigLines){
			String[] values = l.split("\\t+");
			StringBuilder newLine = new StringBuilder();
			for(String value: values){
				if(value.contains("/")){
					newLine.append(pathToWikifierFiles+"/"+value);
					newLine.append("\t");
				}
				else{
					newLine.append(value);
					newLine.append("\t");
				}
			}
			newNERConfigLines.add(newLine.toString().trim());
		}
		
		
		//write out new config parameters
		File newNERConfigFile = File.createTempFile("NER.config", ".tmp");
		newNERConfigFile.deleteOnExit();
		configFiles[0] = newNERConfigFile.getAbsolutePath();
		BufferedWriter nerWriter = new BufferedWriter(new FileWriter(newNERConfigFile));
		for(String l : newNERConfigLines){
			System.out.println(l);
			nerWriter.write(l+"\n");
		}
		nerWriter.close();
		
		
		
		//read in old ne config parameters and change
		List<String> neConfigLines = IOUtils.readLines(new FileInputStream(new File(pathToWikifierFiles + "/" + pathToDefaultNEConfigFile)));
		List<String> newNEConfigLines = new ArrayList<String>();
		for(String l : neConfigLines){
			String[] values = l.split("=");
			String value = values[1];
			if(value.contains("/")){
				String[] paths = value.split("\\s+");
				StringBuilder newValue = new StringBuilder();
				for(String path : paths){
					newValue.append(pathToWikifierFiles+"/"+path);
					newValue.append(" ");
				}
				StringBuilder newLine = new StringBuilder();
				newLine.append(values[0]);
				newLine.append("=");
				newLine.append(newValue.toString().trim());
				newNEConfigLines.add(newLine.toString());
			}
			else{
				newNEConfigLines.add(l);
			}
		}
		//write out new config parameters
		File newNEConfigFile = File.createTempFile("config.txt", ".tmp");
		newNEConfigFile.deleteOnExit();
		configFiles[1] = newNEConfigFile.getAbsolutePath();
		BufferedWriter neWriter = new BufferedWriter(new FileWriter(newNEConfigFile));
		for(String l : newNEConfigLines){
			neWriter.write(l+"\n");
		}
		neWriter.close();
		
		
		//read in old wordnet properties
		List<String> wordNetPropertiesLines = IOUtils.readLines(new FileInputStream(new File(pathToWikifierFiles + "/" + pathToDefaultJWNLConfigFile)));
		List<String> newWordNetPropertiesLines = new ArrayList<String>();
		String replacementString = pathToWikifierFiles +"/data/WordNet/";
		String stringToReplace = "data/WordNet/";
		for(String l : wordNetPropertiesLines){
			if(l.contains("dictionary_path")){
				newWordNetPropertiesLines.add(l.replace(stringToReplace, replacementString));
			}
			else{
				newWordNetPropertiesLines.add(l);
			}
		}
		File newWNConfigFile = File.createTempFile("jwnl_properties.xml",".tmp");
		newWNConfigFile.deleteOnExit();
		configFiles[2] = newWNConfigFile.getAbsolutePath();
		BufferedWriter wnWriter = new BufferedWriter(new FileWriter(newWNConfigFile));
		for(String l : newWordNetPropertiesLines){
			wnWriter.write(l+"\n");
		}
		wnWriter.close();
		
		return configFiles;
	}

}
