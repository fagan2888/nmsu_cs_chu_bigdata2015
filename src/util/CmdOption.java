package util;

import org.kohsuke.args4j.Option;

import java.io.Serializable;

public class CmdOption implements Serializable{

	//@Option(name="-est", usage="Specify whether we want to estimate model from scratch")
	//public boolean est = false;
	
	@Option(name="-help", usage="Print this help info")
	public boolean help = false;
	
	@Option(name="-graphfile", usage="Input graph file name (default: empty)")
	public String graphfile = "";
	//public String graphfile = "data/pubidcite.txt"; 	
	
	@Option(name="-datafile", usage="Input paper folder (default: empty)")
	public String datafile = "";
	//public String datafile = "data/paper/";
	
	@Option(name="-duplicate", usage="Input paper folder (default: empty)")
	public String duplicate = "yes";

	@Option(name="-znum", usage="Number of latent states or topics (default: 10)")
	public int znum = 10;
	
	@Option(name="-numIter", usage="Number of Gibbs sampling iterations  (default: 1000)")
	public int numIter = 100;
	
	@Option(name="-burnin", usage="BURN IN iterations for Gibbs Sampling (default: 10)")
	public int burnin = 10;
	
	@Option(name="-chainNum", usage="The number of chains used to judge convergence (default: 2)")
	public int chainNum = 2;
	
	@Option(name="-rhat", usage="RHAT value for convergence (default: 1.01)")
	public double R_HAT_THRESH = 1.01;

	@Option(name="-samplerId", usage="The sampler id string (default: Cao)")
	public String SAMPLER_ID = "Cao";
	
    //Parameters for distributions p(w|latent-state)
	@Option(name="-alphaPhi", usage="Dirichlet parameter alphaPhi for latent state variables (Default: 0.01)")
	public double alphaPhi=0.01;
	
	//Dirichlet for p(latent-state|object)
	@Option(name="-alphaTheta", usage="Dirichlet parameter alphaTheta (Default: 0.1)")
	public double alphaTheta=0.1;
	
	@Option(name="-alphaGamma", usage="Dirichlet parameter alphaGamma for object interaction mixture (Default: 1.0)")
	public double alphaGamma=0.01;
	
	@Option(name="-alphaTau", usage="Dirichlet parameter alphaEta for aspect mixture (Default: 1.0)")
	public double alphaTau=1;
	
	@Option(name="-alphaLambdaInherit", usage="Dirichlet parameter inherit percentage (Default: 0.5)")
	public double alphaLambdaInherit=50;
	
	@Option(name="-alphaLambdaInnov", usage="Dirichlet parameter innovative percentage (Default: 0.5)")
	public double alphaLambdaInnov=50;
	
    @Option(name = "-numThread", usage = "number of thread during sampling.")
    public int numThread = 2;

    @Option(name = "-concurrent", usage = "use single thread or multi thread to do Gibbs sampling.  y or n (Default y)")
    public String concurrent = "n";

    @Option(name = "-checkConsistence", usage = "whether turning on consistence check. y or n (Default y)")
    public String checkConsistence = "y";

    @Option(name = "-printThread", usage = "whether print thread running information in each iteration.  y or n (Default n")
    public String  printThread = "n";

    @Option(name = "-debug", usage = "whether print debug information.  y or n (By default n)")
    public String debug = "n";

    @Option(name = "-seq", usage = "use sequential sampling (y) or blocked sampling (n).  blocked sampling by default")
    public String seq = "n";

    @Option(name = "-summary", usage = "global result summary file.  Stores the thread average detail running time." +
      "This file will be used many times.  Each time the program should append to the end of the file")
    public String summary = "./multithread_result/summary.xls";

    @Option(name = "-model", usage = "specify the model sampling use")
    public String model = "";

    @Option(name = "-serFilePath", usage = "serializable SampleData count file path")
    public String serFilePath = "null";

    @Option(name = "-retweetFile", usage = "retweet files")
    public String retweetFile= "null";
    @Option(name = "-tweetFile", usage = "tweet files")
    public String tweetFile= "null";
//
//    @Option(name = "-tokenTh", usage = "the token level threshold p(b=1)")
//    public double tokenTh = 0.5;
//
//    @Option(name = "-tweetTh", usage = "tweet level threshold p(retweet=1)")
//    public double tweetTh = 0.5;

}
