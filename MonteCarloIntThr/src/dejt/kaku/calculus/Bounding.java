package dejt.kaku.calculus;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.ToDoubleFunction;

public class Bounding {
	
	private double h_x, l_x, l_y, h_y;
    private final Integrator<Double> integrator;
    private final int step;
    private FunctionType type;

    public Bounding(double h_x, double l_x, int step, Integrator<Double> integrator) {
        this.h_x = h_x;
        this.l_x = l_x;
        this.integrator = integrator;
        this.step = step;
        this.l_y = this.h_y = 0.0D;
    }

    public void swapAndManufactureY() throws IllegalStateException {    
        //check for errored range.
        if(this.h_x < this.l_x) {
            System.err.println("\nRange of x entered backwards. Fixing...");
            this.swap_x();
        }
        //manufacture y values. 
        double local_min = this.functionMinMax(FunctionQuantity.MIN);
        double local_max = this.functionMinMax(FunctionQuantity.MAX);
        System.out.println("\nLocal minimum is: " + local_min);
        System.out.println("Local maximum is: " + local_max);
        //check the type of function. 
        boolean cross_zero = local_min < 0 && local_max > 0;
        boolean under_dir = local_min < 0 && local_max <= 0;
        boolean over_dir = local_min > 0 && local_max > 0;
        //update y values and provide feedback on function type.
        if(cross_zero) {
        	this.l_y = local_min;
        	this.h_y = local_max;
        } else if(under_dir) {
        	this.l_y = 0.0D;
        	this.h_y = -local_min;
        } else if(over_dir) {
        	this.l_y = 0.0D;
        	this.h_y = local_max;
        } else {
        	throw new IllegalStateException("How did we get here?");
        }
        this.type = new FunctionType(under_dir, cross_zero);
    }

    public void swap_x() {
        double u_0 = this.l_x;
        this.l_x = this.h_x;
        this.h_x = u_0;
    }
    
    private double functionMinMax(FunctionQuantity type) {
        double q = 0;
        double t = this.h_x - this.l_x;
        ToDoubleFunction<Double> function = this.integrator.getFunction();
        q = function.applyAsDouble(this.l_x);
                for(double i = this.l_x; i < this.h_x; i += t / this.step) {
                    double currval = function.applyAsDouble(i);
                    if(type.get().test(currval, q)) {
                        q = currval;
                    }
                }
        return q;
    }

    private enum FunctionQuantity {
        MAX((u, t) -> u > t), MIN((u, t) -> u < t), X(null), Y(null);
        private BiPredicate<Double, Double> compare;

        FunctionQuantity(BiPredicate<Double, Double> compare) {
            this.compare = compare;
        }

        BiPredicate<Double, Double> get() {
            return this.compare;
        }
    }
    
    public FunctionType type() {
    	return this.type;
    }
    
    public List<Double> getBoundaries() {
    	return Arrays.asList(this.l_x, this.h_x, this.l_y, this.h_y);
    }
    
    public static record FunctionType(boolean isFlipped, boolean crosses_zero) {
    	
    }
    
}
