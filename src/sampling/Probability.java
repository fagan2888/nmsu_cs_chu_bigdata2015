package sampling;


import cc.mallet.types.Multinomial;
import cc.mallet.util.Randoms;
import org.apache.lucene.analysis.TokenStream;
import preprocess.DataParsed;
import util.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.SynchronousQueue;

public class Probability {
	
//	private static int averageBy = 1;
	
	/**
     * For objects that we do not consider estTemporalInf from others (influencing objects)
     * 
     * For uprime's w, draw its new z
     * @param uprime
     * @param w
     * @return
     */
    public static int getMAPInfluencing_z(CmdOption option, SampleData sampleData, int uprime, int w, int time)
    {
    	//mapTopicPosteriorDistr.clear();
    	MiniDistribution mapTopicPosteriorDistr = new MiniDistribution(option.znum + 1);
    	
    	//1. For each latent state, calculate its posterior probability p(z|others)
        for (int z = 0; z < option.znum; z++) {
            double prob = Probability.influencingPosterior_z(uprime, w, z, time, sampleData, option);
            mapTopicPosteriorDistr.put(z, prob);
        }
        
        //2. make sure the posterior probablity is correct
        if (mapTopicPosteriorDistr.sum() == 0.0) {
            System.err.println("Posterior distribution.sum()==0.0. distr(10):" + mapTopicPosteriorDistr.toString());
            mapTopicPosteriorDistr.initializeEqualDistribution(option.znum);
        } //else {
        //    posteriorDistr.normalize();
        //}
        
        //3. Draw a new latent state
        int mapZ = mapTopicPosteriorDistr.draw();
        //System.out.println(Debugger.getCallerPosition()+"mapTopicPosteriorDistr="+mapTopicPosteriorDistr+",mapK="+mapK);

        //test
//        System.out.println(uprime+" "+w+" normal prob sum "+mapTopicPosteriorDistr.sum());
        return mapZ;
    }

    /**
     * For objects that we do not consider estTemporalInf from others (influencing objects)
     *
     * For uprime's w, draw its new z using Multinomial distribution from mallet library
     * @param uprime
     * @param w
     * @return
     */
    public static int getMAPInfluencing_z_mallet(CmdOption option, SampleData sampleData, int uprime, int w, int time, boolean trainOrTest){
        double[] probArray = new double[option.znum];
        double sum = 0;
        //1. For each latent state, calculate its posterior probability p(z|others)
        for (int z = 0; z < option.znum; z++) {
            double prob = Probability.influencingPosterior_z(uprime, w, z, time, sampleData, option);
            sum += prob;
            probArray[z] = prob;
        }

        for (int z = 0; z<option.znum; z++)
            probArray[z] /= sum;

        Multinomial dist = new Multinomial(probArray);
        Random random = new Random();

        return dist.randomIndex(new Randoms(random.nextInt()));
    }

        /**
         *  andomly draw all latent variable at the same time.
         * @param uid
         * @param w
         * @param time
         * @param sampleData
         * @param option
         * @param trainOrTest
         * @return [mapZ, mapB, mapA, mapOprime]
         */
        public static int[] getMAPInfluenced_zbaop_blocked(int uid, int w, int time, SampleData sampleData, CmdOption option, boolean trainOrTest) {
            //MiniDistribution mapTopicPosteriorDistr = new MiniDistribution(option.znum * (option.oprimeNum + 1)*option.anum);
            Date begin = new Date();

            int distrSize = option.znum * (sampleData.in_userGraph.get(uid).size() + 1);

            MiniDistribution mapTopicPosteriorDistr = new MiniDistribution(distrSize);
            //calculate the probability of p(z=*,oprime=*|b= NOT innovative) when b is NOT innovative

            double sum0 = 0;
            double sum1 = 0;

            for (int z = 0; z < option.znum; z++) {
                double probGj = Probability.influencedPosterior_bupztw(
                  uid, w, time, z, Constant.INNOTVATION, -1, sampleData, option);
                sum0 += probGj;

                mapTopicPosteriorDistr.put(Constant.INNOTVATION, z, -1, probGj);
            }

            //     mapTopicPosteriorDistr.sum0 = mapTopicPosteriorDistr.sum();

            //calculate the probability of p(z=*,oprime=*|b=innovative) when b is innovative
            for (int uprime : sampleData.in_userGraph.get(uid)) {
                for (int z = 0; z < option.znum; z++) {//for each latent state
                    if ((uprime == uid) || (sampleData.testSet.contains(uprime) && trainOrTest))
                        continue;

                    double probGj = Probability.influencedPosterior_bupztw(
                      uid, w, time, z, Constant.INHERITANCE, uprime, sampleData, option);
                    sum1 += probGj;

                    mapTopicPosteriorDistr.put(Constant.INHERITANCE, z, uprime, probGj);
                }
            }
            //        if (Math.random()*100 > 99) //check the difference between sum p(b=0) and sum p(b=1).  It seems something wrong here
//        System.out.println(Debugger.getCallerPosition()+"uid="+uid+" w="+w+" time="+time+" sum p(b=0)="+(sum0)+" sum p(b=1)="+(sum1));

            //        System.out.println(Debugger.getCallerPosition()+" real sum probability "+mapTopicPosteriorDistr.sum());

            //     mapTopicPosteriorDistr.sum1 = mapTopicPosteriorDistr.sum() - mapTopicPosteriorDistr.sum0;
            //     System.out.println(Debugger.getCallerPosition()+" object "+obj+", w "+w+", time"+time+" 's prob "+mapTopicPosteriorDistr.toString());

            if (mapTopicPosteriorDistr.sum() == 0.0) {
                System.err.println("Posterior distribution.sum()==0.0. distr(10):" + mapTopicPosteriorDistr.toString());
                mapTopicPosteriorDistr.initializeEqualDistribution(option.znum);
            }
            //randomly draw new z, new b, new u' which follow the joint distribution calculated above
            int mapB = mapTopicPosteriorDistr.draw(); //mapZ
            int mapZ = mapTopicPosteriorDistr.getKey2Draw(); //mapB
            int mapUprime = mapTopicPosteriorDistr.getKey3Draw(); //mapUprime

            Date end = new Date();
//        System.out.println(Debugger.getCallerPosition()+" time used for sampling u="+uid+" w="+w+" t="+time+(end.getTime()-begin.getTime()));

            return new int[]{mapB, mapZ, mapUprime};
        }


        /**
         * sequentially draw latent variable.  samples are too skewed.  p(b=1) is much bigger than p(b=0)
         * @param u
         * @param word
         * @param time
         * @param sampleData
         * @param option
         * @param trainOrTest
         * @return [mapZ, mapB, mapA, mapOprime]
         */
        public static int[] getMAPInfluenced_zbaop_sequential(
          int u, int oldB, int oldUp, int oldZ, int word, int time, SampleData sampleData, CmdOption option, boolean trainOrTest)
        {
//    	TODO
            //draw mapB
            MiniDistribution bDist = new MiniDistribution(2);
            //P(b=0)
            bDist.put(Constant.INNOTVATION,
              beta(u, Constant.INNOTVATION, sampleData)*
                theta(u, oldZ, sampleData)*
                tau(u, Constant.INNOTVATION, oldUp, oldZ, time, sampleData));
            //P(b=1 | oldB=0)
            if (oldB==Constant.INNOTVATION)
                bDist.put(Constant.INHERITANCE,
                  beta(u, Constant.INHERITANCE, sampleData)*
                    theta(u, oldZ, sampleData)*
                    tau(u, Constant.INNOTVATION, oldUp, oldZ, time, sampleData));
                // P(b=1 | oldB=1)
            else if (oldB==Constant.INHERITANCE)
                bDist.put(Constant.INHERITANCE,
                  beta(u, Constant.INHERITANCE, sampleData)*
                    thetaPrime(oldUp, oldZ, sampleData)*
                    tau(u, Constant.INHERITANCE, oldUp, oldZ, time, sampleData));

            int mapB = bDist.draw(); //mapB
            int mapZ = 0; //mapZ
            int mapUprime = 0; //mapUprime

            if (mapB==Constant.INNOTVATION){//b=0
                mapUprime = -1;
                //draw mapZ
                MiniDistribution zDist = new MiniDistribution(Constant.zNum);
                for (int temZ=0; temZ<Constant.zNum; temZ++){
                    double theta = theta(u, temZ, sampleData);
                    double phi = phi(temZ, word, sampleData);
                    double tau = tau(u, mapB, mapUprime, temZ, time, sampleData);
                    zDist.put(temZ, phi*theta*tau);
                }
                mapZ = zDist.draw();
            }
            else if (mapB==Constant.INHERITANCE){//b=1
                //draw mapUp
                MiniDistribution upDist = new MiniDistribution(sampleData.in_userGraph.get(u).size());
                for (int temUp : sampleData.in_userGraph.get(u)){
                    double gamma = gamma(u, temUp, sampleData);
                    double thetePrime = thetaPrime(temUp, oldZ, sampleData);
                    double tau = tau(u, mapB, temUp, oldZ, time, sampleData);
                    upDist.put(temUp, gamma*thetePrime*tau);
                }
                mapUprime = upDist.draw();
                //draw mapZ
                MiniDistribution zDist = new MiniDistribution(Constant.zNum);
                for (int temZ=0; temZ<Constant.zNum; temZ++){
                    double phi = phi(temZ, word, sampleData);
                    double thetePrime = thetaPrime(mapUprime, temZ, sampleData);
                    double tau = tau(u, mapB, mapUprime, temZ, time, sampleData);
                    zDist.put(temZ, phi*thetePrime*tau);
                }
                mapZ = zDist.draw();
            }

            return new int[]{mapB, mapZ, mapUprime};
        }

        //p(z|...)= N1 *N2
    public static double influencingPosterior_z(int uprime, int w, int z, int time,
                                                final SampleData data, final CmdOption option) {//CitinfSampler.citedPosteriorT
		double phi = phi(z, w, data);
        double thetaPrime = thetaPrime(uprime, z, data);
        double tauPrime = tauPrime(uprime, z, time, data);

		double prob =  phi * thetaPrime * tauPrime; //should it be (N2*N3)*(N3*lambda)? TODO
        assert (prob > 0.0) : "probG0 must be positive but is " + prob +
        ". uprime=" + uprime + " w=" + w + " z=" + z+" phi="+phi+
          " thetaPrime="+thetaPrime+" tauPrime"+tauPrime;

        assert (!Double.isNaN(prob));
        assert (!Double.isInfinite(prob));
        
        return prob;
    }

    /**
     * computer joint probability of one position
     * @param uid
     * @param w
     * @param time
     * @param z
     * @param b
     * @param uprime
     * @param data
     * @param cmdOption
     * @return
     */
	public static double influencedPosterior_bupztw(int uid, int w, int time, int z, int b, int uprime,
                                                    final SampleData data, final CmdOption cmdOption)
	{
		double prob = 0.0;
		if (b == Constant.INNOTVATION) {//for b=0, innovation 
			double beta = beta(uid, b, data); //p(b|.)
            double theta = theta(uid, z, data);// p(z|.)
            double phi = phi(z, w, data);
			double tau = tau(uid, b, uprime, z, time, data);

            prob = beta * theta * phi * tau;

            assert (beta > 0) : "b=0 beta must be positive but is"  + beta + ". uid=" + uid + " time=" + w + " z=" + z;
            assert (theta> 0) : "b=0 theta must be positive but is"  + theta + ". uid=" + uid + " time=" + w + " z=" + z;
            assert (phi> 0) : "b=0 phi must be positive but is"  + phi + ". uid=" + uid + " time=" + w + " z=" + z;
            assert (tau> 0) : "b=0 tau must be positive but is"  + tau + ". uid=" + uid + " time=" + w + " z=" + z;

		} else {//b=1, 
			double beta = beta(uid, b, data);//p(b|.)
			double thetaPrime = thetaPrime(uprime, z, data);
			double gamma = gamma(uid, uprime, data);
            double tau = tau(uid, b, uprime, z, time, data) ;
			double phi = phi(z, w, data);

            assert (beta > 0) : "b=1 beta must be positive but is"  + prob + ". uid=" + uid + " time=" + w + " z=" + z;
            assert (thetaPrime> 0) : "b=1 thetaPrime must be positive but is"  + prob + ". uid=" + uid + " time=" + w + " z=" + z;
            assert (phi > 0) : "b=1 phi must be positive but is"  + prob + ". uid=" + uid + " time=" + w + " z=" + z;
            assert (gamma > 0) : "b=1 gamma must be positive but is"  + prob + ". uid=" + uid + " time=" + w + " z=" + z;
            assert (tau> 0) : "b=1 tau must be positive but is"  + prob + ". uid=" + uid + " time=" + w + " z=" + z;

            prob = beta * thetaPrime * gamma *  phi * tau;
		}
		
		assert (prob > 0.0) : "probG0 must be positive but is " + prob + ". uid=" + uid + " time=" + w + " z=" + z;
		assert (!Double.isNaN(prob));
		assert (!Double.isInfinite(prob));
		return prob;
    }

    /**
     * retweet predication method
     * @param u
     * @param timeStamp
     * @param tweet
     * @param data
     * @param cmdOption
     * @param tokenTh the token level threshold p(b=1)
     * @param tweetTh tweet level threshold p(retweet=1)
     * @return
     */
    public static boolean retweetPredicate(int u, int timeStamp, String tweet, final SampleData data, final CmdOption cmdOption,
                                           double tokenTh, double tweetTh){

        //remove url
        String t1 = tweet.replaceAll("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", "");
        //remove non-ascii
//        String t2 = t1.replaceAll("[^\\x00-\\x7F]", "");
        //remove hashtag.  If we remove \@ the prediction performance accuracy=0.66
        String t2 = t1.replaceAll("(#)\\w*(\\s|$)","");

//        TODO[DONE] majority vote
        int b0count = 0;
        int b1count = 0;

        //compute retweet and unretweet probability
        try(TokenStream tokenizer = DataParsed.getStemmer().tokenStream("", new StringReader(t2))){
            while (tokenizer.incrementToken()){
//        StringTokenizer tokenizer = new StringTokenizer(tweet);
//            while (tokenizer.hasMoreTokens()){
                String token = tokenizer.reflectAsString(false);
                int termBegin = 5;
                int termEnd = token.indexOf(",");
                final String word = token.substring(termBegin, termEnd);

//                final String word = tokenizer.nextToken();

                if (!data.vocabularyMap.containsKey(word) || word.equalsIgnoreCase("rt")) continue;

                int wIndex = (Integer)(data.vocabularyMap.get(word));

                System.out.print(word+" ");
                double wUnRetweetProb = 0;
                double wRetweetProb = 0;

//                System.out.println(word+" p(b=0) components");
                for (int z=0; z<cmdOption.znum; z++) {
                    double beta = beta(u, Constant.INNOTVATION, data);
                    double theta = theta(u, z, data);
                    double tau = tau(u, Constant.INNOTVATION, -1, z, timeStamp, data);
                    double phi = phi(z, wIndex, data);
//                    System.out.println("u="+u+" z="+z+" beta="+beta+" theta="+theta
//                      +" tau="+tau+" phi="+phi
////                    +" beta*theta="+beta*theta
//                    );
                    wUnRetweetProb +=
                      beta*
                      theta*
                      tau*
                      phi
                    ;
                }

                Set<Integer> upSet;
//                if (data.in_userGraph.containsKey(u))
                    upSet = new HashSet<>(data.in_userGraph.get(u));
//                else upSet.add(-1);

//                System.out.println(word+" p(b=1) components len(up)="+upSet.size());
                for (int up : upSet) {
                    for (int z=0; z<cmdOption.znum; z++)  {
                        double beta = beta(u, Constant.INHERITANCE, data);
                        double thetaPrime = thetaPrime(up, z, data);
                        double gamma = gamma(u, up, data);
                        double tau = tau(u, Constant.INHERITANCE, up, z, timeStamp, data);
                        double phi = phi(z, wIndex, data);
//                        System.out.println("u="+u+" up="+up+" z="+z+" beta="+beta
//                            +" gamma="+gamma+" thetaPrime="+thetaPrime
//                          +" tau="+tau+" phi="+phi
////                        +" beta*thetaPrime*gamma="+beta*thetaPrime*gamma
//                        );
                        wRetweetProb +=
                          beta*
                          gamma*
                          thetaPrime*
                          tau*
                          phi
                        ;
                    }
                }

                double beta = beta(u, Constant.INHERITANCE, data);

                /*   only compare p(b=1) p(b=0)*/
//                if ( wRetweetProb > wUnRetweetProb) b1count++;
                /*compare p(b=1) / ( p(b=1)+p(b=0) ) tokeTh*/
//                if ( (wRetweetProb / (wUnRetweetProb+wRetweetProb)) > tokenTh ) b1count++;
                /* naive bayes classify*/
                if ( wRetweetProb*tokenTh > (1.0-tokenTh)*wUnRetweetProb ) b1count++;
                else b0count++;

//                System.out.println(wRetweetProb+"\t"+wUnRetweetProb);
//                System.out.println(word+" p(b=1)="+wRetweetProb+" p(b=0)="+wUnRetweetProb);
            }
        }
        catch (IOException e){e.printStackTrace();}

//        System.out.println("b1count="+b1count+" b0count="+b0count+" beta="+beta(u, Constant.INNOTVATION, data, cmdOption));
//        return ((double)b1count/(b1count+b0count)) >= beta(u, Constant.INNOTVATION, data, cmdOption);

        /* compare b1count/(b1count+b0count) tweetTh */
        System.out.println();
        return ((double)b1count/(b1count+b0count)) >= tweetTh;

         /*plain majority vote*/
//        return b1count > b0count;
    }

    /**
     * retweet from who predication method
     * TODO accurrcy is very low
     * @param u
     * @param timeStamp
     * @param tweet
     * @param data
     * @param cmdOption
     * @return
     */
    public static int retweetFromPredicate(int u, int timeStamp, String tweet, final SampleData data, final CmdOption cmdOption){
        //remove url
        String t1 = tweet.replaceAll("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", "");
        //remove non-ascii
        String t2 = t1.replaceAll("[^\\x00-\\x7F]", "");

        TreeMap<Integer, Double> retweetProbMap = new TreeMap<Integer, Double>();

        try(TokenStream tokenizer = DataParsed.getStemmer().tokenStream("", new StringReader(t2))){
            while (tokenizer.incrementToken()){
                String token = tokenizer.reflectAsString(false);
                int termBegin = 5;
                int termEnd = token.indexOf(",");
                final String word = token.substring(termBegin, termEnd);

                if (!data.vocabularyMap.containsKey(word)) continue;

                int wIndex = (Integer) (data.vocabularyMap.get(word));

                for (int up : data.in_userGraph.get(u)) {
                    double wUpRetweetProb = 0;
                    for (int z = 0; z < cmdOption.znum; z++) {
                        double beta = beta(u, Constant.INHERITANCE, data);
                        double thetaPrime = thetaPrime(up, z, data);
                        double tau = tau(u, Constant.INHERITANCE, up, z, timeStamp, data);
                        double tauPrime = tauPrime(up, z, timeStamp, data);
                        double gamma = gamma(u, up, data);
                        double phi = phi(z, wIndex, data);
                        wUpRetweetProb +=
                          gamma
//                          tau *
//                          tauPrime *
//                          thetaPrime
//                          phi
                        ;
                    }

                    Util.update1MapIncreamental(retweetProbMap, up, Math.log(wUpRetweetProb));
                }
            }
        }
        catch (IOException e){e.printStackTrace();}

        if (!retweetProbMap.isEmpty()){
            Map.Entry<Integer, Double> maxEntry = Collections.max(retweetProbMap.entrySet(),
              new Comparator<Map.Entry<Integer, Double>>() {
                  @Override
                  public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                      if (o1.getValue()>o2.getValue()) return 1;
                      else if (o1.getValue()<o2.getValue()) return -1;
                      return 0;
                  }
              });

            return maxEntry.getKey();
        }

        return -1;
    }
	 
	 /*
	  * Calculate the right part of formula (16) and (17)
	  * p(b|...)
	  */
	 public static double beta(int uid, int b, final SampleData data) {
         CmdOption cmdOption = data.cmdOption;

         double num;
         double den = Util.get1Map(data.N_u_influenced, uid)
           + cmdOption.alphaLambdaInnov
           + cmdOption.alphaLambdaInherit;

         if (b == Constant.INNOTVATION) {//b=0, p(bi=innovation| b not i, time, o', z), alpha1 = alphaLambdaInnov
             num = Util.get2Map(data.N_ub_influenced, uid, b) + cmdOption.alphaLambdaInnov;

         } else {//b=1,p(bi=inherit| b not i, time, o', z), alpha2 = alphaLambdaInherit
             num = Util.get2Map(data.N_ub_influenced, uid, b) + cmdOption.alphaLambdaInherit;
         }

         return num / den;
    }
	 
	 /**
	  * p(z|xxx) for both influenced (b=0)
	  * @return
	  */
	 private static double theta(int uid, int z, final SampleData data) {
         CmdOption cmdOption = data.cmdOption;

		 double num = Util.get2Map(data.N_uz_innov_influenced, uid, z) + cmdOption.alphaTheta;
		 double den = Util.get2Map(data.N_ub_influenced, uid, Constant.INNOTVATION) + cmdOption.znum*cmdOption.alphaTheta;

		 return num / den;
    }
    /**
     *  p(z|xxx) for both influenced (b=1) and influencing
     * @param uprime
     * @param z
     * @param data
     * @return
     */
	 private static double thetaPrime(int uprime, int z, final SampleData data) {
         CmdOption cmdOption = data.cmdOption;

		 double num = Util.get2Map(data.N_upz_inher_influenced, uprime, z)+
           Util.get2Map(data.N_upz_influencing, uprime, z) + cmdOption.alphaTheta;
		 double den = Util.get1Map(data.N_up_inher_influenced, uprime) +
           Util.get1Map(data.N_up_influencing, uprime) + cmdOption.znum*cmdOption.alphaTheta;

		 return num / den;
    }

    /**
     *
     * @param uid
     * @param uprime
     * @param data
     * @return
     */
    public static double gamma(int uid, int uprime, SampleData data) {
        CmdOption cmdOption = data.cmdOption;

    	double num = Util.get2Map(data.N_uup_inher_influenced, uid, uprime) + cmdOption.alphaGamma;
    	double den = Util.get2Map(data.N_ub_influenced, uid, Constant.INHERITANCE) +
          data.getUprimeNumber(uid)*cmdOption.alphaGamma ;

        return num / den;
    }

    /**
     * probability for word
     * @param z
     * @param w
     * @param data
     * @return
     */
    private static double phi(int z, int w, SampleData data){
        CmdOption cmdOption = data.cmdOption;

        double num = Util.get2Map(data.N_zw_all, z, w) + cmdOption.alphaPhi;
        double den = Util.get1Map(data.N_z_all, z) + Constant.wordNum * cmdOption.alphaPhi;

        return num / den;
    }

    /**
     * p(t|xxx) for influenced users (b=0,1)
     * @param u
     * @param b
     * @param uprime
     * @param z
     * @param timestamp
     * @param data
     * @return
     */
    private static double tau(int u, int b, int uprime, int z, int timestamp, SampleData data){
        CmdOption cmdOption = data.cmdOption;
        double num  = 0.0;
        double den = 0;

        //time sliding window
        int timeWindow = 10;
        if (b==Constant.INNOTVATION){
            for (int tmpTime = timestamp-timeWindow; tmpTime < timestamp; tmpTime++)
                num += Util.get3Map(data.N_uzt_innov_influenced, u, z, tmpTime);

//            den = Util.get2Map(data.N_uz_innov_influenced, u, z) + Constant.timeNum*cmdOption.alphaTau;
            den = Util.get2Map(data.N_uz_innov_influenced, u, z);
        }
        else {
            for (int tmpTime = timestamp-timeWindow; tmpTime <= timestamp; tmpTime++)
                num += Util.get4Map(data.N_uupzt_inher_influenced, u, uprime, z, tmpTime);

            den = Util.get3Map(data.N_uupz_inher_influenced, u, uprime, z);
        }

        num += cmdOption.alphaTau;
        den += (data.N_utimeset.containsKey(u)?data.N_utimeset.get(u).size():1) * cmdOption.alphaTau;

        return num / den;
    }

    /**
     * p(t|xxx) for influencing users
     * @param uprime
     * @param z
     * @param timestamp
     * @param data
     * @return
     */
    private static double tauPrime(int uprime, int z, int timestamp, SampleData data){
        CmdOption cmdOption = data.cmdOption;
        int timeWindow = 10;
        double num = cmdOption.alphaTau;

        for (int tmpTime = timestamp-timeWindow; tmpTime<timestamp; tmpTime++)
            num  += Util.get3Map(data.N_upzt_influencing, uprime, z, tmpTime);

        double den = Util.get2Map(data.N_upz_influencing, uprime, z) +
          (data.N_uptimeset.containsKey(uprime)?data.N_uptimeset.get(uprime).size():1) * cmdOption.alphaTau;

        assert num>0 : "num="+num+" up="+uprime+" z="+z+" time="+timestamp;
        assert den>0 : "den="+den+" up="+uprime+" z="+z+" time="+timestamp;

        return num / den;
    }

    public static double inf_uupt(int u, int up, int timestamp, SampleData data){
        double inf = 0;
        CmdOption cmdOption = data.cmdOption;
        for(int z=0; z<cmdOption.znum; z++)
            inf += inf_uupzt(u, up, z, timestamp, data);
        return inf;
    }

    public static double inf_uupzt(int u, int up, int z, int timestamp, SampleData data){
        double inf = tau(u, Constant.INHERITANCE, up, z, timestamp, data) * gamma(u, up, data);
        return inf;
    }

    public static void estTemporalInf(String path, String type){
        SampleDataSer sampleDataSer = null;
        try {
//            dblp-v1-300-z10-seq_chain-0_iter-120.ser
            FileInputStream fin = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fin);
            sampleDataSer = (SampleDataSer) ois.readObject();
            ois.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        SampleData sampleData = new SampleData(sampleDataSer);

        if (type.equals("all"))
            for (int u : sampleData.in_userGraph.keySet())
                for (int up : sampleData.in_userGraph.get(u)){
                    Set<Integer> timeSet = sampleData.N_utimeset.get(u);
                    List<Integer> timeList = new ArrayList<>();
                    timeList.addAll(timeSet);
                    Collections.sort(timeList);

                    for (int t : timeList){
                        double temporal_inf = Probability.inf_uupt(u, up, t, sampleData);
                        System.out.println(u+"\t"+up+"\t"+t+"\t"+temporal_inf);
                    }
                }


        if (type.equals("topic"))
            for (int u : sampleData.in_userGraph.keySet())
                for (int up : sampleData.in_userGraph.get(u))
                    for (int z=0; z<sampleData.cmdOption.znum; z++){
                        Set<Integer> timeSet = sampleData.N_utimeset.get(u);
                        List<Integer> timeList = new ArrayList<>();
                        timeList.addAll(timeSet);
                        Collections.sort(timeList);

                        for (int t : timeList){
                            double temporal_inf = Probability.inf_uupzt(u, up, z, t, sampleData);
                            System.out.println(u+"\t"+up+"\t"+z+"\t"+t+"\t"+temporal_inf);
                        }
                    }
    }

    public static void main(String[] args){
        if (args.length!=2)
            System.out.println("java -cp infdetection.jar sampling.Probability .ser_FILE_PATH INFLUENCE_TYPE");
        Probability.estTemporalInf(args[0], args[1]);
    }

}
