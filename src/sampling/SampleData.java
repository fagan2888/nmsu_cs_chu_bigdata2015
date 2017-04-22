package sampling;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.*;

import com.google.common.util.concurrent.AtomicDouble;
import util.CmdOption;
import util.Constant;
import util.Debugger;
import util.Util;
import org.apache.commons.collections.BidiMap;
import preprocess.DataParsed;

/**
 *
 * Influencing objects: that have the potential to estTemporalInf others
 * Influenced objects: that have the potential to be influenced by others
 *
 * Example: o1->o2, o2->o3, o2->o4
 * Influencing objects: o1,o2
 * Influenced objects: o2,o3,o4
 *
 * @author Huiping Cao
 */
public class SampleData {
    public CmdOption cmdOption;
    public Date firstDate;

    public int chainId = -1;


    public Map<Integer, Map<Integer, Map<Integer, Double>>> in_influencing_wtup;
    public Map<Integer, Map<Integer, Map<Integer, Double>>> in_influenced_wtu;

    public Set<Integer> influenced_userset;
    public Set<Integer> influencing_userset;

    public Set<Integer> testSet; //citing:influenced test set

    public Map<Integer, List<Integer>> in_userGraph;


    /**
     * influencing obj id -> obj sample chain
     */
    public Map<Integer, List<SampleElementInfluencing>> influencingObjChain_utz;
    int influencingCount = 0;
    /**
     * influenced obj id -> obj sample chain
     */
    public Map<Integer, List<SampleElementInfluenced>> influencedObjChain_uwtzbup;
    int influencedCount = 0;

    //            multi thread sampling
    public ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();
    //

//    influenced count
    /**
     * N_u
     */
    public Map<Integer, Double> N_u_influenced;
    /**
     * N_{u,b}
     */
    public Map<Integer, Map<Integer, Double>> N_ub_influenced;
    /**
     * N_{u,z,b=0}
     */
    public Map<Integer, Map<Integer, Double>> N_uz_innov_influenced;
    /**
     * N_{u,z,t,b=0}
     */
    public Map<Integer, Map<Integer, Map<Integer, Double>>> N_uzt_innov_influenced;
    /**
     * N_{u',b=1}
     */
    public Map<Integer, Double> N_up_inher_influenced;
    /**
     * N_{u',z',b=1}
     */
    public Map<Integer, Map<Integer, Double>> N_upz_inher_influenced;
    /**
     * N_{u,u',b=1}
     */
    public Map<Integer, Map<Integer, Double>> N_uup_inher_influenced;
    /**
     * N_{u,u',z,b=1}
     */
    public Map<Integer, Map<Integer, Map<Integer, Double>>> N_uupz_inher_influenced;
    /**
     * N_{u,u',z,t,b=1}
     */
    public Map<Integer, Map<Integer,
      Map<Integer, Map<Integer, Double>>>> N_uupzt_inher_influenced;

    //    influencing count
    /**
     * N_{u'}
     */
    public Map<Integer, Double> N_up_influencing;
    /**
     * N_{u',z'}
     */
    public Map<Integer, Map<Integer, Double>> N_upz_influencing;
    /**
     * N_{u',z',t}
     */
    public Map<Integer, Map<Integer,  Map<Integer, Double>>> N_upzt_influencing;


    //    shared count
    /**
     * N_{z,w} + N_{z',w'}
     */
    public Map<Integer, Map<Integer, Double>> N_zw_all;
    /**
     * N_z + N_{z'}
     */
    public Map<Integer, Double>  N_z_all;

    /**
     * word text <-> word index bi-direction map
     */
    public BidiMap vocabularyMap;

    public Map<Integer, Set<Integer>> N_utimeset = new TreeMap<Integer, Set<Integer>>();
    public Map<Integer, Set<Integer>> N_uptimeset = new TreeMap<Integer, Set<Integer>>();

    /**
     * log likelihood of this model
     */
    public double llh = 0;
    /**
     * perplexity of this model
     */
    public double perplexity;

    Map<Integer, InfluencingGibbsSampler> influencingThreadMap = new HashMap<Integer, InfluencingGibbsSampler>();
    Map<Integer, InfluencedGibbsSampler> influencedThreadMap = new HashMap<Integer, InfluencedGibbsSampler>();

    //TODO record time usage

    public SampleData(final DataParsed parsedData,
                      CmdOption _option, Set<Integer> testSet, Sampler sampler)
    {
        this.cmdOption = _option;

        this.in_influencing_wtup = parsedData.influencing_wtup;
        this.in_influenced_wtu = parsedData.influenced_wtu;

        this.influenced_userset = parsedData.influenced_userset;
        this.influencing_userset = parsedData.influencing_userset;

        this.in_userGraph = parsedData.in_userGraph;
        this.testSet = testSet;
        this.chainId = sampler.runnableChainId;

        System.out.println(Debugger.getCallerPosition()+" initial chain id "+this.chainId);

        init();
    }

    public SampleData(SampleDataSer sampleDataSer){
        this.cmdOption = sampleDataSer.cmdOption;
        this.firstDate = sampleDataSer.firstDate;
        this.N_u_influenced = sampleDataSer.N_u_influenced;
        this.N_ub_influenced = sampleDataSer.N_ub_influenced;
        this.N_uz_innov_influenced = sampleDataSer.N_uz_innov_influenced;
        this.N_uzt_innov_influenced = sampleDataSer.N_uzt_innov_influenced;
        this.N_up_inher_influenced = sampleDataSer.N_up_inher_influenced;
        this.N_upz_inher_influenced = sampleDataSer.N_upz_inher_influenced;
        this.N_uup_inher_influenced = sampleDataSer.N_uup_inher_influenced;
        this.N_uupz_inher_influenced = sampleDataSer.N_uupz_inher_influenced;
        this.N_uupzt_inher_influenced = sampleDataSer.N_uupzt_inher_influenced;
        this.N_up_influencing = sampleDataSer.N_up_influencing;
        this.N_upz_influencing = sampleDataSer.N_upz_influencing;
        this.N_upzt_influencing = sampleDataSer.N_upzt_influencing;
        this.N_zw_all = sampleDataSer.N_zw_all;
        this.N_z_all = sampleDataSer.N_z_all;
        this.vocabularyMap = sampleDataSer.vocabularyMap;
        this.N_utimeset = sampleDataSer.N_utimeset;
        this.N_uptimeset = sampleDataSer.N_uptimeset;
        this.llh = sampleDataSer.llh;
        this.perplexity = sampleDataSer.perplexity;
        this.in_userGraph = sampleDataSer.in_userGraph;
    }

    /**
     * Initialize the data structure for Gibbs sampling
     */
    private void init()
    {
        ////////////////////////////////////////
        //1. w count for influencing and influenced documents

        this.influencingObjChain_utz = new HashMap<>();
        this.influencedObjChain_uwtzbup = new HashMap<>();

        /////////////////////////////////////////////////////

        //3. Initialize sampling counts
        //Sampling counts
//        if (cmdOption.concurrent.equals("y")){ //concurrent sampling
//            N_u_influenced = new ConcurrentHashMap<>();
//            N_ub_influenced = new ConcurrentSkipListMap<>();
//            N_uz_innov_influenced = new ConcurrentHashMap<>();
//            N_uzt_innov_influenced = new ConcurrentHashMap<>();
//            N_up_inher_influenced = new ConcurrentHashMap<>();
//            N_upz_inher_influenced = new ConcurrentHashMap<>();
//            N_uup_inher_influenced = new ConcurrentHashMap<>();
//            N_uupz_inher_influenced = new ConcurrentHashMap<>();
//            N_uupzt_inher_influenced = new ConcurrentHashMap<>();
//
//            N_up_influencing = new ConcurrentHashMap<>();
//            N_upz_influencing = new ConcurrentHashMap<>();
//            N_upzt_influencing = new ConcurrentHashMap<>();
//
//            N_zw_all = new ConcurrentHashMap<>();
//            N_z_all = new ConcurrentHashMap<>();
//        }
//        else if (cmdOption.concurrent.equals("n")){//serial sampling
            N_u_influenced = new HashMap<>();
            N_ub_influenced = new TreeMap<>();
            N_uz_innov_influenced = new HashMap<>();
            N_uzt_innov_influenced = new HashMap<>();
            N_up_inher_influenced = new HashMap<>();
            N_upz_inher_influenced = new HashMap<>();
            N_uup_inher_influenced = new HashMap<>();
            N_uupz_inher_influenced = new HashMap<>();
            N_uupzt_inher_influenced = new HashMap<>();

            N_up_influencing = new HashMap<>();
            N_upz_influencing = new HashMap<>();
            N_upzt_influencing = new HashMap<>();

            N_zw_all = new HashMap<>();
            N_z_all = new HashMap<>();
//        }

//      enable thread contention monitoring
        if ( !tmxb.isThreadContentionMonitoringEnabled() )
            tmxb.setThreadContentionMonitoringEnabled(true);
        if (!tmxb.isCurrentThreadCpuTimeSupported())
            tmxb.setThreadCpuTimeEnabled(true);

    }

    /**
     * For object "uid", get the number of objects that influece it.
     * @param uid
     * @return he number of objects that influece the given object "uid".
     */
    public int getUprimeNumber(int uid) {
        return in_userGraph.get(uid).size();
    }


    /**
     * Draw the initial sample for influencing and influenced objects
     * @param trainOrTest true: training; false: test
     */
    public void drawInitialSample(boolean trainOrTest)
    {
        drawInitialSampleInfluencing(trainOrTest);

        System.out.println(Debugger.getCallerPosition() + " after initial influencing sampling, checking count consistent.");
        this.checkSampleCountConsistency();
        System.out.println(Debugger.getCallerPosition()+" after initial influencing sampling, count is consistent.");

//        System.exit(0);

        drawIntitialSampleInfluenced(trainOrTest);

        System.out.println(Debugger.getCallerPosition() + " after initial influenced sampling, checking count consistent.");
        this.checkSampleCountConsistency();
        System.out.println(Debugger.getCallerPosition()+" after initial influenced sampling, count is consistent.");

//        System.exit(0);

//        this.in_influencing_wtup.clear();
//        this.in_influencing_wtup = null;
        System.gc();

        this.rebuild_N_zw_N_z();
        System.out.println(Debugger.getCallerPosition()+" after initial and free resource memUsed="+
          +Debugger.getMemoryUsed());
    }

    /**
     * Draw initial samples for influencing objects
     * @param trainOrTest true: training; false: test
     */
    private void drawInitialSampleInfluencing(boolean trainOrTest)
    {
        influencing_userset = new HashSet<>();
        //map loop initial
        for (Map.Entry<Integer, Map<Integer, Map<Integer, Double>>> w2tup_entry : in_influencing_wtup.entrySet()){
            int wid = w2tup_entry.getKey();
            for (Map.Entry<Integer, Map<Integer, Double>> t2up_entry : w2tup_entry.getValue().entrySet()) {
                int timeStamp = t2up_entry.getKey();

                for (Map.Entry<Integer, Double> up2count_entry : t2up_entry.getValue().entrySet()) {
                    //Get the number of tokens in object "opIndex"
                    int upIndex = up2count_entry.getKey();
                    final double tokenCount = up2count_entry.getValue();

                    if (testSet.contains(upIndex) && trainOrTest)//in training step and opIndex in testSet. Continue
                        continue;
                    if (!testSet.contains(upIndex) && !trainOrTest)//in test step and opIndex is not in testSet.  Continue
                        continue;

                    if (!N_uptimeset.containsKey(upIndex))
                        N_uptimeset.put(upIndex, new HashSet<Integer>());

                    Set<Integer> uptimeSet = N_uptimeset.get(upIndex);
                    uptimeSet.add(timeStamp);

                    //w occurs "tokenCount" times in the profile of object "opIndex"
                    for (int occ = 0; occ < tokenCount; occ++) {

                        int newZ = Util.initialLatentState(cmdOption.znum);

                        //1.add the sample
                        SampleElementInfluencing e = new SampleElementInfluencing(upIndex, timeStamp, wid, newZ);
                        List<SampleElementInfluencing> objChain = this.influencingObjChain_utz.get(upIndex);
                        if(objChain==null){
                            objChain = new ArrayList<>();
                            this.influencingObjChain_utz.put(upIndex, objChain);
                        }
                        objChain.add(e);

                        //2. update sample count
                        updCountInfluencing_upzwt(upIndex, newZ, wid, timeStamp, +1);
                        influencingCount++;
                    }
                }
            }
        }

        System.out.println(Debugger.getCallerPosition()+ " influencing object count="+ influencingObjChain_utz.size()+
          " influencing sample chain size="+influencingCount+" memory usage="+Debugger.getMemoryUsed());
    }

    /**
     * draw initial sample for influenced documents.
     * @param trainOrTest true: training; false: test
     */
    private void drawIntitialSampleInfluenced(boolean trainOrTest)
    {
        influenced_userset = new HashSet<Integer>();
        //map loop initial
        for (Map.Entry<Integer, Map<Integer, Map<Integer, Double>>> w2tu_entry : in_influenced_wtu.entrySet()){//non zero w
            int wid = w2tu_entry.getKey();
            for (Map.Entry<Integer, Map<Integer, Double>> t2u_entry : w2tu_entry.getValue().entrySet()) {// non zero ta
                int timeStamp = t2u_entry.getKey();

                for (Map.Entry<Integer, Double> u2count_entry : t2u_entry.getValue().entrySet()) {
                    int uid = u2count_entry.getKey();

                    if (testSet.contains(uid) && trainOrTest)//in training step and obj in testSet. Continue
                        continue;
                    if (!testSet.contains(uid) && !trainOrTest)//in test step and obj is not in testSet.  Continue
                        continue;

                    if (!N_utimeset.containsKey(uid))
                        N_utimeset.put(uid, new HashSet<Integer>());

                    Set<Integer> utimeSet = N_utimeset.get(uid);
                    utimeSet.add(timeStamp);

                    final Double tokenCount = u2count_entry.getValue();

                    for (int occ = 0; occ < tokenCount; occ++) {
                        int newZ = Util.initialLatentState(cmdOption.znum);
                        int newB = Util.initialLatentState(2);
                        int newUprime = -1;

                        if (newB == Constant.INHERITANCE) {
                            //							draw initial Oprime from its reference
                            assert (in_userGraph.containsKey(uid)) : " in_userGraph doesn'time contains key " + uid;

                            newUprime = Util.initiaInfluencing(in_userGraph.get(uid));
                        }

                        SampleElementInfluenced e =
                          new SampleElementInfluenced(uid, timeStamp, wid, newZ, newB, newUprime);

                        List<SampleElementInfluenced> objChain = this.influencedObjChain_uwtzbup.get(uid);
                        if (objChain==null){
                            objChain = new ArrayList<>();
                            this.influencedObjChain_uwtzbup.put(uid, objChain);
                        }
                        objChain.add(e);

                        updCountInfluenced_uzbupwtime(uid, newZ, newB, newUprime, wid, timeStamp, 1);
                        influencedCount++;
                    }
                }
            }
        }
        System.out.println(Debugger.getCallerPosition()+ " influenced object count="+ influencedObjChain_uwtzbup.size()+
          " influenced sample chain size="+influencedCount+" memory usage="+Debugger.getMemoryUsed());
    }

    /**
     *   Update the sample for object.  synchronize the whole method.
     * @param upIndex
     * @param word
     * @param z
     * @param value
     */
    public void updCountInfluencing_upzwt(int upIndex, int z, int word, int time, int value){
        //influencing counts
        Util.update1MapIncreamental(N_up_influencing, upIndex, value);
        Util.update2MapIncreamental(N_upz_influencing, upIndex, z, value);
        Util.update3MapIncreamental(N_upzt_influencing, upIndex, z, time, value);

        //shared counts.  do not update topic count after each token sampling.  rebuild the topic word count after one iteration
//        Util.update2MapIncreamental(N_zw_all, z, word, value);
//        Util.update1MapIncreamental(N_z_all, z, value);
    }
    /**
     * udpate count of influenced object.  synchronize the whole method.
     * @param u
     * @param word
     * @param timestamp
     * @param z
     * @param b
     * @param uprime
     * @param value
     */
    public void updCountInfluenced_uzbupwtime(
      int u, int z, int b, int uprime, int word, int timestamp, int value){
        if(b==Constant.INHERITANCE){ //b=1; use the inherited data
            Util.update1MapIncreamental(N_up_inher_influenced, uprime, value);
            Util.update2MapIncreamental(N_upz_inher_influenced, uprime, z, value);
            Util.update2MapIncreamental(N_uup_inher_influenced, u, uprime, value);
            Util.update3MapIncreamental(N_uupz_inher_influenced, u, uprime, z, value);
            Util.update4MapIncreamental(N_uupzt_inher_influenced, u, uprime, z,timestamp, value);
        }

        else if (b==Constant.INNOTVATION){//b=0 innovative
            Util.update2MapIncreamental(N_uz_innov_influenced, u, z,  value);
            Util.update3MapIncreamental(N_uzt_innov_influenced, u, z, timestamp, value);
        }

        Util.update2MapIncreamental(N_ub_influenced, u, b, value);
        Util.update1MapIncreamental(N_u_influenced, u, value);

        //shared counts.  do not update topic count after each token sampling.  rebuild the topic word count after one iteration
//        Util.update2MapIncreamental(N_zw_all, z, word, value);
//        Util.update1MapIncreamental(N_z_all, z, value);
    }

    /**
     * rebuild the N_zw, N_z count table after one iteration
     */
    private void rebuild_N_zw_N_z(){
        N_zw_all = null;
        N_z_all = null;
//        if (cmdOption.concurrent.equals("y")) { //concurrent sampling
//            N_zw_all = new ConcurrentHashMap<>();
//            N_z_all = new ConcurrentHashMap<>();
//        }
//        else if (cmdOption.concurrent.equals("n")){//serial sampling
            N_zw_all = new HashMap<>();
            N_z_all = new HashMap<>();
//        }

        for (Map.Entry<Integer, List<SampleElementInfluencing>> entry : this.influencingObjChain_utz.entrySet())
            for (SampleElementInfluencing e : entry.getValue()){
                Util.update2MapIncreamental(N_zw_all, e.z, e.w, +1);
                Util.update1MapIncreamental(N_z_all, e.z, +1);
            }
        for (Map.Entry<Integer, List<SampleElementInfluenced>> entry : this.influencedObjChain_uwtzbup.entrySet())
            for (SampleElementInfluenced e : entry.getValue()){
                Util.update2MapIncreamental(N_zw_all, e.z, e.w, +1);
                Util.update1MapIncreamental(N_z_all, e.z, +1);
            }
    }

    public void checkSampleCountConsistency()
    {
        double tmpsum =0 ;

//        check shared counts
        //1. total N_z_all[i] = \sum_j N_zw_all[i][j]
        for(int z=0;z<cmdOption.znum;z++){
            tmpsum = 0;

            if (N_zw_all.containsKey(z))
                for(int word : N_zw_all.get(z).keySet())
                    tmpsum += Util.get2Map(N_zw_all, z, word);

            assert(tmpsum == Util.get1Map(N_z_all, z)):"ERROR N_z_all["+z+"]="+Util.get1Map(N_z_all, z)
              +" sum over N_zw_all["+z+"]="+tmpsum;
        }

//        check influenced counts
        //2. influenced: N_u_influenced[i] = \sum_b N_ub_influenced[i][b]
        for(int uid : in_userGraph.keySet()){
            tmpsum = 0;
            for(int b=0;b<2;b++)
                tmpsum+=Util.get2Map(N_ub_influenced, uid, b);

            assert(tmpsum == Util.get1Map(N_u_influenced, uid)):"ERROR N_u_influenced["+uid+"]";
        }

        //3. influenced b0 and b1
        //get b0sample sum and b1 sample sum
        double[] bsum = new double[2];
        bsum[0] = bsum[1] = 0;
        for(int b : new int[]{Constant.INNOTVATION, Constant.INHERITANCE} ) {
            for(int uid : in_userGraph.keySet()){
                bsum[b]+= Util.get2Map(N_ub_influenced, uid, b);
            }
        }

        //4. influenced b innovative
        //N_uz_innov_influenced sum should be equal to bsum[innovative]
        tmpsum=0;
        for(int uid : in_userGraph.keySet()){
            for(int z=0;z<cmdOption.znum;z++)
                tmpsum+=Util.get2Map(N_uz_innov_influenced, uid, z);
        }
        assert(tmpsum == bsum[Constant.INNOTVATION]):
          "bsum["+Constant.INNOTVATION+"]="+bsum[Constant.INNOTVATION]
          +" sum over u z N_uzb=0[][]"+tmpsum;

        //5. influenced b inheritance
        tmpsum=0;
        //N_up_influencing sum should be equal to bsum[inheritance]
        for (int uid : in_userGraph.keySet())
            for(int upId : in_userGraph.get(uid))
                tmpsum+= Util.get2Map(N_uup_inher_influenced, uid, upId);

        assert(tmpsum == bsum[Constant.INHERITANCE]):
          "bsum["+Constant.INHERITANCE+"]="+bsum[Constant.INHERITANCE]
          +" sum over u up N_uup[][]="+tmpsum;

        //6. influenced N_ub_influenced and N_uup_inher_influenced
        for(int uid : N_ub_influenced.keySet()){
            tmpsum = 0;
            for(int upId : in_userGraph.get(uid)){
                tmpsum+=Util.get2Map(N_uup_inher_influenced, uid, upId);
            }
            assert(tmpsum == Util.get2Map(N_ub_influenced, uid, Constant.INHERITANCE)):
              "N_ub_influenced["+uid+"]["+Constant.INHERITANCE+"]!="+tmpsum;
        }

        //7.  N_upz_inher_influenced, N_uup_inher_influenced and N_uupz_inher_influenced
        for (int upId : N_upz_inher_influenced.keySet()){
            for (int z =0; z<Constant.zNum; z++){
                tmpsum = 0;
                for (int uid : N_uupz_inher_influenced.keySet()){
                    tmpsum += Util.get3Map(N_uupz_inher_influenced, uid, upId, z);
                }
                assert (tmpsum == Util.get2Map(N_upz_inher_influenced, upId, z)):
                  "N_upz_inher_influenced["+upId+"]["+z+"]!="+tmpsum;
            }
        }

        for (int uid : N_uup_inher_influenced.keySet()){
            for (int upId : N_uup_inher_influenced.get(uid).keySet()){
                tmpsum = 0;
                for (int z = 0; z<Constant.zNum; z++){
                    tmpsum += Util.get3Map(N_uupz_inher_influenced, uid, upId, z);
                }
                assert (tmpsum == Util.get2Map(N_uup_inher_influenced, uid, upId)):
                  "N_uup_inher_influenced["+uid+"]["+upId+"]!="+tmpsum;
            }
        }

        //8. influenced N_uupz_inher_influenced, N_uupzt_inher_influenced
        for (int uid : N_uupz_inher_influenced.keySet())
            for (int upId : in_userGraph.get(uid))
                for (int z = 0; z<Constant.zNum; z++){
                    tmpsum = 0;
                    for (int time : Constant.timestampSet){
                        tmpsum += Util.get4Map(N_uupzt_inher_influenced, uid, upId, z, time);
                    }
                    assert (tmpsum == Util.get3Map(N_uupz_inher_influenced, uid, upId, z)):
                      "N_uupz_inher_influenced["+uid+"]["+upId+"]["+z+"]!="+tmpsum;
                }


//       influencing counts
        //9. influencing N_up_influencing and N_upz_influencing
        for(int upid : N_up_influencing.keySet()){
            tmpsum=0;
            for(int z=0;z<cmdOption.znum;z++){
                tmpsum+=Util.get2Map(N_upz_influencing, upid, z);
            }
            assert(tmpsum == Util.get1Map(N_up_influencing, upid)):
              "N_up_influencing["+upid+"]="+Util.get1Map(N_up_influencing, upid)+
                " sum over N_upz_influencing["+upid+"][]="+tmpsum;
        }

//        10. N_upz_influencing, N_upzt_influencing
        for (int upId : N_upz_influencing.keySet()){
            for (int z = 0; z<Constant.zNum; z++){
                tmpsum = 0;
                for (int t : Constant.timestampSet){
                    tmpsum+=Util.get3Map(N_upzt_influencing, upId, z, t);
                }
                assert (tmpsum==Util.get2Map(N_upz_influencing, upId, z)):
                  "ERROR: N_upz_influencing["+upId+"]["+z+"]="+Util.get2Map(N_upz_influencing, upId, z)
                    +" sum over t N_upzt_influencing["+upId+"]["+z+"]="+tmpsum;
            }
        }

        System.out.println(Debugger.getCallerPosition()+"Counts are CONSISTENT.");
    }

    /**
     * Draw sample for one iteration
     */
    public void drawOneIterationSample(boolean trainOrTest,  ExecutorService pool) {
        Date beginPoint = new Date();
        drawOneIterationInfluencingSample(trainOrTest, pool);
        Date endPoint = new Date();
        Util.printMsTimeBetween(Debugger.getCallerPosition()+" chain-"+this.chainId
          +" time used in influencing sampling", beginPoint, endPoint);

        beginPoint = new Date();
        this.rebuild_N_zw_N_z();
        endPoint = new Date();
        Util.printMsTimeBetween(Debugger.getCallerPosition()+" chain-"+this.chainId
          +" time used in rebuiling N_zw N_z", beginPoint, endPoint);

        if (cmdOption.checkConsistence.equals("y")){
            //check the consistency of the sample counts
            this.checkSampleCountConsistency();
            System.out.println(Debugger.getCallerPosition()
              + " after one iteration influencing sampling, count is consistent");
        }

        beginPoint = new Date();
        drawOneIterationInfluencedSample(trainOrTest, pool);
        endPoint = new Date();
        Util.printMsTimeBetween(Debugger.getCallerPosition()+" chain-"+this.chainId
          +" time used in influenced sampling", beginPoint, endPoint);

        beginPoint = new Date();
        this.rebuild_N_zw_N_z();
        endPoint = new Date();
        Util.printMsTimeBetween(Debugger.getCallerPosition()+" chain-"+this.chainId
          +" time used in rebuiling N_zw N_z", beginPoint, endPoint);

        if (cmdOption.checkConsistence.equals("y")){
            //check the consistency of the sample counts
            this.checkSampleCountConsistency();
            System.out.println(Debugger.getCallerPosition()
              +" after one iteration influenced sampling, count is consistent");
        }

    }

    /**
     * Draw samples for one influencing document, update related counts
     * Tested, checked the first 10 element's update, correct
     */
    private void drawOneIterationInfluencingSample(boolean trainOrTest, ExecutorService pool)
    {
//        Date influencingBeginDate = new Date();

        Map<Integer, Future<String>> futureMap = new HashMap<Integer, Future<String>>();

        Date beginSubmit = new Date();

        System.out.println(Debugger.getCallerPosition()+" submit threads");
        for (Map.Entry<Integer, List<SampleElementInfluencing>> entry : influencingObjChain_utz.entrySet()){
//        for each group of tokens, start a thread
            int upIdx = entry.getKey();
            List<SampleElementInfluencing> list = entry.getValue();

//           start gibbs sampling threads
            InfluencingGibbsSampler gs = influencingThreadMap.get(upIdx);
            if (gs==null){
                gs = new InfluencingGibbsSampler(upIdx, list, this, trainOrTest);
                influencingThreadMap.put(upIdx, gs);
            }

            gs.setTrainOrTest(trainOrTest);
            gs.setThreadId(0);
            futureMap.put(upIdx, pool.submit(gs));
        }
        Date endSubmit = new Date();

        Date beginRunning = new Date();

        try {
            Thread.sleep(cmdOption.numThread);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        System.out.println(Debugger.getCallerPosition()+" get begin time stamp");
//        //check begin time stamp
//        Map<Long, long[]> beginStamp = new HashMap<Long, long[]>();
//        for (Map.Entry<Integer, InfluencingGibbsSampler> entry : influencingThreadMap.entrySet()){
//            InfluencingGibbsSampler sampler = entry.getValue();
//            long[] tmp = sampler.printSamplerInfo(tmxb);
//            beginStamp.put(sampler.getThreadId(), tmp);
//        }

        //          wait for all thread to finish
        System.out.println(Debugger.getCallerPosition()+" waiting for influencing threads to finish");
        for (Map.Entry<Integer, Future<String >> entry : futureMap.entrySet()){
            try {
                Future<String> f =  entry.getValue();
                String message = f.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
//        compute the averaged influencing thread running time
        int n=0;
        double avg_b_time = 0;
        double avg_w_time = 0;
        double avg_c_time = 0;
        for (Map.Entry<Integer, List<SampleElementInfluencing>> entry : influencingObjChain_utz.entrySet()) {
            int upIdx = entry.getKey();
            InfluencingGibbsSampler gs = influencingThreadMap.get(upIdx);
            long b_time = gs.blocked_time;
            long w_time = gs.waiting_time;
            long c_time = gs.cpu_time;
            avg_b_time += b_time;
            avg_w_time += w_time;
            avg_c_time += c_time;
            n++;
        }
        avg_b_time /= n;
        avg_w_time /= n;
        avg_c_time /= n;
        System.out.println(Debugger.getCallerPosition()+" chain-"+chainId+" influencing chains avg blocking time="+avg_b_time
        +" avg waiting time="+avg_w_time+" avg cpu time="+avg_c_time);

        Date endRunning = new Date();
        Util.printMsTimeBetween(Debugger.getCallerPosition()+" chain-"+this.chainId+" influencing thread submission", beginSubmit, endSubmit);
        Util.printMsTimeBetween(Debugger.getCallerPosition()+" chain-"+this.chainId+" influencing all threads from submit to end running", beginSubmit, endRunning);
        Util.printMsTimeBetween(Debugger.getCallerPosition()+" chain-"+this.chainId+" influencing all threads running", beginRunning, endRunning);


//        Map<Long, Integer> thread2groupMap = new HashMap<Long, Integer>();
////        check end time stamp
//        Map<Long, long[]> endStamp = new HashMap<Long, long[]>();
//        for (Map.Entry<Integer, InfluencingGibbsSampler> entry : influencingThreadMap.entrySet()){
//            InfluencingGibbsSampler sampler = entry.getValue();
//            long[] tmp = sampler.printSamplerInfo(tmxb);
//            endStamp.put(sampler.getThreadId(), tmp);
//            thread2groupMap.put(sampler.getThreadId(), entry.getKey());
////            sampler.setThreadId(0);
//        }

//        System.out.println(Debugger.getCallerPosition() + "influencing thread number=" + beginStamp.size());
//
//        //for each thread print time detail
//        if (cmdOption.printThread.equals("y")) {
//            for (Map.Entry<Long, long[]> entry : endStamp.entrySet()) {
//                long tid = entry.getKey();
//                int gid = thread2groupMap.get(tid);
//                int chainSize = influencingThreadMap.get(gid).getChainSize();
//                long[] begin = beginStamp.get(tid);
//                long[] end = entry.getValue();
//
//                if (begin == null)
//                    continue;
//
//                System.out.println(Debugger.getCallerPosition() + "\n ONE ITERATION influencing chain-" + this.chainId
//                  + " thread-" + tid
//                  + " group-" + gid
//                  + " length=" + chainSize
//                  + " blocked_time=" + (end[0] - begin[0])
//                  + " waiting_time=" + (end[1] - begin[1])
//                  + " cpu_time=" + (end[2] - begin[2])
//                  + "\n TOTAL TIME blocked=" + end[0] + " waiting=" + end[1] + " cpu=" + end[2]);
//
//                String record =
//                  gid
//                    + "\t" + tid
//                    + "\t" + chainSize
//                    + "\t" + (end[0] - begin[0])
//                    + "\t" + (end[1] - begin[1])
//                    + "\t" + (end[2] - begin[2])
//                    + "\t" + end[0]
//                    + "\t" + end[1]
//                    + "\t" + end[2] + "\n";
//
////                time usage for one thread
//                double[] iterRecord = new double[4];
//                iterRecord[0] = chainSize;
//                iterRecord[1] = (end[0] - begin[0]);
//                iterRecord[2] = (end[1] - begin[1]);
//                iterRecord[3] = (end[2] - begin[2]);
//
//            }
//        }
    }

    /**
     * Draw samples for one influenced document, update related counts
     */
    private void drawOneIterationInfluencedSample(boolean trainOrTest, ExecutorService pool) {
//        Date influencedBeginDate = new Date();
        System.out.println(Debugger.getCallerPosition()+" sequential sampling="+cmdOption.seq);


//        for each group of tokens, start a thread
            Map<Integer, Future<String>> futureMap = new HashMap<Integer, Future<String>>();

            Date beginSubmit = new Date();
//        TODO thread load balance
            for (Map.Entry<Integer, List<SampleElementInfluenced>> entry : influencedObjChain_uwtzbup.entrySet()){
                int uIdx = entry.getKey();
                List<SampleElementInfluenced> list = entry.getValue();

//           start gibbs sampling thread
                InfluencedGibbsSampler gs = influencedThreadMap.get(uIdx);
                if (gs==null) {
                    gs = new InfluencedGibbsSampler(uIdx, list, this, trainOrTest);
                    influencedThreadMap.put(uIdx, gs);
                }

                gs.setTrainOrTest(trainOrTest);
                futureMap.put(uIdx, pool.submit(gs));
            }

            //invoke all block.
//            try {
//                pool.invokeAll(influencedThreadMap.values());
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            Date endSubmit = new Date();

            Date beginRunning = new Date();
            try {
                Thread.sleep(cmdOption.numThread);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            //check begin time stamp
//            Map<Long, long[]> beginStamp = new HashMap<Long, long[]>();
//            for (Map.Entry<Integer, InfluencedGibbsSampler> entry : influencedThreadMap.entrySet()){
//                InfluencedGibbsSampler sampler = entry.getValue();
//                long[] tmp = sampler.printSamplerInfo(tmxb);
//                beginStamp.put(sampler.getThreadId(), tmp);
//            }

//          wait for all thread to finish
            for (Map.Entry<Integer, Future<String >> entry : futureMap.entrySet()){
                try {
                    Future<String> f =  entry.getValue();
                    String message = f.get();
//                    System.out.println(Debugger.getCallerPosition()+" "+message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        //        compute the averaged influencing thread running time
        int n=0;
        double avg_b_time = 0;
        double avg_w_time = 0;
        double avg_c_time = 0;
        for (Map.Entry<Integer, List<SampleElementInfluenced>> entry : influencedObjChain_uwtzbup.entrySet()){
            int uIdx = entry.getKey();
            InfluencedGibbsSampler gs = influencedThreadMap.get(uIdx);

            long b_time = gs.blocked_time;
            long w_time = gs.waiting_time;
            long c_time = gs.cpu_time;
            avg_b_time += b_time;
            avg_w_time += w_time;
            avg_c_time += c_time;
            n++;
        }
        avg_b_time /= n;
        avg_w_time /= n;
        avg_c_time /= n;
        System.out.println(Debugger.getCallerPosition()+" chain-"+chainId+" influenced chains avg blocking time="+avg_b_time
          +" avg waiting time="+avg_w_time+" avg cpu time="+avg_c_time);

            Date endRunning = new Date();
            Util.printMsTimeBetween(Debugger.getCallerPosition()+" chain-"+this.chainId+" influenced thread submission", beginSubmit, endSubmit);
            Util.printMsTimeBetween(Debugger.getCallerPosition()+" chain-"+this.chainId+" influenced all threads from submit to end running", beginSubmit, endRunning);
            Util.printMsTimeBetween(Debugger.getCallerPosition()+" chain-"+this.chainId+" influenced all threads running", beginRunning, endRunning);

//            Map<Long, Integer> thread2groupMap = new HashMap<Long, Integer>();
//            //        check end time stamp
//            Map<Long, long[]> endStamp = new HashMap<Long, long[]>();
//            for (Map.Entry<Integer, InfluencedGibbsSampler> entry : influencedThreadMap.entrySet()){
//                InfluencedGibbsSampler sampler = entry.getValue();
//                long[] tmp = sampler.printSamplerInfo(tmxb);
//                endStamp.put(sampler.getThreadId(), tmp);
//                thread2groupMap.put(sampler.getThreadId(), entry.getKey());
//                sampler.setThreadId(0);
//            }
              
//            System.out.println(Debugger.getCallerPosition()+"influenced thread number="+beginStamp.size());

//            //for each thread print time detail
//            if (cmdOption.printThread.equals("y"))
//                for (Map.Entry<Long, long[]> entry : endStamp.entrySet()){
//                    long tid = entry.getKey();
//                    int gid = thread2groupMap.get(tid);
//                    int chainSize = influencedThreadMap.get(gid).getChainSize();
//                    int avgOp = influencedThreadMap.get(gid).avgOp;
//                    long[] begin = beginStamp.get(tid);
//                    long[] end = entry.getValue();
//
////                    || influencedThreadMap.get(gid)==null
//                    if (begin==null )
//                        continue;
//
////                    Debugger.getCallerPosition()+
//                    System.out.println(Debugger.getCallerPosition()+"\n ONE ITERATION influenced chain-"+this.chainId
//                      +" thread-"+tid
//                      +" group-"+gid
//                      +" length="+chainSize
//                      +" avgOp="+avgOp
//                      +" blocked="+(end[0]-begin[0])
//                      +" waiting="+(end[1]-begin[1])
//                      +" cpu="+(end[2]-begin[2])
//                      +"\n TOTAL TIME blocked="+end[0]+" waiting="+end[1]+" cpu="+end[2]);
//
//                    String record =
//                      gid
//                        +"\t"+tid
//                        +"\t"+chainSize
//                        +"\t"+avgOp
//                        +"\t"+(end[0]-begin[0])
//                        +"\t"+(end[1]-begin[1])
//                        +"\t"+(end[2]-begin[2])
//                        +"\t"+end[0]
//                        +"\t"+end[1]
//                        +"\t"+end[2]+"\n";
//
//                    double[] iterRecord = new double[5];
//                    iterRecord[0] = chainSize;
//                    iterRecord[1] = avgOp;
//                    iterRecord[2] = end[0] - begin[0];
//                    iterRecord[3] = end[1] - begin[1];
//                    iterRecord[4] = end[2] - begin[2];
//
//                }
    }

    public String toString() {
        final StringBuffer str = new StringBuffer();

        str.append(Debugger.getCallerPosition()+"****Sample**** \n");

        return str.toString();
    }

    /**
     * calculate log likelihood of this model
     */
    public void calculate_llh(){
        llh = 0;

        for (InfluencingGibbsSampler gs : influencingThreadMap.values())
            llh += gs.llh;
        for (InfluencedGibbsSampler gs : influencedThreadMap.values())
            llh += gs.llh;

        /**
         * sequential llh calculation is too slow.  move the llh calculation to each thread.
        //influencing part
        for (Map.Entry<Integer, List<SampleElementInfluencing>> entry : this.influencingObjChain_utz.entrySet())
            for (SampleElementInfluencing e: entry.getValue()){
                double sampleProb = 0;
                for (int tmpZ=0; tmpZ< Constant.zNum; tmpZ++)
                    sampleProb += Probability.influencingPosterior_z(e.u, e.w, tmpZ, e.time, this, this.cmdOption);

                llh += Math.log(sampleProb);
            }

        //influenced part
        for (Map.Entry<Integer, List<SampleElementInfluenced>> entry : this.influencedObjChain_uwtzbup.entrySet())
            for (SampleElementInfluenced e : entry.getValue()){
                double sampleProb = 0;
                //prob(b=0)
                double sampleProb0 = 0;
                for (int tmpZ=0; tmpZ< Constant.zNum; tmpZ++)
                    sampleProb0 += Probability.influencedPosterior_bupztw(e.u, e.w, e.time, tmpZ, Constant.INNOTVATION, -1, this, cmdOption);

                //prob(b=1)
                double sampleProb1 = 0;
                for (int up : in_userGraph.get(e.u))
                    for (int tmpZ=0; tmpZ< Constant.zNum; tmpZ++)
                        sampleProb1 += Probability.influencedPosterior_bupztw(e.u, e.w, e.time, tmpZ, Constant.INHERITANCE, up, this, cmdOption);

                sampleProb = sampleProb0 + sampleProb1;

                llh += Math.log(sampleProb);
            }
         */
    }

    public void reset_llh(){ llh = 0; }

    /**
     * calculate perplexity of this model.
     */
    public void calculate_perplexity(){
        perplexity = Math.exp( -1*(llh) / (influencingCount+influencedCount) );
    }
}
