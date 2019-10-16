package app;

import com.sun.istack.internal.Nullable;

import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.List;

/**
 * This class communicates with the database using named queries. This transactional class deals
 * with the Measurement entity.
 *
 * @see app.Measurement
 */
@Singleton
@Stateless
@Transactional
public class MeasurementStore extends Application {
	@PersistenceContext
	private EntityManager em;

	/**
	 * Return all measurements stored in database.
	 * @return  list of all measurements
	 */
	public List<Measurement> getAllMeasurements() {
		TypedQuery<Measurement> srq = em.createNamedQuery("getMeasurements", Measurement.class);
		return srq.getResultList();
	}

	/**
	 * Collects all measurement from database with the parameters defined.
	 * @param timestampFrom
	 * @param timestampTo
	 * @param device
	 * @return
	 */
	public List<Measurement> getMeasurementsFrom(long timestampFrom, long timestampTo, @Nullable String device) {
		TypedQuery<Measurement> srq = null;

		if (timestampTo == 0) {
			timestampTo = System.currentTimeMillis();
		}

		if (device != null) {
			// Device name is defined
			if (!device.isEmpty()) {
				// and device name is NOT empty, then add it to the named query
				// the timestamps, if not defined, has default values
				srq = em.createNamedQuery("getAllMeasurementsFromWithDevice", Measurement.class)
						.setParameter("cn", device)
						.setParameter("tf", timestampFrom)
						.setParameter("tt", timestampTo);
			}
		}

		if (srq == null) {
			// If device name is not defined (hence 'srq' being null), use another named query
			// that doesn't need a device name
			srq = em.createNamedQuery("getAllMeasurementsFrom", Measurement.class)
					.setParameter("tf", timestampFrom)
					.setParameter("tt", timestampTo);
		}

		// Get result as list
		List<Measurement> srList = srq.getResultList();

		if (srList == null) {
			// If no results were returned, create an empty list
			srList = new ArrayList<Measurement>(0);
		}

		return srList;
	}

	/**
	 * Add single (partially or fully defined) measurement to database.
	 * @param sr    measurement to commit to database
	 * @return      the same measurement, but with variables such as 'id' added
	 */
	public Measurement addMeasurement(Measurement sr) {
		// Add (partially filled) entity to the database (and fill in the rest)
		em.persist(sr);

		// Flush the filled data to the measurement instance
		em.flush();

		return sr;
	}

	/**
	 * Add measurement for the sake of testing the API.
	 */
	public void addDummyMeasurements() {
		// Define measurements
		Measurement s1 = new Measurement("test", 0, 2, 3,1,0);
		Measurement s2 = new Measurement("test2", 10, 27, 473,162,99);
		Measurement s3 = new Measurement("test3", 150, 277, 438,125,99);
		Measurement s4 = new Measurement("test", 1980, 287, 43,1,99);

		// Add measurements to database
		em.persist(s1);
		em.persist(s2);
		em.persist(s3);
		em.persist(s4);
	}

	/**
	 * Remove all measurement from database.
	 */
	public void removeAll() {
		Query srq = em.createQuery("DELETE FROM Measurements sr");
		srq.executeUpdate();
	}
}
