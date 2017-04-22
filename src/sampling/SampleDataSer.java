package sampling;

import com.google.common.util.concurrent.AtomicDouble;
import org.apache.commons.collections.BidiMap;
import util.CmdOption;
import util.Constant;

import java.io.Serializable;
import java.util.*;

/**
 * Created by chu on 8/26/14.
 * a wrapper for serializable SamplerData
 */
public class SampleDataSer implements Serializable{
    public CmdOption cmdOption;
    public Date firstDate;

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
    public double llh;
    /**
     * perplexity of this model
     */
    public double perplexity;

    public Map<Integer, List<Integer>> in_userGraph;

    public SampleDataSer(SampleData sampleData){
        this.cmdOption = sampleData.cmdOption;
        this.firstDate = sampleData.firstDate;
        this.N_u_influenced = sampleData.N_u_influenced;
        this.N_ub_influenced = sampleData.N_ub_influenced;
        this.N_uz_innov_influenced = sampleData.N_uz_innov_influenced;
        this.N_uzt_innov_influenced = sampleData.N_uzt_innov_influenced;
        this.N_up_inher_influenced = sampleData.N_up_inher_influenced;
        this.N_upz_inher_influenced = sampleData.N_upz_inher_influenced;
        this.N_uup_inher_influenced = sampleData.N_uup_inher_influenced;
        this.N_uupz_inher_influenced = sampleData.N_uupz_inher_influenced;
        this.N_uupzt_inher_influenced = sampleData.N_uupzt_inher_influenced;
        this.N_up_influencing = sampleData.N_up_influencing;
        this.N_upz_influencing = sampleData.N_upz_influencing;
        this.N_upzt_influencing = sampleData.N_upzt_influencing;
        this.N_zw_all = sampleData.N_zw_all;
        this.N_z_all = sampleData.N_z_all;
        this.vocabularyMap = sampleData.vocabularyMap;
        this.N_utimeset = sampleData.N_utimeset;
        this.N_uptimeset = sampleData.N_uptimeset;
        this.llh = sampleData.llh;
        this.perplexity = sampleData.perplexity;
        this.in_userGraph = sampleData.in_userGraph;
    }
}
