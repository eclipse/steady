package com.sap.psr.vulas.backend.util;

import java.util.Collection;

import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotNull;

import org.springframework.data.repository.CrudRepository;

import com.sap.psr.vulas.backend.model.ConstructId;

/**
 * {@link CrudRepository#findOne(java.io.Serializable)} only works for the primary key of the respective {@link Entity}.
 * All other "find" methods that can be specified in the extended interface only return {@link Collection}s of objects
 * that match the search criteria. This class works around this problem...
 *
 * @param <T>
 */
public class ResultSetFilter<T> {

	/**
	 * <p>findOne.</p>
	 *
	 * @return the single object contained in the given collection
	 * @throws {@link EntityNotFoundException} if the given collection is empty or contains multiple elements
	 * @param _collection a {@link java.util.Collection} object.
	 */
	public T findOne(@NotNull Collection<T> _collection) throws EntityNotFoundException {
		if(_collection==null || _collection.isEmpty()) {
			throw new EntityNotFoundException("Object not found");
		}
		else if(_collection.size()>1) {
			throw new EntityNotFoundException("Multiple objects found");
		}
		else {
			return _collection.iterator().next();
		}
	}
}
