package com.aleclownes.Quidditch;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class TestQuaffle extends Quaffle {

	public TestQuaffle(Quidditch p, Player referee) {
		super(p, referee);
		System.out.println("This is gravity " + halfGravity);
	}
	
	@Override
	public void action(Field f){
		if (isCarried()){
			setVector(new Vector(0, 0, 0));
			setLocation(getLastTouched().getLocation().add(0, 0.5, 0));
		}
		else{
			setVector(getVector().add(Quaffle.halfGravity));
		}
	}

}
