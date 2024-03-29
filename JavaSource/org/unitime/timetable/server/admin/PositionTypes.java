/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.server.admin;

import java.text.DecimalFormat;


import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.PositionTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=position]")
public class PositionTypes implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pagePositionType(), MESSAGES.pagePositionTypes());
	}

	@Override
	@PreAuthorize("checkPermission('PositionTypes')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldReference(), FieldType.text, 160, 20, Flag.UNIQUE),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 60, Flag.UNIQUE),
				new Field(MESSAGES.fieldSortOrder(), FieldType.number, 80, 10, Flag.UNIQUE)
				);
		data.setSortBy(2, 0, 1);
		DecimalFormat df = new DecimalFormat("0000");
		for (PositionType position: PositionTypeDAO.getInstance().findAll()) {
			int used =
				((Number)hibSession.createQuery(
						"select count(f) from Staff f where f.positionType.uniqueId = :uniqueId")
						.setLong("uniqueId", position.getUniqueId()).uniqueResult()).intValue() +
				((Number)hibSession.createQuery(
						"select count(f) from DepartmentalInstructor f where f.positionType.uniqueId = :uniqueId")
						.setLong("uniqueId", position.getUniqueId()).uniqueResult()).intValue();
			Record r = data.addRecord(position.getUniqueId(), used == 0);
			r.setField(0, position.getReference());
			r.setField(1, position.getLabel());
			r.setField(2, df.format(position.getSortOrder()));
		}
		data.setEditable(context.hasPermission(Right.PositionTypeEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('PositionTypeEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (PositionType position: PositionTypeDAO.getInstance().findAll()) {
			Record r = data.getRecord(position.getUniqueId());
			if (r == null)
				delete(position, context, hibSession);
			else
				update(position, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('PositionTypeEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		PositionType position = new PositionType();
		position.setReference(record.getField(0));
		position.setLabel(record.getField(1));
		position.setSortOrder(Integer.valueOf(record.getField(2)));
		record.setUniqueId((Long)hibSession.save(position));
		ChangeLog.addChange(hibSession,
				context,
				position,
				position.getReference() + " " + position.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(PositionType position, Record record, SessionContext context, Session hibSession) {
		if (position == null) return;
		DecimalFormat df = new DecimalFormat("0000");
		if (ToolBox.equals(position.getReference(), record.getField(0)) &&
				ToolBox.equals(position.getLabel(), record.getField(1)) &&
				ToolBox.equals(df.format(position.getSortOrder()), record.getField(2))) return;
		position.setReference(record.getField(0));
		position.setLabel(record.getField(1));
		position.setSortOrder(Integer.valueOf(record.getField(2)));
		hibSession.saveOrUpdate(position);
		ChangeLog.addChange(hibSession,
				context,
				position,
				position.getReference() + " " + position.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	@PreAuthorize("checkPermission('PositionTypeEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(PositionTypeDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void delete(PositionType position, SessionContext context, Session hibSession) {
		if (position == null) return;
		ChangeLog.addChange(hibSession,
				context,
				position,
				position.getReference() + " " + position.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(position);
	}

	@Override
	@PreAuthorize("checkPermission('PositionTypeEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(PositionTypeDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}

}
