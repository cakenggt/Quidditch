package com.aleclownes.Quidditch;

public class QuidditchScheduler implements Runnable {

	Quidditch p;

	public QuidditchScheduler(Quidditch p) {
		this.p = p;
	}

	@Override
	public void run() {
		Field f = p.getField();
		if (f != null){
			for (Ball ball : f.getBalls()){
				ball.move(f);
				ball.action(f);
			}
		}
		//TODO debug
		TestField tf = p.getTestField();
		if (tf != null){
			for (Ball ball : tf.getBalls()){
				ball.move(tf);
				ball.action(tf);
			}
		}
	}

}
