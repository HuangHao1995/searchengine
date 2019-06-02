package homework2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class Main {
	static ArrayList<String> Passages = new ArrayList<String>();// Be careful that the DID start
	// from D0001.
	static HashMap<String, String> invertedFile = new HashMap<String, String>();

	public static void main(String[] args) throws IOException {
		
		//Read in the file content. 
		File file = new File("collection-100.txt");
		BufferedReader bf = new BufferedReader(new FileReader(file));
		String content = "";
		StringBuilder tempMemory = new StringBuilder();
		while (!content.equals(null)) {
			content = bf.readLine();
			if (content == null) {
				Passages.add(tempMemory.toString());
				break;
			}
			if (content.equals("")) {
				Passages.add(tempMemory.toString());
				tempMemory = new StringBuilder();
			}
			tempMemory.append(content);
		}
		bf.close();
		
		//Filter. 
		for (int i = 0; i < Passages.size(); i++) {
			Passages.set(i, filtering(Passages.get(i)));
		}

		for (int k = 0; k < Passages.size(); k++) {
			int PassagesIndex = k + 1;
			ArrayList<String> already = new ArrayList<String>();
			// words that already appears in the inverted file, for this
			// Passage.
			String[] split = Passages.get(k).split(" ");
			for (int i = 0; i < split.length; i++) {
				if (already.contains(split[i])) {

				} else {
					already.add(split[i]);
					String newresult = "";
					if (invertedFile.containsKey(split[i])) {
						String oldresult = invertedFile.get(split[i]);
						newresult = oldresult + ";D" + PassagesIndex + ":" + getIndex(Passages.get(k), split[i]);
						invertedFile.put(split[i], newresult);
					} else {
						newresult = "D" + PassagesIndex + ":" + getIndex(Passages.get(k), split[i]);
						invertedFile.put(split[i], newresult);
					}
				}
			}
		}
		
		//Read in the query and print the result. 
		ArrayList<String> querys = new ArrayList<String>();
		try {
			FileReader fr = new FileReader("query-10.txt");
			BufferedReader bf2 = new BufferedReader(fr);
			String str;
			while ((str = bf2.readLine()) != null) {
				querys.add(str);
			}
			bf.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < querys.size(); i++) {
			System.out.println("<---------Query: " + querys.get(i) + "--------->\n");
			querys.set(i, querys.get(i).replaceAll("[\\p{Punct}\\pP]", " ").trim().toLowerCase());
			querys.set(i, filtering(querys.get(i)));
			queryResults(querys.get(i));
		}
	}

	//This method will filter the inputting texts and will return the filtered text. 
	public static String filtering(String input) {
		String output = "";
		input = input.replaceAll("/", " ").replaceAll("-", " ");
		String[] split = input.split(" ");
		for (int i = 0; i < split.length; i++) {
			if (split[i].equals("") || split[i].equals(null) || split[i].length() < 4) {
				split[i] = "";
			} else {
				split[i] = split[i].replaceAll(" ", "").replaceAll("[\\p{Punct}\\pP]", "");
				String[] wordSplit = split[i].split("");
				if (wordSplit[wordSplit.length - 1].equals("s")) {
					StringBuilder wordTemp = new StringBuilder();
					for (int j = 0; j < wordSplit.length - 1; j++) {
						wordTemp.append(wordSplit[j]);
					}
					split[i] = wordTemp.toString();
				}
			}

		}
		StringBuilder temp = new StringBuilder();
		for (int i = 0; i < split.length; i++) {
			if (!split[i].equals("")) {
				temp.append(split[i] + " ");
			}
		}
		output = temp.toString();
		output = output.replaceAll("[\\p{Punct}\\pP]", " ").trim().toLowerCase();
		return output;
	}

	//This method will return the index of a word in a String. 
	public static String getIndex(String text, String find) {
		StringBuilder findings = new StringBuilder();
		int returnValue = -1;
		while (true) {
			returnValue = text.indexOf(find, returnValue + 1);
			if (returnValue == -1) {
				break;
			}
			if (returnValue - 1 >= 0 && text.charAt(returnValue - 1) != ' ') {
			} else if (returnValue + find.length() <= (text.length() - 1)
					&& text.charAt(returnValue + find.length()) != ' ') {
			} else {
				String adding;
				if (findings.toString().equals("")) {
					adding = "" + returnValue;
				} else {
					adding = "," + returnValue;
				}

				findings.append(adding);
			}
		}
		return findings.toString();
	}

	//This method will print the query result of a query. 
	public static void queryResults(String query) {
		//StringBuilder result = new StringBuilder();
		String[] allWords = query.toLowerCase().split(" ");
		String[] words;
		int wordsLength = 0;
		for (int i = 0; i < allWords.length; i++) {
			if (!invertedFile.containsKey(allWords[i])) {
				System.out.println("The word " + allWords[i] + " does not appear. \n");
				allWords[i] = "";
			} else {
				wordsLength = wordsLength + 1;
			}
		}
		words = new String[wordsLength];
		int tempLength = 0;
		for (int i = 0; i < allWords.length; i++) {
			if (!allWords[i].equals("")) {
				words[tempLength++] = allWords[i];
			}
		}

		ArrayList<Integer> documentsID = new ArrayList<Integer>();
		for (int i = 0; i < words.length; i++) {
			String HashValues = invertedFile.get(words[i]);
			String[] split = HashValues.split(";");
			for (int j = 0; j < split.length; j++) {
				String[] split2 = split[j].split(":");
				String ID = split2[0].replace("D", "");
				int docNumber = Integer.parseInt(ID);
				if (!documentsID.contains(docNumber)) {
					documentsID.add(docNumber);
				}
			}
		}
		ArrayList<String> resultsAndScores = new ArrayList<String>();
		for (int i = 0; i < documentsID.size(); i++) {
			int docID = documentsID.get(i);
			String docFile = getSingleDocumentFile(docID);
			String PassageFile = Passages.get(docID - 1);
			String[] wordsSplit = PassageFile.split(" ");
			ArrayList<String> differentWords = new ArrayList<String>();
			for (int j = 0; j < wordsSplit.length; j++) {
				if (!differentWords.contains(wordsSplit[j])) {
					differentWords.add(wordsSplit[j]);
				}
			}
			int dwNum = invertedFile.size();
			double[] DVector = new double[dwNum];
			double[] QVector = new double[dwNum];
			for (int j = 0; j < DVector.length; j++) {
				DVector[j] = 0;
				QVector[j] = 0;
			}
			int indexForQ = 0;
			for (int j = 0; j < words.length; j++) {
				QVector[j] = 1;
				if (docFile.indexOf(words[j]) != -1) {
					double weight = getTermWeight(words[j], docFile);
					DVector[indexForQ++] = weight;
				} else {
				}
			}
			double[] realD = new double[dwNum];
			realD = calculateRealDocumentVector(docFile);
			double score = calculateScore(DVector, QVector, realD);
			String input = docID + ":" + score;
			resultsAndScores.add(input);
		}
		Comparator c = new Comparator<String>() {
			@Override
			public int compare(String a, String b) {
				String[] splita = a.split(":");
				double scorea = Double.parseDouble(splita[1]);
				String[] splitb = b.split(":");
				double scoreb = Double.parseDouble(splitb[1]);
				return scorea == scoreb ? 0 : (scorea > scoreb ? -1 : 1);
			}
		};
		try {
			Collections.sort(resultsAndScores, c);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (resultsAndScores.size() <= 3) {
			String numberValue = "";
			if (resultsAndScores.size() == 0) {
				System.out.print("No document contain the words. ");
			} else {
				if (resultsAndScores.size() == 1) {
					numberValue = "one";
				} else if (resultsAndScores.size() == 2) {
					numberValue = "two";
				}
				System.out.print("The " + numberValue + " most highest score documents' ID are: [ ");
			}
		} else {
			System.out.print("The three most highest score documents' ID are: [ ");
		}
		int[] threeHighestIndex = new int[3];
		for (int i = 0; i < threeHighestIndex.length; i++) {
			threeHighestIndex[i] = -1;
		}
		double[] threeHighestSimilarityScore = new double[3];
		int thiCount = 0;

		for (int i = 0; i < 3; i++) {
			if (resultsAndScores.size() <= i) {
				break;
			}
			System.out.print("D" + resultsAndScores.get(i).split(":")[0] + " ");
			String[] split = resultsAndScores.get(i).split(":");
			int tempIndex1 = Integer.parseInt(split[0]);
			double tempIndex2 = Double.parseDouble(split[1]);
			threeHighestIndex[thiCount] = tempIndex1;
			threeHighestSimilarityScore[thiCount] = tempIndex2;
			thiCount++;
		}
		if (resultsAndScores.size() > 0) {
			System.out.println("]\n");
		} else {
			System.out.println("\n");
		}
		for (int i = 0; i < 3; i++) {
			if (threeHighestIndex[i] == -1) {
				break;
			}
			String docFile = getSingleDocumentFile(threeHighestIndex[i]);
			int localIndex = i + 1;
			System.out.println("Top " + localIndex + " document: \n");
			System.out.print("DID: " + threeHighestIndex[i] + "\n");
			System.out.println(getFiveMaxWords(threeHighestIndex[i], docFile));
			System.out.println("\nNumber of unique keyword: " + getUniqueNum(threeHighestIndex[i]));
			double magnitude = 0;
			double[] localD = new double[invertedFile.size()];
			localD = calculateRealDocumentVector(docFile);
			for (int j = 0; j < localD.length; j++) {
				magnitude = magnitude + localD[j] * localD[j];
			}
			magnitude = Math.sqrt(magnitude);
			System.out.println("The magnitude of the document vector is: " + magnitude);
			System.out.println("The similarity score is: " + threeHighestSimilarityScore[i]);
			System.out.println();
		}
//		return result.toString();
	}
	
	//This method will return a Passage's words' posting lists after receiving the Passage ID.  
	public static String getSingleDocumentFile(int docIndex) {
		String input = Passages.get(docIndex - 1);
		StringBuilder tempListSave = new StringBuilder();
		ArrayList<String> already = new ArrayList<String>();
		// words that already appears in the inverted file, for this Passage.
		String[] split = input.split(" ");
		for (int i = 0; i < split.length; i++) {
			if (already.contains(split[i])) {

			} else {
				already.add(split[i]);
				tempListSave.append(split[i] + ":" + getIndex(input, split[i]) + ";");
			}
		}
		return tempListSave.toString();
		// calculate the document info with given document ID (remember to -1.
		// That is D1 for Passage[0]).
	}

	//This method will return the number of unique words of a Passage. 
	public static int getUniqueNum(int documentID) {
		int docID = documentID - 1;
		int unique_number = 0;
		String thePassage = Passages.get(docID);
		String[] wordsSplit = thePassage.split(" ");
		ArrayList<String> exists = new ArrayList<String>();
		for (int i = 0; i < wordsSplit.length; i++) {
			if (exists.contains(wordsSplit[i])) {

			} else {
				exists.add(wordsSplit[i]);
				if (invertedFile.containsKey(wordsSplit[i])) {
					if (invertedFile.get(wordsSplit[i]).split(";").length > 1) {

					} else {
						unique_number = unique_number + 1;
					}
				}
			}
		}
		return unique_number;
	}

	//This method will return the frequency of a word that appears most within a Passage. 
	public static int getMaxOccurrence(String input) {
		int retValue = 0;
		ArrayList<String> words = new ArrayList<String>();
		Comparator c = new Comparator<String>() {
			@Override
			public int compare(String a, String b) {
				String[] splita1 = a.split(":");
				String[] splita2 = splita1[1].split(",");
				int lengtha = splita2.length;
				String[] splitb1 = b.split(":");
				String[] splitb2 = splitb1[1].split(",");
				int lengthb = splitb2.length;
				return lengtha == lengthb ? 0 : (lengtha > lengthb ? -1 : 1);
			}
		};
		String[] inputSplit = input.split(";");
		for (int i = 0; i < inputSplit.length; i++) {
			words.add(inputSplit[i]);
		}

		try {
			Collections.sort(words, c);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String[] numSplit = words.get(0).split(":");
		int numLength = numSplit[1].split(",").length;
		retValue = numLength;

		return retValue;
	}

	//This method will return the five words that are highest weighted, given a document. 
	public static String getFiveMaxWords(int documentID, String docFile) {
		int docID = documentID - 1;
		ArrayList<String> words = new ArrayList<String>();
		String[] wordsSplit = Passages.get(docID).split(" ");
		for (int i = 0; i < wordsSplit.length; i++) {
			if (words.contains(wordsSplit[i])) {

			} else {
				words.add(wordsSplit[i]);
			}
		}
		ArrayList<String> results = new ArrayList<String>();
		for (int i = 0; i < words.size(); i++) {
			double weight = getTermWeight(words.get(i), docFile);
			results.add(words.get(i) + ":" + weight);
		}

		Comparator c = new Comparator<String>() {
			@Override
			public int compare(String a, String b) {
				String[] splita = a.split(":");
				double scorea = Double.parseDouble(splita[1]);
				String[] splitb = b.split(":");
				double scoreb = Double.parseDouble(splitb[1]);
				return scorea == scoreb ? 0 : (scorea > scoreb ? -1 : 1);
			}
		};
		try {
			Collections.sort(results, c);
		} catch (Exception e) {
			e.printStackTrace();
		}
		StringBuilder retValue = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			String[] split = results.get(i).split(":");
			String theAdding1 = split[0];
			String theAdding2 = invertedFile.get(theAdding1).replace(";", " | ");
			int numOfBlank = 15 - theAdding1.length();
			numOfBlank = Math.max(0, numOfBlank);
			retValue.append("\n" + theAdding1);
			for (int j = 0; j < numOfBlank; j++) {
				retValue.append(" ");
			}
			retValue.append("-> | " + theAdding2 + " |");
		}
		return retValue.toString();
	}

	//This method will return the term weight of a word in a document. 
	public static double getTermWeight(String word, String docFile) {
		double termWeight;
		double wordCount = 0;
		double totalWordNum = 0;
		String[] wordSplit = docFile.split(word + ":");
		if (wordSplit.length <= 1) {
			return 0;
		}

		String[] split = wordSplit[1].split(";");
		wordCount = split[0].split(",").length;

		wordSplit = docFile.split(";");
		for (int i = 0; i < wordSplit.length; i++) {
			String[] tempsplit = wordSplit[i].split(",");
			int length = tempsplit.length;
			totalWordNum = totalWordNum + length;
		}
		double tf = wordCount;
		double globalFileCount = 0;
		wordSplit = invertedFile.get(word).split(";");
		int length = wordSplit.length;
		globalFileCount = globalFileCount + length;
		double df = globalFileCount;
		double tfmax = getMaxOccurrence(docFile);
		double NDF = Passages.size() / df;
		double idf = Math.log((double) NDF) / Math.log((double) 2);
		termWeight = tf / tfmax * idf;
		return termWeight;
	}

	//This method will return the real document vector. 
	public static double[] calculateRealDocumentVector(String docFile) {
		double[] realD = new double[invertedFile.size()];
		int index = 0;
		Set<String> keys = invertedFile.keySet();
		Iterator<String> iter = keys.iterator();
		ArrayList<String> exist = new ArrayList<String>();
		while (iter.hasNext()) {
			String word = iter.next();
			if (exist.contains(word)) {

			} else {
				exist.add(word);
				if (docFile.indexOf(word) != -1) {
					if (docFile.indexOf(word) - 1 >= 0 && docFile.charAt(docFile.indexOf(word) - 1) != ';') {
						realD[index++] = 0;
					} else if ((docFile.indexOf(word) + word.length() <= (docFile.length() - 1))
							&& (docFile.charAt(docFile.indexOf(word) + word.length()) != ':')) {
						realD[index++] = 0;
					} else {
						try {
							double weight = getTermWeight(word, docFile);
							realD[index++] = weight;
						} catch (Exception e) {
							e.printStackTrace();
							realD[index++] = 0;
						}
					}
				} else {
					realD[index++] = 0;
				}
			}
		}
		return realD;
	}

	//This method will calculate the score given the D vector, Q vector and realD vector. 
	public static double calculateScore(double[] D, double[] Q, double[] realD) {
		double result = 0;
		double upper_part = 0;
		double lower_part = 1;
		for (int i = 0; i < D.length; i++) {
			upper_part = upper_part + D[i] * Q[i];
		}
		double sqD = 0;
		double sqQ = 0;
		for (int i = 0; i < D.length; i++) {
			sqD = sqD + realD[i] * realD[i];
			sqQ = sqQ + Q[i] * Q[i];
		}
		sqD = Math.sqrt(sqD);
		sqQ = Math.sqrt(sqQ);
		lower_part = sqD * sqQ;
		result = upper_part / lower_part;
		return result;
	}
}
