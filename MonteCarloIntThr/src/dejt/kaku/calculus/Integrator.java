package dejt.kaku.calculus;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

public class Integrator<T extends Number> {
	
	private final ToDoubleFunction<T> function;
    private final BiPredicate<T, Double> checkLesser;
    private final BiPredicate<T, Double> checkMore;
    private final TripleF<T> randomise;
    private final QuintF<T> areaCalculator;
    private final Function<T, T> negate;
    private final T zero;

    /**
     *  For those who know what your randomise function will be. 
    */
    public Integrator(ToDoubleFunction<T> function, 
    		BiPredicate<T, Double> checkLesser, BiPredicate<T, Double> checkMore,
    		TripleF<T> randomise, 
    		QuintF<T> areaCalculator, Function<T, T> negate, Supplier<T> zero) {
        this.function = function;
        this.checkLesser = checkLesser;
        this.checkMore = checkMore;
        this.randomise = randomise;
        this.areaCalculator = areaCalculator;
        this.negate = negate;
        this.zero = zero.get();
    }

    public double calculate(int trials, BoundaryConditions<T> bounds) {
        T x;
        T y;
        int i = 0, hit = 0;
        //split up
        if(bounds.type.crosses_zero()) {
        	//function crosses zero
        	for(; i < trials; i++) {
                x = this.randomise.apply(bounds.x_0, bounds.x_1); //leave this
                y = this.randomise.apply(bounds.y_0, this.zero);
                if(this.checkMore.test(y, this.function.applyAsDouble(x))) {
                    hit++;
                }
            }
            double d0 = this.getTotalArea(hit, trials, 
            		this.zero, bounds.y_0, bounds.x_1, bounds.x_0);
            for(i = 0, hit = 0; i < trials; i++) {
                x = this.randomise.apply(bounds.x_0, bounds.x_1); //leave this
                y = this.randomise.apply(this.zero, bounds.y_1);
                if(this.checkLesser.test(y, this.function.applyAsDouble(x))) {
                    hit++;
                }
            }
            double d1 = this.getTotalArea(hit, trials, 
            		bounds.y_1, this.zero, bounds.x_1, bounds.x_0);
            return d1 - d0;
        } else if(bounds.type.isFlipped()) {
        	//function is completely under the y = 0 line. 
        	for(; i < trials; i++) {
                x = this.randomise.apply(bounds.x_0, bounds.x_1);
                y = this.randomise.apply(bounds.y_0, bounds.y_1);
                if(this.checkLesser.test(y, -this.function.applyAsDouble(x))) {
                    hit++;
                }
            }
            return -this.getTotalArea(hit, trials, 
            		bounds.y_1, bounds.y_0, bounds.x_1, bounds.x_0);
        } else {
        	//function is in the 1st and 2nd quadrant. The easiest. 
        	for(; i < trials; i++) {
                x = this.randomise.apply(bounds.x_0, bounds.x_1);
                y = this.randomise.apply(bounds.y_0, bounds.y_1);
                if(this.checkLesser.test(y, this.function.applyAsDouble(x))) {
                    hit++;
                }
            }
            return this.getTotalArea(hit, trials, 
            		bounds.y_1, bounds.y_0, bounds.x_1, bounds.x_0);
        }
        
    }
    
    public double getTotalArea(int inside, int totals, T a, T b, T c, T d) {
    	return this.areaCalculator.f((double)inside / (double)totals, a, b, c, d);
    }

    @FunctionalInterface
    public interface TripleF<F extends Number> extends BiFunction<F, F, F>{}
    
    @FunctionalInterface
    public interface QuintF<F extends Number>{
    	double f(double a, F b0, F b1, F b2, F b3);
    }

    public ToDoubleFunction<T> getFunction() {
        return this.function;	
    }
    
    public static record BoundaryConditions<T>(T x_0, T x_1, T y_0, T y_1, Bounding.FunctionType type) {
    	
    	public static BoundaryConditions<Double> getDoubleType(Bounding bounds) {
    		return new BoundaryConditions<Double>(bounds.getBoundaries().get(0), bounds.getBoundaries().get(1), 
    				bounds.getBoundaries().get(2), bounds.getBoundaries().get(3), 
    				bounds.type());
    		//return new BoundaryConditions<Double>(0.0D, 1.0D, 0.0D, 1.0D, bounds.type());
    	}
    	
    }

}
