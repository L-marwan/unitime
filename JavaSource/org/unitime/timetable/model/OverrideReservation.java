/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model;

import java.util.Date;

import org.unitime.timetable.gwt.shared.ReservationInterface.OverrideType;
import org.unitime.timetable.model.base.BaseOverrideReservation;

public class OverrideReservation extends BaseOverrideReservation {
	private static final long serialVersionUID = 1L;

	public OverrideReservation() {
		super();
	}
	
	public OverrideType getOverrideType() {
		return getType() == null ? null : OverrideType.values()[getType()];
	}
	
	public void setOverrideType(OverrideType type) {
		setType(type == null ? null : type.ordinal());
	}
	
	@Override
	public boolean isExpired() {
		OverrideType type = getOverrideType();
		return (type == null || type.isCanHaveExpirationDate() ? super.isExpired() : type.isExpired());
	}
	
	@Override
	public Date getExpirationDate() {
		OverrideType type = getOverrideType();
		return (type == null || type.isCanHaveExpirationDate() ? super.getExpirationDate() : null);		
	}
}
