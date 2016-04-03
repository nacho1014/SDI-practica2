package com.sdi.business.impl.classes;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.sdi.infrastructure.Factories;
import com.sdi.model.Trip;
import com.sdi.model.TripStatus;
import com.sdi.persistence.TripDao;
import com.sdi.persistence.util.DateUtil;

public class TripsUpdateStatusTask {
	public static final int interval = 3000;
	
	private Timer timer = new Timer();
	
	class Task extends TimerTask {

		@Override
		public void run() {
			TripDao dao = Factories.persistence.newTripDao();
			List<Trip> trips = dao.findTravelsOpenAndClosed();
			
			boolean modified = false;
			for (Trip trip:trips) {
				if (!trip.getStatus().equals(TripStatus.CANCELLED)) {
					if (trip.getArrivalDate().before(DateUtil.today())) {
						trip.setStatus(TripStatus.DONE);
						modified = true;
					}
					else if (trip.getClosingDate().before(DateUtil.today())) {
						trip.setStatus(TripStatus.CLOSED);
						modified = true;
					}
				}
				
				if (modified)
					dao.update(trip);
				
				modified = false;
			}

		}
		
	}
	
	public TripsUpdateStatusTask() {
		timer.schedule(new Task(), 0, interval);
	}
	
}
