/**
 * This program takes any input text and produces both a frequency table and the
 * corresponding Huffman code for the same
 * By Aditi Patel
 * Date : 04/28/2015
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;


public class HuffManCode {


	private class Forest {
		int root;
		int weight;

		public Forest(int root,int weight){
			this.root = root;
			this.weight = weight;
		}

		@Override
		public String toString() {
			return "Forest [root=" + root + ", weight=" + weight + "]";
		}


	}

	private class Alphabet {
		char symbol;
		int freq;
		int leaf; //maps to root

		public Alphabet (char symbol,int freq,int leaf){
			this.symbol = symbol;
			this.freq = freq;
			this.leaf = leaf;
		}
	}

	private class Tree{
		int leftChild;
		int rightChild;
		int parent;
		int root;
		String code;


		public Tree(int leftChild, int rightChild, int parent, int root) {
			this.leftChild = leftChild;
			this.rightChild = rightChild;
			this.parent = parent;
			this.root = root;
			this.code="";
		}
	}

	HashMap<Character,Integer> freqTableMap = new HashMap<Character,Integer>();
	PriorityQueue<Forest> forestPq = null;

	List<Tree> treeList = null;
	List<Alphabet> alphabetList = new ArrayList<Alphabet>();

/**
 * Method to generate dat file
 * @param fileName
 * @throws FileNotFoundException
 * @throws IOException
 */
	private void createDatFile(String fileName) throws FileNotFoundException, IOException{
		String huffmanIntOutput = "data/infile.dat";
		try(BufferedReader br = new BufferedReader(new FileReader(fileName))){
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(huffmanIntOutput))){
				int r;
				while ((r = br.read()) != -1) {
					char ch = (char) r;

					if(Character.isDigit(ch) || Character.isLetter(ch)){
						bw.write(ch);
					}
				}
			}
		}
	}

	/**
	 * Method to create the frequency table for each character & integer
	 * @param file
	 * @return
	 */
	private HashMap<Character,Integer> createFreqTableFromDatFile(String file){
		HashMap<Character,Integer> freqMap = new HashMap<>();
		try(BufferedReader br = new BufferedReader(new FileReader(file))){
			int r;
			while ((r = br.read()) != -1) {
				Character ch = new Character((char) r);
				if(freqMap.containsKey(ch)){
					int count = freqMap.get(ch);
					freqMap.put(ch, ++count);
				}else
					freqMap.put(ch, 1);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return freqMap;
	}
/**
 * Method to create a priority queue from frequency table
 * @param freqTableMap
 */
	private void createPriorityQueueFromFreqTable(HashMap<Character,Integer> freqTableMap){
		Forest forest = null;
		Alphabet alphabet = null;
		int root = 1;
		for(Character symbol : freqTableMap.keySet()){
			forest = new Forest(root,freqTableMap.get(symbol));
			alphabet = new Alphabet(symbol, freqTableMap.get(symbol), root);
			alphabetList.add(alphabet);
			forestPq.add(forest);
			root++;
		}
	}

	private void preFillTree(int size){
		treeList = new ArrayList<Tree>();
		Tree tree = null;
		for (int i=0;i<=size;i++){
			tree = new Tree(0,0,0,i);
			treeList.add(tree);
		}
	}

	private void initialize(HashMap<Character,Integer> freqTableMap){
		forestPq = new PriorityQueue<Forest>(freqTableMap.size(), new Comparator<Forest>(){

			@Override
			public int compare(Forest o1, Forest o2) {
				return new Integer(o1.weight).compareTo(new Integer(o2.weight));
			}

		});
		preFillTree(freqTableMap.size());
		createPriorityQueueFromFreqTable(freqTableMap);
	}

	private void huffmanAlgo(HashMap<Character,Integer> freqTableMap){
		Forest least = null, second = null, newRoot = null;
		int lastNode = freqTableMap.size();
		Tree tree = null;
		while(forestPq.size() > 1){
			least = forestPq.remove();
			second = forestPq.remove();
			lastNode++;
			tree = new Tree(0,0,0,lastNode);
			treeList.add(tree);
			tree.leftChild = least.root;
			tree.rightChild = second.root;
			treeList.get(least.root).parent = lastNode;
			treeList.get(second.root).parent = lastNode;
			newRoot = new Forest(lastNode,least.weight+second.weight);
			forestPq.add(newRoot);
		}
	}
/**
 * Method to create output dat file
 */
	private void createOutPutDatFile(){
		Forest result = forestPq.peek();
		Tree finalTree = treeList.get(result.root);
		traverseTree(finalTree);
	}
/**
 * Method for tree traversal
 * @param t
 */
	private void traverseTree(Tree t){
		Stack<Tree> treeStack = new Stack<Tree>();
		treeStack.add(t);
		int totalNoOfBits = 0;
		try(BufferedWriter bw = new BufferedWriter(new FileWriter("data/outfile.dat"))){
			while(!treeStack.isEmpty()){
				Tree tree = treeStack.pop();

				if(tree.rightChild != 0){
					Tree rightChild = treeList.get(tree.rightChild);
					rightChild.code = tree.code + "1";
					treeStack.add(rightChild);
				}
				if(tree.leftChild != 0){
					Tree leftChild = treeList.get(tree.leftChild);
					leftChild.code = tree.code + "0";
					treeStack.add(leftChild);
				}

				if(tree.leftChild == 0 && tree.rightChild == 0){
					Alphabet a = lookupSymbol(tree.root);
					bw.write("Symbol : "+a.symbol+" Frequency : "+a.freq+" code : "+tree.code);
					totalNoOfBits += (a.freq * (tree.code).length());
					bw.write("\n");
				}
			}//end of while
			bw.write("\nTotal length of the coded message in bits : "+totalNoOfBits);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private Alphabet lookupSymbol(int leaf){
		for(Alphabet a : alphabetList){
			if(a.leaf == leaf)
				return a;
		}
		return null;
	}

	public static void main(String args[]){
		HuffManCode hf = new HuffManCode();

		try {
			//create input dat file from the source text file, excluding all special characters
			hf.createDatFile("data/huffman_input.txt");
			//create frequency table from the input dat file
			HashMap<Character,Integer> freqTableMap = hf.createFreqTableFromDatFile("data/infile.dat");
			//Initialize all data structures
			hf.initialize(freqTableMap);
			//run the huffman algorithm to encode each character/digit from the input dat file
			hf.huffmanAlgo(freqTableMap);
			//create the output dat file
			hf.createOutPutDatFile();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
