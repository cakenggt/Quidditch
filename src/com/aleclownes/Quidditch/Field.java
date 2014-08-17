package com.aleclownes.Quidditch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class Field {

	private Quidditch p;
	private World world;
	private double x;
	private double y;
	private double z;
	private double ns;
	private double ew;
	private boolean orientedNS;
	private Team teamA;
	private Team teamB;
	private Player referee;
	private Quaffle quaffle;
	private List<Bludger> bludgers = new ArrayList<Bludger>();
	private Snitch snitch;
	private boolean started;
	private int goals;
	private Map<UUID, Location> spawnLocMap = new HashMap<UUID, Location>();

	public Field(Quidditch p, Player referee, Location location, double ns, double ew, String nameA, ChatColor colorA, String nameB, ChatColor colorB) {
		this.p = p;
		this.referee = referee;
		world = location.getWorld();
		x = location.getX();
		y = location.getY();
		z = location.getZ();
		this.ns = ns;
		this.ew = ew;
		orientedNS = ns >= ew;
		Location aCenter;
		Location bCenter;
		if (orientedNS){
			aCenter = new Location(world, x, y, z-(ns/4));
			bCenter = new Location(world, x, y, z+(ns/4));
		}
		else{
			aCenter = new Location(world, x+(ns/4), y, z);
			bCenter = new Location(world, x-(ns/4), y, z);
		}
		this.teamA = new Team(nameA, colorA, aCenter);
		this.teamB = new Team(nameB, colorB, bCenter);
		findPlayers();
		setSpawnLoc();
		started = false;
		quaffle = new Quaffle(p, referee);
		Bludger b1 = new Bludger(p, referee);
		Bludger b2 = new Bludger(p, referee);
		b1.setOtherBludger(b2);
		bludgers.add(b1);
		bludgers.add(b2);
		snitch = new Snitch(p, referee);
		goals = 0;
	}

	public void replaceGoalBlocks(){
		Material goalMat = Material.getMaterial(p.getConfiguration().getString("goalDesignator"));
		for (Block block : teamA.getGoal()){
			block.setType(goalMat);
		}
		for (Block block : teamB.getGoal()){
			block.setType(goalMat);
		}
	}

	private void findPlayers(){
		List<Player> playersA = new ArrayList<Player>();
		List<Player> playersB = new ArrayList<Player>();
		for (Player ply : world.getPlayers()){
			if (ply.equals(referee)){
				continue;
			}
			if (isInside(ply.getLocation())){
				if (orientedNS){
					if (ply.getLocation().getZ() < z){
						playersA.add(ply);
					}
					else{
						playersB.add(ply);
					}
				}
				else{
					if (ply.getLocation().getX() > x){
						playersA.add(ply);
					}
					else{
						playersB.add(ply);
					}
				}
			}
		}
		final Location center = new Location(world, x, y, z);
		Collections.sort(playersA, new Comparator<Player>() {

			public int compare(Player a, Player b) {
				//we want to sort in descending order
				double distanceA = a.getLocation().distance(center);
				double distanceB = b.getLocation().distance(center);
				if (distanceA > distanceB){
					return -1;
				}
				else if (distanceA == distanceB){
					return 0;
				}
				else{
					return 1;
				}
			}
		});
		Collections.sort(playersB, new Comparator<Player>() {

			public int compare(Player a, Player b) {
				//we want to sort in descending order
				double distanceA = a.getLocation().distance(center);
				double distanceB = b.getLocation().distance(center);
				if (distanceA > distanceB){
					return -1;
				}
				else if (distanceA == distanceB){
					return 0;
				}
				else{
					return 1;
				}
			}
		});
		//now the lists are sorted from farthest to nearest
		for (Player ply : playersA){
			if (teamA.getKeeper() == null){
				teamA.setKeeper(ply);
				ply.sendMessage("You are the keeper for " + teamA.getName());
			}
			else if (teamA.getSeeker() == null){
				teamA.setSeeker(ply);
				ply.sendMessage("You are the seeker for " + teamA.getName());
			}
			else if (teamA.getBeaters().size() < 2){
				teamA.getBeaters().add(ply);
				ply.sendMessage("You are a beater for " + teamA.getName());
			}
			else {
				teamA.getChasers().add(ply);
				ply.sendMessage("You are a chaser for " + teamA.getName());
			}
		}
		for (Player ply : playersB){
			if (teamB.getKeeper() == null){
				teamB.setKeeper(ply);
				ply.sendMessage("You are the keeper for " + teamB.getName());
			}
			else if (teamB.getSeeker() == null){
				teamB.setSeeker(ply);
				ply.sendMessage("You are the seeker for " + teamB.getName());
			}
			else if (teamB.getBeaters().size() < 2){
				teamB.getBeaters().add(ply);
				ply.sendMessage("You are a beater for " + teamB.getName());
			}
			else {
				teamB.getChasers().add(ply);
				ply.sendMessage("You are a chaser for " + teamB.getName());
			}
		}
	}

	public boolean isInside(Location loc){
		return Math.pow((loc.getZ() - z)/(ns/2), 2) + Math.pow((loc.getX() - x)/(ew/2), 2) <= 1;
	}

	/**Returns whether or not the location is inside the specified team's half of the field
	 * @param loc - The location to be checked
	 * @param team - team to check
	 * @return - True if yes
	 */
	public boolean isInsideHalf(Location loc, Team team){
		if (!isInside(loc)){
			return false;
		}
		if (teamA.equals(team)){
			if (orientedNS){
				return loc.getZ()<z;
			}
			else{
				return loc.getX()>x;
			}
		}
		else{
			if (orientedNS){
				return loc.getZ()>z;
			}
			else{
				return loc.getX()<x;
			}
		}
	}

	/**Returns a list of players with the field if it is grown.
	 * @param growth - the amount to add to each axis
	 * @return list of players
	 */
	public List<Player> getAnnouncementRecipients(int growth){
		List<Player> recipients = new ArrayList<Player>();
		for (Player player : world.getPlayers()){
			if (Math.pow((player.getLocation().getZ() - z)/((ns/2)+growth), 2) + Math.pow((player.getLocation().getX() - x)/((ew/2)+growth), 2) <= 1){
				recipients.add(player);
			}
		}
		return recipients;
	}

	public World getWorld(){
		return world;
	}

	public Set<Player> getQuidditchPlayers(){
		Set<Player> quidditchPlayers = new HashSet<Player>();
		quidditchPlayers.addAll(teamA.getPlayers());
		quidditchPlayers.addAll(teamB.getPlayers());
		return quidditchPlayers;		
	}

	/**Get the team to the north or east
	 * @return Team
	 */
	public Team getTeamA(){
		return teamA;
	}

	/**Get the team to the south or west
	 * @return Team
	 */
	public Team getTeamB(){
		return teamB;
	}

	public boolean isOrientedNS(){
		return orientedNS;
	}

	public Player getReferee(){
		return referee;
	}

	public List<Ball> getBalls(){
		List<Ball> balls = new ArrayList<Ball>();
		balls.add(quaffle);
		balls.addAll(bludgers);
		balls.add(snitch);
		return balls;
	}

	public Quaffle getQuaffle(){
		return quaffle;
	}

	public List<Bludger> getBludgers(){
		return bludgers;
	}

	public Snitch getSnitch(){
		return snitch;
	}

	public Team getTeam(Player player){
		if (teamA.contains(player)){
			return teamA;
		}
		else {
			return teamB;
		}
	}

	public boolean getOrientedNS(){
		return orientedNS;
	}

	public boolean isStarted(){
		return started;
	}

	public void setStarted(boolean started){
		this.started = started;
	}

	public int getGoals(){
		return goals;
	}

	public void incGoals(){
		goals ++;
	}

	/**
	 * This should only be used by the TestField class. This removes all of the balls generated by Field
	 */
	void removeBalls(){
		quaffle = null;
		bludgers.clear();
		snitch = null;
	}

	public Location getLocation(){
		return new Location(world, x, y, z);
	}

	public void addGoalBlocks(Team team, Block block){
		Material goalMat = Material.getMaterial(p.getConfiguration().getString("goalDesignator"));
		team.getGoal().add(block);
		block.setType(Material.AIR);
		Block newBlock = block.getRelative(BlockFace.UP);
		if (!team.getGoal().contains(newBlock) && newBlock.getType() == goalMat){
			addGoalBlocks(team, newBlock);
		}
		newBlock = block.getRelative(BlockFace.DOWN);
		if (!team.getGoal().contains(newBlock) && newBlock.getType() == goalMat){
			addGoalBlocks(team, newBlock);
		}
		newBlock = block.getRelative(BlockFace.EAST);
		if (!team.getGoal().contains(newBlock) && newBlock.getType() == goalMat){
			addGoalBlocks(team, newBlock);
		}
		newBlock = block.getRelative(BlockFace.WEST);
		if (!team.getGoal().contains(newBlock) && newBlock.getType() == goalMat){
			addGoalBlocks(team, newBlock);
		}
		newBlock = block.getRelative(BlockFace.NORTH);
		if (!team.getGoal().contains(newBlock) && newBlock.getType() == goalMat){
			addGoalBlocks(team, newBlock);
		}
		newBlock = block.getRelative(BlockFace.SOUTH);
		if (!team.getGoal().contains(newBlock) && newBlock.getType() == goalMat){
			addGoalBlocks(team, newBlock);
		}
	}

	public void setSpawnLoc(){
		for (Player ply : getQuidditchPlayers()){
			spawnLocMap.put(ply.getUniqueId(), ply.getBedSpawnLocation());
			ply.setBedSpawnLocation(getLocation(), true);
		}
	}

	public void resetSpawnLoc(){
		for (Player ply : getQuidditchPlayers()){
			ply.setBedSpawnLocation(spawnLocMap.get(ply.getUniqueId()));
		}
	}

	/**
	 * Called when a field is about to be removed.
	 */
	public void cleanUp(){
		replaceGoalBlocks();
		resetSpawnLoc();
	}

}
