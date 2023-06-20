package dejt.kaku;

public class DoubleHolder {
	
	private double in;
	
	public DoubleHolder() {
		this(0.0D);
	}
	
	public DoubleHolder(double in) {
		this.in = in;
	}
	
	public void incrementBy(double incr) {
		this.in += incr;
	}
	
	public double get() {
		return this.in;
	}

}
