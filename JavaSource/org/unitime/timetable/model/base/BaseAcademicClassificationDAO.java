/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model.base;

import java.util.List;

import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.AcademicClassificationDAO;

public abstract class BaseAcademicClassificationDAO extends _RootDAO<AcademicClassification,Long> {

	private static AcademicClassificationDAO sInstance;

	public static AcademicClassificationDAO getInstance() {
		if (sInstance == null) sInstance = new AcademicClassificationDAO();
		return sInstance;
	}

	public Class<AcademicClassification> getReferenceClass() {
		return AcademicClassification.class;
	}

	@SuppressWarnings("unchecked")
	public List<AcademicClassification> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from AcademicClassification x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
