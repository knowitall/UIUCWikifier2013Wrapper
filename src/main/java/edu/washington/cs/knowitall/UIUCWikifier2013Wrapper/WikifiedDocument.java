package edu.washington.cs.knowitall.UIUCWikifier2013Wrapper;

import org.apache.commons.lang3.StringEscapeUtils;

import edu.illinois.cs.cogcomp.wikifier.models.LinkingProblem;
import edu.illinois.cs.cogcomp.wikifier.models.Mention;

public class WikifiedDocument {

	private String name;
	private String text;
	private String wikifiedOutput;
	private LinkingProblem problem;
	
	public WikifiedDocument(String name, String text, LinkingProblem problem){
		this.name = name;
		this.text = text;
		this.problem = problem;
		this.wikifiedOutput = getWikifierOutput(this.problem);
	}
	
	
	public String getWikifiedDocumentString(){
		return name +"\t"+wikifiedOutput;
	}
	
	public LinkingProblem getLinkingProblem(){
		return problem;
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
}
