package com.aleclownes.Quidditch;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TestField extends Field {
	
	private TestQuaffle quaffle;
	private List<TestBludger> bludgers = new ArrayList<TestBludger>();
	private TestSnitch snitch;

	public TestField(Quidditch p, Player referee, Location location, double ns,
			double ew, String nameA, ChatColor colorA, String nameB,
			ChatColor colorB) {
		super(p, referee, location, ns, ew, nameA, colorA, nameB, colorB);
		quaffle = new TestQuaffle(p, referee);
		bludgers.add(new TestBludger(p, referee));
		bludgers.add(new TestBludger(p, referee));
		snitch = new TestSnitch(p, referee);
		removeBalls();
		setStarted(true);
	}
	
	@Override
	public List<Ball> getBalls(){
		List<Ball> balls = new ArrayList<Ball>();
		balls.add(quaffle);
		balls.addAll(bludgers);
		balls.add(snitch);
		return balls;
	}
	
	@Override
	public TestQuaffle getQuaffle(){
		return quaffle;
	}
	
	public List<TestBludger> getTestBludgers(){
		return bludgers;
	}
	
	@Override
	public TestSnitch getSnitch(){
		return snitch;
	}

}
