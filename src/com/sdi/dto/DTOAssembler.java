package com.sdi.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sdi.dto.ComentariosUsuarioDto.Comentario;
import com.sdi.persistence.RatingDao;
import com.sdi.persistence.TripDao;
import com.sdi.persistence.UserDao;
import com.sdi.infrastructure.Factories;
import com.sdi.model.Application;
import com.sdi.model.Rating;
import com.sdi.model.Seat;
import com.sdi.model.SeatStatus;
import com.sdi.model.Trip;
import com.sdi.model.User;

public class DTOAssembler {

	public static SolicitudesDto generateSolicitudesDTO(Long idTrip) {

		SolicitudesDto scdto = new SolicitudesDto(Factories.persistence
				.newTripDao().findById(idTrip));

		Trip trip = Factories.persistence.newTripDao().findById(idTrip);

		List<User> admitidos = getAdmitidos(idTrip, trip.getPromoterId());
		List<User> excluidos = getExcluidos(idTrip, trip.getPromoterId());
		List<User> pendientes = getPendientes(idTrip, trip.getPromoterId());

		scdto.setAdmitidos(admitidos);
		scdto.setExcluidos(excluidos);
		scdto.setPendientes(pendientes);

		return scdto;

	}

	public static List<ViajeImplicadoDto> getViajesImplicadosDto(Long userId) {

		List<ViajeImplicadoDto> dto = new ArrayList<ViajeImplicadoDto>();
		
		Seat seat;

		for (Application aplication : Factories.persistence.newApplicationDao()
				.findByUserId(userId)) {

			seat = Factories.persistence.newSeatDao().findByUserAndTrip(userId,
					aplication.getTripId());

			if (seat == null)
				dto.add(new ViajeImplicadoDto(Factories.persistence
						.newTripDao().findById(aplication.getTripId()), null));
			else
				dto.add(new ViajeImplicadoDto(Factories.persistence
						.newTripDao().findById(aplication.getTripId()), seat
						.getStatus()));

		}

		return dto;

	}

	private static List<User> getPendientes(Long tripId, Long long1) {

		List<User> pendientes = new ArrayList<User>();
		List<Application> applications = Factories.persistence
				.newApplicationDao().appNotInSeat(tripId);

		for (Application application : applications) {

			if (!application.getUserId().equals(long1))
				pendientes.add(Factories.persistence.newUserDao().findById(
						application.getUserId()));

		}

		return pendientes;

	}

	private static List<User> getAdmitidos(Long idTrip, Long long1) {

		List<User> admitidos = new ArrayList<User>();
		List<Seat> seats = Factories.persistence.newSeatDao().findAll();
		for (Seat seat : seats) {
			if (seat.getTripId().equals(idTrip)
					&& !seat.getTripId()
							.equals(Factories.persistence.newTripDao()
									.findById(idTrip))
					&& seat.getStatus().equals(SeatStatus.ACCEPTED)
					&& !seat.getUserId().equals(long1)) {
				admitidos.add(Factories.persistence.newUserDao().findById(
						seat.getUserId()));
			}
		}
		return admitidos;
	}

	private static List<User> getExcluidos(Long idTrip, Long long1) {

		List<User> admitidos = new ArrayList<User>();
		List<Seat> seats = Factories.persistence.newSeatDao().findAll();
		for (Seat seat : seats) {
			if (seat.getTripId().equals(idTrip)
					&& !seat.getTripId()
							.equals(Factories.persistence.newTripDao()
									.findById(idTrip))
					&& seat.getStatus().equals(SeatStatus.EXCLUDED)
					&& !seat.getUserId().equals(long1)) {
				admitidos.add(Factories.persistence.newUserDao().findById(
						seat.getUserId()));
			}
		}
		return admitidos;
	}

	public static TripDto generateTripDto(Trip trip, User user) {

		User promotor = Factories.persistence.newUserDao().findById(
				trip.getPromoterId());
		TripDto tdao = new TripDto(trip, user);
		tdao.setIdPromotor(promotor.getId());
		tdao.setPromotorLogin(promotor.getLogin());
		tdao.setPromotorName(promotor.getName());
		tdao.setPromotorSurname(promotor.getSurname());
		Map<User, SeatStatus> usersAndStatus = findUsersAndStatusSeatBySeat(
				trip.getId(), promotor.getId());
		tdao.setPasajeros(getPasajeros(trip.getId(), usersAndStatus,
				promotor.getId()));
		tdao.setIdPromotor(promotor.getId());
		if (user != null)
			tdao.setIsInTrip(tdao.checkInTrip(user.getId()));

		return tdao;

	}

	public static ComentariosUsuarioDto generateComentariosUsuarioDto(User user) {
		List<Rating> ratings = Factories.persistence.newRatingDao()
				.findByUserId(user.getId());

		ComentariosUsuarioDto dto = new ComentariosUsuarioDto();
		dto.setUser(user);

		UserDao userDao = Factories.persistence.newUserDao();
		TripDao tripDao = Factories.persistence.newTripDao();

		User userComenta;
		Trip trip;

		for (Rating rating : ratings) {
			userComenta = userDao.findById(rating.getSeatFromUserId());
			trip = tripDao.findById(rating.getSeatAboutTripId());

			if (!dto.getComentarios().containsKey(userComenta))
				dto.getComentarios().put(userComenta,
						new ArrayList<Comentario>());

			dto.getComentarios()
					.get(userComenta)
					.add(new Comentario(trip, rating.getComment(), rating
							.getValue()));
		}

		return dto;
	}

	public static ComentarEnViajeDto generateComentarEnViajeDto(Long idTrip,
			Long idUser) {
		ComentarEnViajeDto dto = new ComentarEnViajeDto(Factories.persistence
				.newTripDao().findById(idTrip));

		UserDao userDao = Factories.persistence.newUserDao();
		RatingDao ratingDao = Factories.persistence.newRatingDao();

		List<Seat> seats = Factories.persistence.newSeatDao().findByTripId(
				idTrip);

		for (Seat seat : seats) {
			if (!seat.getUserId().equals(idUser)
					&& seat.getStatus().equals(SeatStatus.ACCEPTED)
					&& ratingDao.findByAboutFrom(seat.getUserId(), idTrip,
							idUser, idTrip) == null)
				dto.getParticipantes().add(userDao.findById(seat.getUserId()));
		}

		return dto;

	}

	private static List<PasajeroInfoDto> getPasajeros(Long idViaje,
			Map<User, SeatStatus> usuarios, Long idPromotor) {

		List<PasajeroInfoDto> pasajeros = new ArrayList<PasajeroInfoDto>();

		for (User usuario : usuarios.keySet())
			pasajeros.add(new PasajeroInfoDto(usuario.getId(), usuario
					.getLogin(), usuario.getName(), usuario.getSurname(),
					usuarios.get(usuario)));

		return pasajeros;

	}

	private static Map<User, SeatStatus> findUsersAndStatusSeatBySeat(Long id,
			Long promoterID) {

		List<Seat> seats = Factories.persistence.newSeatDao().findAll();
		Map<User, SeatStatus> map = new HashMap<User, SeatStatus>();
		Set<Long> idUsers = new HashSet<Long>();

		for (Seat seat : seats) {

			if (seat.getTripId().equals(id)
					&& !seat.getUserId().equals(promoterID)) {
				map.put(Factories.persistence.newUserDao().findById(
						seat.getUserId()), seat.getStatus());
				idUsers.add(seat.getUserId());
			}
		}

		List<Application> applications = Factories.persistence
				.newApplicationDao().findAll();

		for (Application application : applications) {

			if (application.getTripId().equals(id)
					&& !application.getUserId().equals(promoterID)
					&& !idUsers.contains(application.getUserId())) {
				map.put(Factories.persistence.newUserDao().findById(
						application.getUserId()), null);

			}

		}

		return map;

	}

}
