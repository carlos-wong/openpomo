package it.unibz.pomodroid.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import android.util.Log;
import it.unibz.pomodroid.factories.ActivityFactory;
import it.unibz.pomodroid.persistency.DBHelper;
import it.unibz.pomodroid.persistency.User;

/**
 * @author tom
 *
 */
public class TrackTicketFetcher {
	
	public static void fetch (User user, DBHelper dbHelper){
		Vector<Integer> ticketIds;
		ticketIds = getTicketIds(user);
		// FIXME: added this to prevent a NullPointerException in case
		// of 0 tickets for the user - Daniel
		if (ticketIds==null)
			return;
		HashMap<String,String> attributes;
		Date deadLine = new Date();
	
		for (Integer ticketId : ticketIds) {
		   Integer[] id = {ticketId};
		   attributes = getTickets(user, id);
		   if (!(attributes.get("milestone").toString().equals(""))){
		     deadLine = getDeadLine(user, attributes.get("milestone").toString());
		   }
		   ActivityFactory.produce((int)ticketId, deadLine, attributes, dbHelper);
		}
		Log.i("TrackTicketFetcher", "Tickets are now in the DB");
	}
	
	public static Vector<Integer> getTicketIds (User user){
		String[] params = {"status!=closed"};
		Object[] result = XmlRpcClient.fetchMultiResults(user.getTracUrl(),user.getTracUsername(),user.getTracPassword(), "ticket.query",params);
		if (result != null){
			Vector<Integer> ticketsIds = new Vector<Integer>();
			for (Object i : result)
				ticketsIds.addElement(new Integer(i.toString()));
			return ticketsIds;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static HashMap<String,String> getTickets(User user, Integer[] id){
		Object[] ticket;
		HashMap<String,String> attributes;
		ticket = XmlRpcClient.fetchMultiResults(user.getTracUrl(),user.getTracUsername(),user.getTracPassword(), "ticket.get",id);
		attributes = (HashMap<String,String>) ticket[3];
		return attributes;
	}
	
	/**
	 * @param user
	 * @param milestoneId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Date getDeadLine (User user, String milestoneId){
		Date result;
		Object dataObject;
		String params[] = {milestoneId};
		Object milestone = XmlRpcClient.fetchSingleResult(user.getTracUrl(),user.getTracUsername(),user.getTracPassword(), "ticket.milestone.get",params);
		dataObject = ((HashMap<String, Object>) milestone).get("due");
		 if (dataObject.equals(0))
		   result = new Date();
		 else
		   result = (Date) dataObject;
		return result;
	}
}