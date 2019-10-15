package app;

import com.sun.istack.internal.Nullable;

import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Singleton
@Stateless
public class SensorStore extends Application {
	@PersistenceContext
	private EntityManager em;

	public List<SensorReading> getReadings() {
		TypedQuery<SensorReading> srq = em.createNamedQuery("getAllReadings", SensorReading.class);
		return srq.getResultList();
	}

	public List<SensorReading> getReadingsFrom(long timestampFrom, long timestampTo, @Nullable String device) {
		TypedQuery<SensorReading> srq = null;

		if (device != null) {
			if (!device.isEmpty()) {
				srq = em.createNamedQuery("getAllReadingsFromWithDevice", SensorReading.class)
						.setParameter("cn", device)
						.setParameter("tf", timestampFrom)
						.setParameter("tt", timestampTo);
			}
		}

		if (srq == null) {
			srq = em.createNamedQuery("getAllReadingsFrom", SensorReading.class)
					.setParameter("tf", timestampFrom)
					.setParameter("tt", timestampTo);
		}

		List<SensorReading> srList = srq.getResultList();

		return (srList != null) ? srList : new ArrayList<SensorReading>();
	}

	public SensorReading addReading(SensorReading sr) {
		em.persist(sr);
		em.flush();

		return sr;
	}

	public void addReadings() {
		SensorReading s1 = new SensorReading("test", 0, 2, 3,1,0);
		SensorReading s2 = new SensorReading("test2", 10, 27, 473,162,99);
		SensorReading s3 = new SensorReading("test3", 150, 277, 438,125,99);
		SensorReading s4 = new SensorReading("test", 1980, 287, 43,1,99);

		em.persist(s1);
		em.persist(s2);
		em.persist(s3);
		em.persist(s4);
	}

	public boolean removeAll() {
		Query srq = em.createQuery("DELETE FROM SensorReadings sr");
		srq.executeUpdate();

		return true;
	}
}
