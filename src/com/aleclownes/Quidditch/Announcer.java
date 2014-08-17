package com.aleclownes.Quidditch;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**Contains the events that can be announced in a Quidditch game and the methods to announce them.
 * @author lownes
 *
 */
public enum Announcer {
	GAME_START,    //QuidditchListener
	QUAFFLE_THROW, //QuidditchListener
	QUAFFLE_CATCH, //QuidditchListener
	BLUDGER_BAT,   //QuidditchListener
	BLUDGER_IMPACT,//Bludger
	QUAFFLE_SCORE, //Quaffle
	PENALTY,       //QuidditchListener
	SNITCH_CATCH,  //QuidditchListener
	GAME_END;      //QuidditchListener and Quidditch
	
	public static void announce(Quidditch p, ChatColor c, Announcer type, String subject){
		Field f = p.getField();
		if (f == null){
			return;
		}
		List<Player> announceArea = f.getAnnouncementRecipients(50);
		String message = "";
		Team teamA = f.getTeamA();
		Team teamB = f.getTeamB();
		String scores = "\n" + teamA.getName() + ": " + teamA.getScore() + "\n" + teamB.getName() + ": " + teamB.getScore();
		switch (type){
		case GAME_START:
			message = "The Quidditch match has started!";
			break;
		case QUAFFLE_THROW:
			message = c + subject + " throws the quaffle!";
			break;
		case QUAFFLE_CATCH:
			message = c + subject + " now has the quaffle!";
			break;
		case BLUDGER_BAT:
			message = c + subject + " has given the bludger a mighty hit with their bat!";
			break;
		case BLUDGER_IMPACT:
			message = c + "Ouch! " + subject + " has just been hit by the bludger!";
			break;
		case QUAFFLE_SCORE:
			message = c + subject + " has just scored with the quaffle!" + scores;
			break;
		case PENALTY:
			message = c + "The referee has just penalized " + subject + "!" + scores;
			break;
		case SNITCH_CATCH:
			message = c + subject + " has just caught the snitch!" + scores;
			break;
		case GAME_END:
			if (teamA.getScore() > teamB.getScore()){
				message = teamA.getName() + " wins!";
			}
			else if (teamB.getScore() > teamA.getScore()){
				message = teamB.getName() + " wins!";
			}
			else{
				message = "It's a tie!";
			}
			message += scores;
			break;
		}
		if (!message.equals("")){
			for (Player receiver : announceArea){
				receiver.sendMessage(message);
			}
		}
	}

}
