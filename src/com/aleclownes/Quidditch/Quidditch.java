package com.aleclownes.Quidditch;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;

public class Quidditch extends JavaPlugin {
	
	private Field field;
	private TestField testField;
	private QuidditchScheduler schedule;
	private QuidditchListener listener;
	private FileConfiguration config;

	@Override
	public void onEnable(){
		config = getConfig();
		saveDefaultConfig();
		listener = new QuidditchListener(this);
		getServer().getPluginManager().registerEvents(listener, this);
		schedule = new QuidditchScheduler(this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, schedule, 20L, 1L);
		getLogger().info(this + " is now enabled!");
	}
	
	@Override
	public void onDisable(){
		if (field != null){
			field.replaceGoalBlocks();
		}
		getLogger().info(this + " is now disabled!");
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		//Qstart 100 50 Gryffindor RED Slytherin GREEN [referee name (optional)]
		if(cmd.getName().equalsIgnoreCase("Qstart")){
			//have permission Quidditch.QSTART
			Player player = null;
			if (sender instanceof Player){
				player = (Player) sender;
			}
			if (player != null){
				if (!player.hasPermission("Quidditch.QSTART")){
					sender.sendMessage("You do not have the permission required to start a game");
					return false;
				}
			}
			if (args.length < 6){
				return false;
			}
			if (args.length == 7){
				player = Bukkit.getPlayer(args[6]);
				if (player == null){
					sender.sendMessage(args[6] + " does not exist.");
					return false;
				}
			}
			if (player == null){
				sender.sendMessage("If this command is given through the terminal, a referee name must be included at the end");
				return false;
			}
			if (field != null){
				sender.sendMessage("A game is already in progress. You must use /Qend to end that game before you start another one.");
				return false;
			}
			double ns;
			double ew;
			try {
				ns = Double.parseDouble(args[0]);
				ew = Double.parseDouble(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage("North-South or East-West number is not correctly formatted. Aborting game creation.");
				return false;
			}
			ChatColor colorA;
			try {
				colorA = ChatColor.valueOf(args[3]);
			} catch (IllegalArgumentException e) {
				sender.sendMessage("Illegal color value for first team. First team's color will be white.");
				colorA = ChatColor.WHITE;
			}
			ChatColor colorB;
			try {
				colorB = ChatColor.valueOf(args[5]);
			} catch (Exception e) {
				sender.sendMessage("Illegal color value for second team. Second team's color will be white.");
				colorB = ChatColor.WHITE;
			}
			field = new Field(this, player, player.getLocation(), ns, ew, args[2], colorA, args[4], colorB);
			if (field.getTeamA().getKeeper() == null || field.getTeamB().getKeeper() == null){
				sender.sendMessage("There is not at least one person on each team. Aborting game creation.");
				destroyField();
				return false;
			}
			for (Player ply : field.getQuidditchPlayers()){
				ply.sendMessage("Keepers: left click on each of your three nets. After the last net is clicked, the game will start.");
			}
			field.getReferee().sendMessage("Keepers: left click on each of your three nets. After the last net is clicked, the game will start.");
			return true;
		}
		else if (cmd.getName().equalsIgnoreCase("Qend")){
			//have permission Quidditch.QEND
			//TODO debug
			testField = null;
			Player player = null;
			if (sender instanceof Player){
				player = (Player) sender;
			}
			if (field == null){
				sender.sendMessage("No game currently exists.");
				return true;
			}
			if (player != null){
				if (!player.hasPermission("Quidditch.QEND") && !player.equals(field.getReferee())){
					sender.sendMessage("You do not have the permission required to end a game");
					return false;
				}
			}
			Announcer.announce(this, null, Announcer.GAME_END, null);
			destroyField();
			return true;
		}
		else if (cmd.getName().equalsIgnoreCase("Test")){
			Player player = (Player) sender;
			double ns;
			double ew;
			try {
				ns = Double.parseDouble(args[0]);
				ew = Double.parseDouble(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage("North-South or East-West number is not correctly formatted. Aborting game creation.");
				return false;
			}
			testField = new TestField(this, player, player.getLocation(), ns, ew, "Gryffindor", ChatColor.RED, "Slytherin", ChatColor.GREEN);
			testField.getReferee().sendMessage("Test started.");
			return true;
		}
		return false;
	}
	
	public Field getField(){
		return field;
	}
	
	public TestField getTestField(){ return testField; }
	
	public FileConfiguration getConfiguration(){
		return config;
	}
	
	public void destroyField(){
		field.cleanUp();
		field = null;
	}

}
