package sampling;

import java.io.Serializable;

/**
 * The element used to keep the sample for one influenced (citing)
 * object's one position (s) 
 * 
 * @author administrator
 *
 */
public class SampleElementInfluenced implements Comparable<SampleElementInfluenced>, Cloneable {

    public int u;
    public int time;
	public int w; //w
	public int z; //time
	public int b; //binary value, s
    public int uprime;
	
	public SampleElementInfluenced(int _u, int _time, int _t,  int _z, int _b, int _uprime)
	{
        u = _u;
        time = _time;
		w = _t;
		z = _z;
		b = _b;
        uprime = _uprime;
	}

    @Override
    public int compareTo(SampleElementInfluenced arg0) {
        if (u < arg0.u) return  (-1);
        else if (u > arg0.u) return 1;
        else {
            if (time < arg0.time) return (-1);
            else if (time > arg0.time) return 1;
            else {
                if(w < arg0.w) return (-1);
                else if (w > arg0.w) return 1;
                else{
                    if (z < arg0.z) return (-1);
                    else if (z > arg0.z) return 1;
                    else {
                        if( b < arg0.b) return (-1);
                        else if (b > arg0.b) return 1;
                        else return 0;
                    }
                }
            }
        }
    }
	
	public String toString()
	{
		String str = "[user"+u+"][time"+time+"][time"+ w +"][z"+z+"]"+"[b"+b+"][up"+uprime+"]";
		return str;
	}

	/**
	 * @return the w
	 */
	public int getW() {
		return w;
	}

	/**
	 * @param w the w to set
	 */
	public void setW(int w) {
		this.w = w;
	}

	/**
	 * @return the z
	 */
	public int getZ() {
		return z;
	}

	/**
	 * @param z the z to set
	 */
	public void setZ(int z) {
		this.z = z;
	}

	/**
	 * @return the b
	 */
	public int getB() {
		return b;
	}

	/**
	 * @param b the b to set
	 */
	public void setB(int b) {
		this.b = b;
	}

    public int getUprime() {
        return uprime;
    }

    public void setUprime(int uprime) {
        this.uprime = uprime;
    }

    public int getU() {
        return u;
    }

    public void setU(int u) {
        this.u = u;
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
