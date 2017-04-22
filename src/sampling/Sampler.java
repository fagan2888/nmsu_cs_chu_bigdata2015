package sampling;

import convergence.ConvergenceDiagnose;
import util.CmdOption;
import util.Constant;
import util.Debugger;
import util.Util;
import preprocess.DataParsed;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Sampler that do Gibbs sampling.
 * @author convergence
 *
 */
public class Sampler {
	
	//input parameters for the sampler
	//private Parameter in_DistributionParam;
	private CmdOption cmdOption;
	//private String in_InputGraphFileName;
	public int runnableChainId =-1;
	List<Integer> allChainIds; 
	
	public DataParsed parsedData; //internal structures of the parsed data
	
    boolean takeSamplesFromThisChain; 	//the final results is calculated from this sampler
    									//the data structure sampleAvgData needs to be calculated
    
    public int totalIter = 0;
    
    public long totalTime = 0;
    
    private SampleData sampleData = null;

    //  operation -> iteration -> time
    Map<String, List<Long>> sampler_time_map;

    ConvergenceDiagnose convDiag;

    /**
     * return last SampleData
     * @return
     */
    public SampleData getSampleData(){
    	return this.sampleData;
    }
    
    //private MiniDistribution mapTopicPosteriorDistr;
    /**
     * Create the internal data structure that would be used in the sampling process.
     * (1) Sample structure
	 * (2) Sample drawing for each document  
	 *  @param _takeSamplesFromThisChain
     * @param _chainid
//     * @param testSet.  Test set
     * @param _sampleChain
     */
    public Sampler(boolean _takeSamplesFromThisChain, int _chainid, SamplerChain _sampleChain)
    {
    	cmdOption = _sampleChain.cmdOption;
    	takeSamplesFromThisChain = _takeSamplesFromThisChain;
    	allChainIds = _sampleChain.allChainIds;
    	runnableChainId = _chainid;
    	parsedData = _sampleChain.parsedData;
    	
    	System.out.println(Debugger.getCallerPosition()+"chain "+ runnableChainId +" init sample data...");
        sampleData = new SampleData(parsedData,
          cmdOption, _sampleChain.testSet, this);

        sampler_time_map = new TreeMap<String, List<Long>>();
        convDiag = _sampleChain.convDiag;
    }
    /**
	 * Draw the initial sample
	 * @param trainOrTest true: training; false: test
	 */
	public void drawInitSample(boolean trainOrTest)
	{
        Util.beginRecordTime(sampler_time_map, Constant.init_sample_time);
		//draw initial sample
		System.out.println(Debugger.getCallerPosition()+"chain "+ runnableChainId +"initial sample");
		sampleData.drawInitialSample(trainOrTest);

		//System.out.println(Debugger.getCallerPosition()+" initial SampleData\n"+sampleData);
        Util.endRecordTime(sampler_time_map, Constant.init_sample_time);

        Map<String, Long> sumMap = Util.sumTimeMap(sampler_time_map);
        System.out.println(Debugger.getCallerPosition() + " Initial sampling time="+sumMap.get(Constant.init_sample_time));
	}
	
    /**
     * Gibbs sampling process 
     * If there are multiple chains, this function is run concurrently
     * 
//     * @param BURN_IN: the number of iterations for burn-in stage
     */
    public void doGibbs(boolean trainOrTest)
	{
		boolean converged = false;
		
		int iter = 0;

        Date startCheckpoint = new Date();

        ExecutorService pool;

        if (cmdOption.numThread==-1)
            pool = Executors.newCachedThreadPool();
        else
            pool = Executors.newFixedThreadPool(cmdOption.numThread);

        System.out.println(Debugger.getCallerPosition()+" Thread pool size="+cmdOption.numThread);

        for (iter = 0; iter < cmdOption.numIter && !converged; iter++) {
//            Date samplerIterBeginDate = new Date();

            Date newCheckPoint = new Date();
            //time is in ms (0.001 second).
            System.out.println(Debugger.getCallerPosition()+"chain "+ runnableChainId +": Iteration " +
              iter + "/" + cmdOption.numIter + " time = " +
              (newCheckPoint.getTime() - startCheckpoint.getTime())+" memUsed="+Debugger.getMemoryUsed()
              +" available processors " + Runtime.getRuntime().availableProcessors()+"\n");

            //(1) draw sample for one iteration

            Util.beginRecordTime(sampler_time_map, Constant.sample_time);
            sampleData.drawOneIterationSample(trainOrTest, pool);
            Date beginPoint = new Date();
            sampleData.calculate_llh();
            sampleData.calculate_perplexity();
            Date endPoint = new Date();
            Util.printMsTimeBetween(Debugger.getCallerPosition()+" chain-"+this.runnableChainId
              +" time used in calculating llh and perplexity=", beginPoint, endPoint);

            Util.endRecordTime(sampler_time_map, Constant.sample_time);

            //(3) Check the convergence of two chains after the burnning in phase
//              TODO [DONE] each chain will do this   [DEBUG here; DEBUG DONE]
            if(iter>=cmdOption.burnin){
//                System.out.println(Debugger.getCallerPosition()+" current iteration "+ iter+" burning iteration  "+cmdOption.burnin);
                Util.beginRecordTime(sampler_time_map, Constant.converge_check_time);

                convDiag.addOneChainIteration(runnableChainId, sampleData.llh, sampleData.perplexity); //add this summary data to monitor; this does not create the problem that different chains are sampled in different rates
                System.out.println(Debugger.getCallerPosition()+" chain-"+runnableChainId+" iter="+iter+" llh="+sampleData.llh+" perplexity="+sampleData.perplexity+"\n");
                //check convergence of multiple chains
                converged = convDiag.checkAllChainConvergence();

                Util.endRecordTime(sampler_time_map, Constant.converge_check_time);
            }
            Date endCheckPoint = new Date();
            totalIter = iter;
            totalTime = endCheckPoint.getTime() - startCheckpoint.getTime();

            if (iter % 10 ==0 && takeSamplesFromThisChain){
                beginPoint = new Date();
                Util.prtResult2File(cmdOption, sampleData, totalIter, totalTime);
                convDiag.writeConvergenceHistory();
                endPoint = new Date();
                Util.printMsTimeBetween(Debugger.getCallerPosition()+" chain-"+this.runnableChainId
                  +" writing convergence history and parameter files time =", beginPoint, endPoint);
            }

//            Date samplerIterEndDate = new Date();
//            Util.printMsTimeBetween("chain-"+this.runnableChainId+" one iteration sampling method time=", samplerIterBeginDate, samplerIterEndDate);

        }//finish all the iterations
		//end for loop

        //            shutdown pool after all threads terminate
        pool.shutdown();

        long total_gibbs_sampling_time  =new Date().getTime()-startCheckpoint.getTime();
		System.out.println(Debugger.getCallerPosition()+"chain "+ runnableChainId +": Last Iteration: "
          + iter + ", converged multiple chain = "
          + converged+" total_Gibbs_sampling_time="
          +(total_gibbs_sampling_time));
		
//		this.sampleData.printSparsity();
		
		convDiag.finish();

        if (takeSamplesFromThisChain && cmdOption.concurrent.equals("y")){

//            write thread average time usage to global summary file and print detail usage per iteration into log.
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(new File(cmdOption.summary), true));
//             TODO
                bw.flush();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
	}
}
