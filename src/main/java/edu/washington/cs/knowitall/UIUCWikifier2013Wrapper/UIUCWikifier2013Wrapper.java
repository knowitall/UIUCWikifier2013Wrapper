package edu.washington.cs.knowitall.UIUCWikifier2013Wrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.wikifier.common.GlobalParameters;
import edu.illinois.cs.cogcomp.wikifier.common.GlobalParameters.SettingManager;
import edu.illinois.cs.cogcomp.wikifier.common.GlobalPaths;
import edu.illinois.cs.cogcomp.wikifier.inference.InferenceEngine;
import edu.illinois.cs.cogcomp.wikifier.models.LinkingProblem;
import edu.illinois.cs.cogcomp.wikifier.models.Mention;
import edu.illinois.cs.cogcomp.wikifier.models.ReferenceInstance;


public class UIUCWikifier2013Wrapper {
	
	private static final String pathToDefaultNERConfigFile = "configs/NER.config";
	private static final String pathToDefaultNEConfigFile = "data/NESimdata/config.txt";
	private static final String pathToDefaultJWNLConfigFile = "configs/jwnl_properties.xml";
	
	
	public static void main(String[] args) throws Exception{
		String pathToWikifierFiles = args[0];
		String[] newConfigPaths = writeNewConfigFiles(pathToWikifierFiles);
//		for(String s : newConfigPaths){
//			System.out.println(s);
//		}
		GlobalParameters.loadSettings(getSettingManager(pathToWikifierFiles,newConfigPaths[0],newConfigPaths[1],newConfigPaths[2]));
        InferenceEngine inference = new InferenceEngine(false);
        
        
        System.out.println(Runtime.getRuntime().availableProcessors());
        System.out.println(GlobalParameters.THREAD_NUM);

//		String fileName = args[1];
//		String fileText = IOUtils.toString(new BufferedReader(new FileReader(new File(fileName))));
        
        File inputDir = new File(args[1]);
        File outputDir = new File(args[2]);
        for(File f : inputDir.listFiles()){
        	BufferedWriter bw = new BufferedWriter(new FileWriter(outputDir.getAbsolutePath()+"/"+f.getName()+".out"));
	    	try{
	    		String fileName = f.getName();
	    		String fileText = IOUtils.toString(new BufferedReader(new FileReader(new File(f.getAbsolutePath()))));
				TextAnnotation ta = GlobalParameters.curator.getTextAnnotation(fileText); 
				LinkingProblem problem=new LinkingProblem(fileName, ta, new ArrayList<ReferenceInstance>());
				inference.annotate(problem, null, false, false, 0);
				String output = getWikifierOutput(problem);
				bw.write(output);
		
	    	}
			catch(Exception e){
				bw.write("ERROR");
			}
	    	bw.close();
        }
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
	
	private static SettingManager getSettingManager(String pathToWikifierData, String nerConfigFile, String neConfigFile, String wordNetConfigFile){
		SettingManager sm = new SettingManager();
		sm.paths = setGlobalPaths(pathToWikifierData,nerConfigFile,neConfigFile,wordNetConfigFile);
		return sm;
	}

	public static String getWikifierOutput(LinkingProblem problem) {
	    StringBuilder res = new StringBuilder();

		for(Mention entity : problem.components){
			if(entity.topCandidate == null)
				continue;
			String escapedSurface = StringEscapeUtils.escapeXml(entity.surfaceForm.replace('\n', ' '));
			res.append(entity.charStart);
			res.append(":");
			res.append(entity.charStart + entity.charLength);
			res.append(" ");
			res.append(entity.topCandidate.titleName);
			res.append(" ");
			res.append(String.format("%3f",entity.linkerScore));
			res.append(" ");
			res.append(String.format("%3f",entity.topCandidate.rankerScore));
			res.append(" ");
			res.append(escapedSurface);
			res.append("\t");
		}
		return res.toString().trim();
	}
	
    public static String getTitleCategories(String title) {
        return StringUtils.join(GlobalParameters.getCategories(title),'\t');
    }

}
