package com.aleclownes.Quidditch;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class QuidditchListener implements Listener {

	Quidditch p;

	public QuidditchListener(Quidditch p) {
		this.p = p;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerMove(PlayerMoveEvent event){
		Player player = event.getPlayer();
		Field field = p.getField();
		if (field != null){
			if (field.getQuidditchPlayers().contains(player)){
				Team team = field.getTeam(player);
				if (team.getKeeper().equals(player)){
					if (!field.isInsideHalf(player.getLocation(), team)){
						player.setVelocity(team.getCenter().toVector().subtract(player.getLocation().toVector()).normalize().setY(player.getVelocity().getY()));
					}
				}
				else if (!field.isInside(player.getLocation())){
					player.setVelocity(field.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().setY(player.getVelocity().getY()));
				}
			}
		}
	}

	/**This finds out when a player is throwing a quaffle, hitting a bludger, or catching a snitch
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLeftClick(PlayerInteractEvent event){
		if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK){
			return;
		}
		Field f = p.getField();
		if (f == null){
			return;
		}
		Player player = event.getPlayer();
		Team team = f.getTeam(player);
		if (!f.isStarted()){
			if (team.getKeeper().equals(player) && event.getAction() == Action.LEFT_CLICK_BLOCK){
				//keepers designating goals
				Material goalMat = Material.getMaterial(p.getConfiguration().getString("goalDesignator"));
				if (event.getClickedBlock().getType() == goalMat){
					f.addGoalBlocks(team, event.getClickedBlock());
					f.incGoals();
					for (Player qplayer : f.getQuidditchPlayers()){
						qplayer.sendMessage("Goal " + f.getGoals() + " out of 6 registered");
					}
					f.getReferee().sendMessage("Goal " + f.getGoals() + " out of 6 registered");
					if (f.getGoals() == 6){
						f.setStarted(true);
						for (Player qplayer : f.getQuidditchPlayers()){
							qplayer.sendMessage("All goals have been registered! The referee will now throw the quaffle to start the game!");
						}
						f.getReferee().sendMessage("All goals have been registered! The referee will now throw the quaffle to start the game!");
					}
				}
			}
			//returns if the field is not started yet
			return;
		}
		//starting the game with the referee or penalizing
		if (f.getReferee().equals(player)){
			Quaffle quaffle = f.getQuaffle();
			if (quaffle.isCarried() && quaffle.getLastTouched().equals(player)){
				quaffle.toss();
				Announcer.announce(p, null, Announcer.GAME_START, null);
			}
			else if ((f.getOrientedNS() && player.getEyeLocation().getDirection().getZ()<0)||(!f.getOrientedNS() && player.getEyeLocation().getDirection().getX()>0)){
				//teamA penalty
				f.getTeamA().setScore(f.getTeamA().getScore()-5);
				Announcer.announce(p, f.getTeamA().getColor(), Announcer.PENALTY, f.getTeamA().getName());
			}
			else if ((f.getOrientedNS() && player.getEyeLocation().getDirection().getZ()>0)||(!f.getOrientedNS() && player.getEyeLocation().getDirection().getX()<0)){
				//teamB penalty
				f.getTeamB().setScore(f.getTeamB().getScore()-5);
				Announcer.announce(p, f.getTeamB().getColor(), Announcer.PENALTY, f.getTeamB().getName());
			}
		}
		if (team.getChasers().contains(player) || team.getKeeper().equals(player)){
			Quaffle quaffle = f.getQuaffle();
			if (!quaffle.isCarried()){
				//catching the quaffle
				if (player.getLocation().distance(quaffle.getLocation()) <= 4){
					quaffle.setCarried(true);
					quaffle.setLastTouched(player);
					Announcer.announce(p, team.getColor(), Announcer.QUAFFLE_CATCH, player.getDisplayName());
				}
			}
			else{
				//throwing the quaffle
				if (quaffle.getLastTouched().equals(player)){
					quaffle.toss();
					Announcer.announce(p, team.getColor(), Announcer.QUAFFLE_THROW, player.getDisplayName());
				}
			}
		}
		if (team.getBeaters().contains(player)){
			//hitting bludger
			List<Bludger> bludgers = f.getBludgers();
			for (Bludger bludger : bludgers){
				if (bludger.getLocation().distance(player.getLocation()) <= 4){
					bludger.hit(player);
					Announcer.announce(p, team.getColor(), Announcer.BLUDGER_BAT, player.getDisplayName());
				}
			}
		}
		if (team.getSeeker().equals(player)){
			//catching snitch
			Snitch snitch = f.getSnitch();
			if (player.getLocation().distance(snitch.getLocation()) <= 4){
				team.setScore(team.getScore()+150);
				Announcer.announce(p, team.getColor(), Announcer.GAME_END, player.getDisplayName() + " from " + team.getName());
				Announcer.announce(p, null, Announcer.GAME_END, null);
				p.destroyField();
			}
		}
	}

	/**This finds out when a player is stealing the quaffle from another player
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerHitPlayer(EntityDamageByEntityEvent event){
		Field f = p.getField();
		if (f == null || !f.isStarted()){
			return;
		}
		Player damager;
		Player victim;
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player){
			damager = (Player) event.getDamager();
			victim = (Player) event.getEntity();
		}
		else{
			return;
		}
		if (!f.getQuidditchPlayers().contains(damager) || !f.getQuidditchPlayers().contains(victim)){
			return;
		}
		Team team;
		if (f.getTeamA().contains(damager)){
			team = f.getTeamA();
		}
		else{
			team = f.getTeamB();
		}
		if (team.getChasers().contains(damager) || team.getKeeper().equals(damager)){
			Quaffle quaffle = f.getQuaffle();
			if (quaffle.isCarried() && quaffle.getLastTouched().equals(victim)){
				quaffle.setLastTouched(damager);
				ChatColor c;
				if (f.getTeamA().contains(damager)){
					c = f.getTeamA().getColor();
				}
				else{
					c = f.getTeamB().getColor();
				}
				Announcer.announce(p, c, Announcer.QUAFFLE_CATCH, damager.getDisplayName());
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLeftClickTestField(PlayerInteractEvent event){
		if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK){
			return;
		}
		TestField f = p.getTestField();
		if (f == null){
			return;
		}
		Player player = event.getPlayer();
		TestQuaffle quaffle = f.getQuaffle();
		if (!quaffle.isCarried()){
			//catching the quaffle
			if (player.getLocation().distance(quaffle.getLocation()) <= 4){
				quaffle.setCarried(true);
				quaffle.setLastTouched(player);
			}
		}
		else{
			//throwing the quaffle
			if (quaffle.getLastTouched().equals(player)){
				quaffle.toss();
			}
		}
		List<TestBludger> bludgers = f.getTestBludgers();
		for (TestBludger bludger : bludgers){
			if (bludger.getLocation().distance(player.getLocation()) <= 4){
				bludger.hit(player);
			}
		}
		TestSnitch snitch = f.getSnitch();
		if (player.getLocation().distance(snitch.getLocation()) <= 4){
			System.out.println("Snitch caught!");
			player.sendMessage("Snitch Caught!");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerMoveTestField(PlayerMoveEvent event){
		Player player = event.getPlayer();
		TestField field = p.getTestField();
		if (field != null){
			if (field.getReferee().equals(player)){
				if (!field.isInside(player.getLocation())){
					player.setVelocity(field.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().setY(player.getVelocity().getY()));
				}
			}
		}
	}

}
