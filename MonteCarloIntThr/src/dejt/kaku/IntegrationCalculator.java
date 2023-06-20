package dejt.kaku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import dejt.kaku.calculus.Bounding;
import dejt.kaku.calculus.Integrator;

public class IntegrationCalculator implements Runnable {
	
	private final static int MIN_TRIALS = 4500;
	private final static int THREADS = 27;
	private List<Double> holder;
	private final Integrator<Double> subj;
	private final Bounding bound;
	private final int trial_cnt;
	private int count;
	
	public IntegrationCalculator(List<Double> holder, Integrator<Double> subj, Bounding bound, int trial_cnt, int count) {
		this.holder = holder;
		this.subj = subj;
		this.bound = bound;
		this.trial_cnt = trial_cnt;
		this.count = count;
	}
	
	public static void main(String[] args) throws IllegalArgumentException{
			Integrator<Double> subj = new Integrator<Double>(
	            p -> Math.pow(Math.E, -p * p / 2) * (p + 1.0D), 
			    //p -> 3 * p * p + 5 * p - 2,
	            (t1, t2) -> t1 <= t2, 
	            (t1, t2) -> t1 >= t2,
	            IntegrationCalculator::randomIntFromInterval,
	            (d0, u, w, x, y) -> d0 * (u - w) * (x - y),
	            p -> -p,
	            () -> 0.0D
	        );
	        double l_x, h_x;
	        int trial_cnt;
	        List<Double> holder = new ArrayList<>(THREADS);
	        List<Thread> threadlist = new ArrayList<>(THREADS);
	        Scanner scannerInput = new Scanner(System.in);
	        System.out.println("Enter lower bound x: ");
	        l_x = scannerInput.nextDouble();
	        System.out.println("Enter upper bound x: ");
	        h_x = scannerInput.nextDouble();
	        System.out.format("Chosen bounds are [%.4f] and [%.4f]", l_x, h_x);
	        Bounding bound = new Bounding(h_x, l_x, 5000, subj);
	        //check for errored range
	        bound.swapAndManufactureY();
	        System.out.println("\nTrials per thread: ");
	        trial_cnt = scannerInput.nextInt();
	        if(trial_cnt <= 4500) {
	            scannerInput.close();
	            throw new IllegalArgumentException("Error! Trial count must be more than " + MIN_TRIALS + " to get meaningful results!.");
	        }
	        scannerInput.close();
	        System.out.println("\n\n");
	        //now let's do threading. But first, let's see if my idea works at all
	        for(int i = 0; i < THREADS; i++) {
	        	Thread thread = new Thread(new IntegrationCalculator(holder, subj, bound, trial_cnt, i));
	        	thread.start();
	        	threadlist.add(thread);
	        }
	        //wait for all the threads above to finish. 
	        threadlist.forEach(thread -> {
				try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
	        DoubleHolder resultHolder = new DoubleHolder();
	        holder.forEach(d -> resultHolder.incrementBy(d));
	        ////System.out.println("Result from single thread: [" + trial_cnt + "]: ~" + res);
	        System.out.println("Result from n=" + THREADS + " thread(s): [" + trial_cnt + "]: ~" + resultHolder.get() / THREADS);
	}
	
	private static double randomIntFromInterval(double min, double max) { // min and max included 
		return Math.random() * (max - min) + min;
    }
	
	public void run() {
		//make sure that the arraylist is written to one at a time(threads are to line up!)
		synchronized(this.holder) {
			holder.add(subj.calculate(trial_cnt, Integrator.BoundaryConditions.getDoubleType(bound)));
		}
	}

	
}
