/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.security.permissions;

import org.unitime.timetable.guice.context.UserContext;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.security.rights.Right;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

public class CurriculumPermissions {
	
	@RequestScoped
	public static class CanView extends SimpleSessionPermission {
		@Override
		public Right right() {
			return Right.CurriculumView;
		}
	}
	

	@RequestScoped
	public static class CanAdd extends SimpleDepartmentPermission {
		@Override
		public Right right() {
			return Right.CurriculumEdit;
		}
		
		@Override
		public boolean checkStatus(DepartmentStatusType status) {
			return status.canManagerEdit() || status.canOwnerEdit();
		}
	}
	
	@RequestScoped
	public static class CanEdit implements Permission<Curriculum> {
		@Inject UserContext context;
		@Inject CanAdd curriculumCanAdd;

		@Override
		public boolean check(Curriculum curriculum) {
			return curriculumCanAdd.check(curriculum.getDepartment(), right());
			/* // Instead of the following lines ...
			// Not authenticated -> no editing
			if (!context.isAuthenticated()) return false;
			
			// System administrator can always edit
			if (context.getRole().hasRight(Right.IsSystemAdmin)) return true;
			
			// For all other users, session check must pass
			Session session = (curriculum == null ? null : curriculum.getAcademicArea().getSession());
			if (session == null || !session.getUniqueId().equals(context.getAcademicSessionId()))
				return false;
			
			// Administrator can edit if the class is of a correct academic session
			if (context.getRole().hasRight(Right.IsAdmin)) return true;
			
			// Change the class edit right
			if (!context.getRole().hasRight(Right.CurriculumEdit)) return false;
			
			// Curriculum manager can edit all departments
			if (CurriculumMgrRole.class.isInstance(context.getRole())) return true;
			
			// Other users may only edit curricula of their department
			return context.hasDepartment(curriculum.getDepartment().getUniqueId());
			 */
		}
		
		public Right right() {
			return Right.CurriculumEdit;
		}

		@Override
		public Class<Curriculum> type() {
			return Curriculum.class;
		}
	}
	
	@RequestScoped
	public static class CanDelete extends CanEdit {
		
		@Override
		public Right right() {
			return Right.CurriculumDelete;
		}
	}
}