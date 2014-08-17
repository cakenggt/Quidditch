package com.aleclownes.Quidditch;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Team {
	
	private String name;
	private ChatColor color;
	private Location center;
	private int score = 0;
	private Set<Block> goal = new HashSet<Block>();
	private Set<Player> chasers = new HashSet<Player>();
	private Set<Player> beaters = new HashSet<Player>();
	private Player keeper;
	private Player seeker;

	public Team(String name, ChatColor color, Location center) {
		this.name = name;
		this.color = color;
		this.center = center;
	}
	
	public Set<Block> getGoal(){
		return goal;
	}
	
	public Set<Player> getChasers(){
		return chasers;
	}
	
	public Set<Player> getBeaters(){
		return beaters;
	}
	
	public Player getKeeper(){
		return keeper;
	}
	
	public void setKeeper(Player keeper){
		this.keeper = keeper;
	}
	
	public Player getSeeker(){
		return seeker;
	}
	
	public void setSeeker(Player seeker){
		this.seeker = seeker;
	}
	
	public Set<Player> getPlayers(){
		Set<Player> players = new HashSet<Player>();
		players.addAll(chasers);
		players.addAll(beaters);
		players.add(keeper);
		players.add(seeker);
		players.remove(null);
		return players;
	}
	
	public boolean contains(Player player){
		return getPlayers().contains(player);
	}
	
	public String getName(){
		return name;
	}
	
	public ChatColor getColor(){
		return color;
	}
	
	public int getScore(){
		return score;
	}
	
	public void setScore(int score){
		this.score = score;
	}
	
	public Location getCenter(){
		return center.clone();
	}

}
