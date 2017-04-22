package sampling;

import util.Constant;
import util.Debugger;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: chu
 * Date: 11/5/13
 * Time: 1:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class InfluencedGibbsSampler implements Callable<String>{
    List<SampleElementInfluenced> obj_list;
    double llh = 0;
    SampleData call_sample_data;
    private boolean trainOrTest;

    /**
     * groupId is the partition id.
     */
    private long groupId;
    /**
     * after the thread begin to run, it is set to thread id.
     */
    private long threadId;

    public int avgOp;

    long blocked_time;
    long waiting_time;
    long cpu_time;

    public InfluencedGibbsSampler(int groupId, List<SampleElementInfluenced> obj_list, SampleData call_sample_data, boolean trainOrTest) {
        setGroupId(groupId);
        this.obj_list = obj_list;
        this.call_sample_data = call_sample_data;
        this.setTrainOrTest(trainOrTest);

        this.avgOp = 0;

        for (SampleElementInfluenced e : this.obj_list){
            int uid = e.u;
            this.avgOp += this.call_sample_data.getUprimeNumber(uid);
        }

        this.avgOp = this.avgOp / this.obj_list.size();
    }

    public void runSampling(){
        llh = 0;

        for (SampleElementInfluenced e : obj_list){
            call_sample_data.updCountInfluenced_uzbupwtime(e.u, e.z, e.b, e.uprime, e.w, e.time, -1);

            int[] normal_new_zbop;
            if (call_sample_data.cmdOption.seq.equals("n"))
                normal_new_zbop = Probability.getMAPInfluenced_zbaop_blocked(e.u, e.w, e.time, call_sample_data, call_sample_data.cmdOption, isTrainOrTest());
            else //if (call_sample_data.cmdOption.seq.equals("y"))
                normal_new_zbop = Probability.getMAPInfluenced_zbaop_sequential(e.u, e.b, e.uprime, e.z, e.w, e.time, call_sample_data, call_sample_data.cmdOption, isTrainOrTest());

            int newB = normal_new_zbop[0];
            int newZ = normal_new_zbop[1];
            int newUprime = normal_new_zbop[2];
            e.z = newZ;
            e.b = newB;
            e.uprime = newUprime;

            call_sample_data.updCountInfluenced_uzbupwtime(e.u, e.z, e.b, e.uprime, e.w, e.time, +1);

            double sampleProb = 0;
            //prob(b=0)
            double sampleProb0 = 0;
            for (int tmpZ=0; tmpZ< Constant.zNum; tmpZ++)
                sampleProb0 += Probability.influencedPosterior_bupztw(e.u, e.w, e.time, tmpZ, Constant.INNOTVATION,
                  -1, call_sample_data, call_sample_data.cmdOption);

            //prob(b=1)
            double sampleProb1 = 0;
            for (int up : call_sample_data.in_userGraph.get(e.u))
                for (int tmpZ=0; tmpZ< Constant.zNum; tmpZ++)
                    sampleProb1 += Probability.influencedPosterior_bupztw(e.u, e.w, e.time, tmpZ, Constant.INHERITANCE,
                      up, call_sample_data, call_sample_data.cmdOption);

            sampleProb = sampleProb0 + sampleProb1;

            llh += Math.log(sampleProb);
        }

    }

    @Override
    public String call() throws Exception {
        //set thread id
        if (call_sample_data.cmdOption.debug.equals("y"))
            System.out.println(Debugger.getCallerPosition()+"chain-"+this.call_sample_data.chainId
              +" group-id="+getGroupId()+" thread-id="+getThreadId()+" influenced begin to run");

        setThreadId(Thread.currentThread().getId());

        if (call_sample_data.cmdOption.debug.equals("y"))
            System.out.println(Debugger.getCallerPosition() + "chain-" + this.call_sample_data.chainId
              + " group-id=" + getGroupId() + " thread-id=" + getThreadId() + " influenced reset thread id");

        long[] beforeStamp = null;
        long[] afterStamp;

        if (this.call_sample_data.cmdOption.printThread.equals("y")){
            beforeStamp = this.printSamplerInfo(  call_sample_data.tmxb);
        }

        this.runSampling();

        if (this.call_sample_data.cmdOption.printThread.equals("y")){
            afterStamp = this.printSamplerInfo( call_sample_data.tmxb);

            blocked_time = (afterStamp[0]-beforeStamp[0]);
            waiting_time = (afterStamp[1]-beforeStamp[1]);
            cpu_time = (afterStamp[2]-beforeStamp[2]);

//            print withing thread running time
            System.out.println(Debugger.getCallerPosition()+" influenced chain-"+call_sample_data.chainId+" obj-"+this.groupId
              +" thread_id="+this.threadId
              +" blocked_time="+blocked_time+" waiting_time="+waiting_time+" cpu_time="+cpu_time);
        }

        return "TIM model influenced object "+getGroupId()+" sampling finish"; //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isTrainOrTest() {
        return trainOrTest;
    }

    public void setTrainOrTest(boolean trainOrTest) {
        this.trainOrTest = trainOrTest;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public int getChainSize(){
        return this.obj_list.size();
    }

    public long[] printSamplerInfo(ThreadMXBean tmxb){
//        if (getGroupId()==0)
//            setGroupId(Thread.currentThread().getId());
//        pin check whether thread start or not

        if (call_sample_data.cmdOption.debug.equals("y"))
            System.out.println(Debugger.getCallerPosition()+" chain-"+this.call_sample_data.chainId
              +" group-id="+getGroupId()+" thread-id="+getThreadId()
              +" enter influenced printSamplerInfo");

        while (getThreadId()==0){
//            if (call_sample_data.cmdOption.debug.equals("y"))
//                System.out.println(Debugger.getCallerPosition()+"chain-"+this.call_sample_data.chainId
//                  +"group-id="+getGroupId()+" influenced pin check thread id");
        }

        if (call_sample_data.cmdOption.debug.equals("y"))
            System.out.println(Debugger.getCallerPosition()+"chain-"+this.call_sample_data.chainId
              +"group-id="+getGroupId()+" thread-id="+getThreadId()
              +" finish influenced thread id check");

//        setThreadId(Thread.currentThread().getId());

        long[] time = new long[3];

        ThreadInfo threadInfo = tmxb.getThreadInfo(getThreadId());

        time[0] =  threadInfo.getBlockedTime();
        time[1] =  threadInfo.getWaitedTime();
        time[2] =  tmxb.getThreadUserTime(getThreadId())/1000000;

        return time;
    }
}
