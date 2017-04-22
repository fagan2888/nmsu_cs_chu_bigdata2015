package sampling;

import util.CmdOption;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chu on 6/16/14.
 */
public class MainInfDetectionTest {
    public static void main(String[] args){
        CmdOption option = new CmdOption();
        option.chainNum = 2;
        option.graphfile = "./data/twitter500/cite.txt";
        option.datafile = "./data/twitter500/tweet/";
        option.SAMPLER_ID = "twitter_500_z_10_numThread_test";
        option.znum = 10;
        option.burnin = 100;
        option.concurrent = "n";
        option.numThread = 2;

        //2. do Gibbs sampling to estimate parameters on all data.
        Set<Integer> emptyTestSet = new HashSet<Integer>();
        SamplerChain samplerChain = new SamplerChain(option, emptyTestSet);
        samplerChain.doGibbs();
    }
}