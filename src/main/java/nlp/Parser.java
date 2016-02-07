package nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.washington.cs.knowitall.morpha.MorphaStemmer;
import opennlp.tools.chunker.Chunker;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.tokenize.Tokenizer;

/**
 * TODO Add some meaningful class description...
 */
public class Parser {

	public static final POSTagger POS_TAGGER = Factory.getPOSTagger();
	public static final Chunker CHUNKER = Factory.getChunker();
	private static final SentenceDetector SENTENCE_DETECTOR = Factory.getSentenceDetector();
	private static final Tokenizer TOKENIZER = Factory.getTokenizer();

	public static List<List<List<String>>> parse(String document) {
		document = Objects.requireNonNull(document).trim();
		if (document.isEmpty()) {
			throw new IllegalArgumentException("'document' is empty");
		}

		List<List<List<String>>> result = new ArrayList<>();
		for (String section : document.split("\\n\\n")) {
			for (String line : section.split("\\n")) {

				for (String sentence : SENTENCE_DETECTOR.sentDetect(line)) {
					String[] tokens = TOKENIZER.tokenize(sentence);
					String[] tags = POS_TAGGER.tag(tokens);
					String[] chunks = CHUNKER.chunk(tokens, tags);

//					String string = "";
					List<String> lemmas = new ArrayList<>();
					List<List<String>> parts = new ArrayList<>();
					for (int i = 0; i < chunks.length; ) {
						for (int start = i; i < chunks.length && (i == start || chunks[i].startsWith("I-")); i++) {
							String lemma = MorphaStemmer.stemToken(tokens[i], tags[i]);
							if (i == start) {
								if (chunks[i].contains("-")) {
									String type = chunks[i].split("-")[1];
									if (type.equalsIgnoreCase("NP") || type.equalsIgnoreCase("VP")) {
										lemmas.add(type);
//										string += type + ">  ";
									}
								}
							}
							if (lemma.matches("[\\w\\.:]+")) {
								if (chunks[i].endsWith("VP") && tags[i].startsWith("VB")) {
									lemmas.add(lemma);
									if (lemmas.size() > 2) {
										String first = lemmas.get(1);
										if (first.equalsIgnoreCase("be") || first.equalsIgnoreCase("have")) {
											lemmas.remove(1);
										}
									}
//									string += lemma + "  ";
								}
								if (chunks[i].endsWith("NP") && (
										tags[i].startsWith("CD") || tags[i].startsWith("JJ") ||
												tags[i].startsWith("VB") || tags[i].startsWith("NN"))) {
									lemmas.add(lemma);
//									string += lemma + "  ";
								}
								if (chunks[i].equals("O") && (tags[i].equals(".") || tags[i].equals(":"))) {
									if (!lemmas.isEmpty()) {
										if (lemmas.size() > 1) {
											parts.add(lemmas);
										}
									}
									lemmas = new ArrayList<>();
//									if (!string.trim().isEmpty()) {
//										if (!string.trim().endsWith(">")) {
//											System.out.println(string);
//										} else {
//											System.out.println();
//										}
//									}
//									string = "";
								}
							}
						}
						if (!lemmas.isEmpty()) {
							if (lemmas.size() > 1) {
								parts.add(lemmas);
							}
						}
						lemmas = new ArrayList<>();
//						if (!string.trim().isEmpty()) {
//							if (!string.trim().endsWith(">")) {
//								System.out.println(string);
//							} else {
//								System.out.println();
//							}
//						}
//						string = "";
					}
					if (!parts.isEmpty()) {
						result.add(parts);
					}
					parts = new ArrayList<>();
//					System.out.println();
				}
			}
		}
		return result;
	}

}
