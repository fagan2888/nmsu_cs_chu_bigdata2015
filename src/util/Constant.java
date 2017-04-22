package util;


import java.util.HashSet;
import java.util.Set;

public class Constant {
	public final static int INNOTVATION= 0;
	public final static int INHERITANCE= 1;
	
	public final static boolean DeleteTemporaryFile = false;
	public final static String DefaultResultOutputFolder = "./output/";
	

	public static int wordNum;//done

    public static Set<Integer> timestampSet = new HashSet<>();
	
	public static int zNum;

    public final static String load_time = "load time";

    public final  static String init_sample_time = "initial sample time";

    public  final  static  String sample_time = "sample time";

    public final static String sample_sum_time = "sample sum time";

    public  final static String converge_check_time = "converge check time";
}
