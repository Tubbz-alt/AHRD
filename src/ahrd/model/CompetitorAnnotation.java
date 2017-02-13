package ahrd.model;

import static ahrd.model.TokenScoreCalculator.tokenize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CompetitorAnnotation {

	private String accession;
	private String description;
	private Set<String> evaluationTokens;
	private Double evaluationScore = 0.0;
	private Set<GOterm> goAnnotations = new HashSet<GOterm>();
	private Fscore simpleGoAnnotationScore;
	private Fscore ancestryGoAnnotationScore;
	private Fscore semSimGoAnnotationScore;

	public CompetitorAnnotation(String accession, String description) {
		super();
		setAccession(accession);
		setDescription(description);
		setEvaluationTokens(tokenize(getDescription(), new ArrayList<String>()));
	}
	
	public CompetitorAnnotation(String accession, String description, Set<GOterm> goAnnots) {
		this(accession, description);
		this.setGoAnnotations(goAnnots);
	}

	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<String> getEvaluationTokens() {
		return evaluationTokens;
	}

	public void setEvaluationTokens(Set<String> evaluationTokens) {
		this.evaluationTokens = evaluationTokens;
	}

	public Double getEvaluationScore() {
		return evaluationScore;
	}

	public void setEvaluationScore(Double evaluationScore) {
		this.evaluationScore = evaluationScore;
	}

	public Set<GOterm> getGoAnnotations() {
		return goAnnotations;
	}

	public void setGoAnnotations(Set<GOterm> goAnnotations) {
		this.goAnnotations = goAnnotations;
	}

	public Fscore getSimpleGoAnnotationScore() {
		return simpleGoAnnotationScore;
	}

	public void setSimpleGoAnnotationScore(Fscore simpleGoAnnotationScore) {
		this.simpleGoAnnotationScore = simpleGoAnnotationScore;
	}

	public Fscore getAncestryGoAnnotationScore() {
		return ancestryGoAnnotationScore;
	}

	public void setAncestryGoAnnotationScore(Fscore ancestryGoAnnotationScore) {
		this.ancestryGoAnnotationScore = ancestryGoAnnotationScore;
	}

	public Fscore getSemSimGoAnnotationScore() {
		return semSimGoAnnotationScore;
	}

	public void setSemSimGoAnnotationScore(Fscore semSimGoAnnotationScore) {
		this.semSimGoAnnotationScore = semSimGoAnnotationScore;
	}
	
}
