package com.sdi.presentation;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import com.sdi.business.TripService;
import com.sdi.infrastructure.Factories;
import com.sdi.model.AddressPoint;
import com.sdi.model.TripStatus;
import com.sdi.model.Waypoint;
import com.sdi.persistence.util.DateUtil;

@ManagedBean(name = "tripController")
@SessionScoped
public class BeanTripController {

	@ManagedProperty(value = "#{trip}")
	private BeanTrip trip;
	private AddressPoint departure;
	private AddressPoint destination;
	private Date dateSalida;
	private Date dateInscripcion;
	private Date dateLlegada;
	private String costeEstimado;
	private String plazasDisponibles;
	private String plazasMaximas;

	public String getCosteEstimado() {
		return costeEstimado;
	}

	public void setCosteEstimado(String costeEstimado) {
		this.costeEstimado = costeEstimado;
	}

	public String getPlazasDisponibles() {
		return plazasDisponibles;
	}

	public void setPlazasDisponibles(String plazasDisponibles) {
		this.plazasDisponibles = plazasDisponibles;
	}

	public String getPlazasMaximas() {
		return plazasMaximas;
	}

	public void setPlazasMaximas(String plazasMaximas) {
		this.plazasMaximas = plazasMaximas;
	}

	public Date getDateInscripcion() {
		return dateInscripcion;
	}

	public void setDateInscripcion(Date dateInscripcion) {
		this.dateInscripcion = dateInscripcion;
	}

	public Date getDateLlegada() {
		return dateLlegada;
	}

	public void setDateLlegada(Date dateLlegada) {
		this.dateLlegada = dateLlegada;
	}

	public BeanTrip getTrip() {
		return trip;
	}

	public void setTrip(BeanTrip trip) {
		this.trip = trip;
	}

	public AddressPoint getDeparture() {
		return departure;
	}

	public void setDeparture(AddressPoint departure) {
		this.departure = departure;
	}

	public Date getDateSalida() {
		return dateSalida;
	}

	public void setDateSalida(Date dateSalida) {
		this.dateSalida = dateSalida;
	}

	public AddressPoint getArrival() {
		return destination;
	}

	public void setArrival(AddressPoint arrival) {
		this.destination = arrival;
	}

	@PostConstruct
	public void init() {

		departure = new AddressPoint("", "", "", "", "", new Waypoint(0D, 0D));
		setArrival(new AddressPoint("", "", "", "", "", new Waypoint(0D, 0D)));
		System.out.println("BeanAlumnos - PostConstruct");
		// Buscamos el alumno en la sesión. Esto es un patrón factoría
		// claramente.
		trip = (BeanTrip) FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().get(new String("trip"));
		// si no existe lo creamos e inicializamos
		if (trip == null) {
			System.out.println("BeanTrip - No existia");
			trip = new BeanTrip();
			FacesContext.getCurrentInstance().getExternalContext()
					.getSessionMap().put("trip", trip);
		}
	}

	public void save() {

		TripService tservice;
		System.out.println("PASO POR AQUI");

		try {

			Double estimatedCost = Double.parseDouble(costeEstimado);
			Integer availablePax = Integer.parseInt(plazasDisponibles);
			Integer maxPax = Integer.parseInt(plazasMaximas);

			if (maxPax < availablePax) {

				return;
			}

			if (DateUtil.isAfter(dateInscripcion, dateSalida)
					|| DateUtil.isAfter(dateSalida, dateLlegada)) {
				return;
			}

			trip.setArrivalDate(dateLlegada);
			trip.setAvailablePax(availablePax);
			trip.setClosingDate(dateLlegada);
			trip.setDeparture(departure);
			trip.setDepartureDate(dateSalida);
			trip.setDestination(destination);
			trip.setEstimatedCost(estimatedCost);
			trip.setMaxPax(maxPax);
			trip.setStatus(TripStatus.OPEN);
			trip.setPromoterId(314L);
			
			tservice = Factories.services.createTripService();

			tservice.SaveTrip(trip);

		} catch (NumberFormatException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@PreDestroy
	public void end() {
		System.out.println("BeanAlumnos - PreDestroy");
	}

}