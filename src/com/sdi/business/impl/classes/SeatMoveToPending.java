package com.sdi.business.impl.classes;

import com.sdi.infrastructure.Factories;
import com.sdi.model.Application;
import com.sdi.model.Seat;
import com.sdi.model.SeatStatus;
import com.sdi.model.Trip;
import com.sdi.persistence.SeatDao;

public class SeatMoveToPending {

	public void move(Long idUser, Long idTrip) {
		SeatDao seatDao = Factories.persistence.newSeatDao();
		Seat seat = seatDao.findByUserAndTrip(idUser, idTrip);
		
		if (seat != null) {
			seatDao.delete(seat.makeKey());
			Application app = new Application(idUser, idTrip);
			Factories.services.createApplicationService().save(app);
			if (seat.getStatus() != null && seat.getStatus().equals(SeatStatus.ACCEPTED))
				incrementAvailablePax(idTrip);
		}
	}
	
	private void incrementAvailablePax(Long idTrip) {
		Trip trip = Factories.services.createTripService().findTrip(idTrip);
		trip.setAvailablePax(trip.getAvailablePax() + 1);
		Factories.persistence.newTripDao().update(trip);
	}
}
