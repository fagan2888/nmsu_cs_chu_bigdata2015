package preprocess;

import java.io.*;
import java.util.*;

import org.apache.commons.collections.bidimap.DualTreeBidiMap;
import org.apache.lucene.analysis.*;
import org.apache.lucene.util.Version;
import util.CmdOption;
import util.Constant;
import util.Debugger;
import org.apache.commons.collections.BidiMap;
import util.Util;


/**
 * Class which represents a raw data.
 * @author convergence
 *
 */
public class DataParsed {

    private static Analyzer stemmer = new PorterAnalyzer();

    /**
     * Influenced word document count
     * w -> o -> freq
     */
    public Map<Integer,  Map<Integer, Map<Integer, Double>>> influencing_wtup = new HashMap<>();

    /**
     * influenced word timestamp document count
     */
    public Map<Integer, Map<Integer, Map<Integer, Double>>> influenced_wtu = new HashMap<>();

    public Map<Integer, List<Integer>> in_userGraph = new HashMap<>();

    public Set<Integer> influenced_userset = new HashSet<>();
    public Set<Integer> influencing_userset = new HashSet<>();

    /**
     * Initialize the sample's internal structure.
     * Read the input graph, and the documents for the nodes
     * If the passed graphFileName is empty, the program will set a manual set of data
     * otherwise, the sampler will read the file and get the internal structures
     * Create the internal data structure that would be used in the sampling process.
     *
     * These internal structures include
     * (1) topological structure of estTemporalInf
     * (2) Vocabulary
     */
    public void init(CmdOption cmdOption)
    {
//        read graph edges
        try {
            BufferedReader bw = new BufferedReader(new FileReader(new File(cmdOption.graphfile)));
            String line = bw.readLine();
            while (line!=null){
                String[] tokens = line.split("\\s");
                int influenced_id = Integer.parseInt(tokens[0]);
                int influencing_id = Integer.parseInt(tokens[1]);
                influenced_userset.add(influenced_id);
                influencing_userset.add(influencing_id);

                if (!in_userGraph.containsKey(influenced_id))
                    in_userGraph.put(influenced_id, new ArrayList<>());
                in_userGraph.get(influenced_id).add(influencing_id);

                line = bw.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        Set<Integer> vocabSet = new HashSet<>();
//        read uid timestamp word:count data
        try {
            BufferedReader bw = new BufferedReader(new FileReader(new File(cmdOption.datafile)));
            String line = bw.readLine();
            while (line!=null){
                String[] tokens = line.split("\\s");
                int uid = Integer.parseInt(tokens[0]);
                int timestamp = Integer.parseInt(tokens[1]);
                Constant.timestampSet.add(timestamp);

                for (int i=2; i<tokens.length; i++){
                    String[] items = tokens[i].split(":");
                    int wid = Integer.parseInt(items[0]);
                    vocabSet.add(wid);

                    int count = Integer.parseInt(items[1]);
                    if(influencing_userset.contains(uid))
                        Util.update3MapIncreamental(influencing_wtup, wid, timestamp, uid, count);
                    if(influenced_userset.contains(uid))
                        Util.update3MapIncreamental(influenced_wtu, wid, timestamp, uid, count);
                }

                line = bw.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        Constant.wordNum = vocabSet.size();
    }

    public static Analyzer getStemmer() {
        return stemmer;
    }

    public final static class PorterAnalyzer extends Analyzer implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 2L;

        public final TokenStream tokenStream(String fieldName, Reader reader) {

//            lucuene stop word list is too small
//            TokenStream ts = new StopFilter(Version.LUCENE_36,
//                    new PorterStemFilter(new LowerCaseTokenizer(Version.LUCENE_36, reader)),
//                    StopAnalyzer.ENGLISH_STOP_WORDS_SET);
//            Chuan manual collected stop word list
            TokenStream ts = new StopFilter(Version.LUCENE_36,
              new PorterStemFilter(new LowerCaseTokenizer(Version.LUCENE_36, reader)),
              readStopWord("./data/stopword_pure.txt"));

            // lower case + porter stemmer
//            TokenStream ts = new PorterStemFilter(new LowerCaseTokenizer(Version.LUCENE_36, reader));

            return ts;
        }

        /**
         * read stop word set from file
         * @param path
         * @return
         */
        public static Set<String> readStopWord(String path){
            Set<String> stopWordSet = new TreeSet<String>();
            try {
                BufferedReader br = new BufferedReader(new FileReader(path));

                String line  = br.readLine();
                while (line!=null){
                    stopWordSet.add(line.trim());
                    line = br.readLine();
                }

                br.close();
            }
            catch (FileNotFoundException e){
                e.printStackTrace();
            }
            catch (IOException e){

            }
            return stopWordSet;
        }
    }
}

