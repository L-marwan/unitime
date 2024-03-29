/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.form;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SessionDAO;


/** 
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class DatePatternEditForm extends ActionForm {
	private static final long serialVersionUID = -929558620061783652L;
	private String iOp;
    private String iName;
    private Long iUniqueId;
    private String iType;
    private boolean iIsUsed;
    private boolean iIsDefault;
    private boolean iVisible;
    private DatePattern iDp = null;
    private Vector iDepartmentIds = new Vector();
    private Long iDepartmentId;
    private Vector iParentIds = new Vector();
    private Long iParentId;
    private Long iSessionId;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();
        
		if(iName==null || iName.trim().length()==0)
			errors.add("name", new ActionMessage("errors.required", ""));
		else {
			try {
				DatePattern pat = DatePattern.findByName(iSessionId,iName);
				if (pat!=null && !pat.getUniqueId().equals(iUniqueId))
					errors.add("name", new ActionMessage("errors.exists", iName));
			} catch (Exception e) {
				errors.add("name", new ActionMessage("errors.generic", e.getMessage()));
			}
        }
        
		if (getTypeInt()<0)
			errors.add("type", new ActionMessage("errors.required", ""));
		
		if (getTypeInt()!=DatePattern.sTypeExtended && !iDepartmentIds.isEmpty())
			errors.add("type", new ActionMessage("errors.generic", "Only extended pattern can contain relations with departments."));
		
		try {
			DatePattern dp = getDatePattern(request);
			if (getTypeInt() == DatePattern.sTypePatternSet) {
				if (dp.size()!=0)
					errors.add("type", new ActionMessage("errors.generic", "Alternative pattern set date pattern can not have any dates selected."));
				if (getParentIds() != null && !getParentIds().isEmpty()) {
					errors.add("type", new ActionMessage("errors.generic", "Alternative pattern set date pattern can not have a pattern set."));
				}
			} else {
				if (dp.size()==0)
					errors.add("pattern", new ActionMessage("errors.required", ""));
			}
			if (dp.getPattern().length() > 366)
				errors.add("pattern", new ActionMessage("errors.generic", "Date Patterns cannot contain more than 1 year."));
		} catch (Exception e) {}

        return errors;
	}
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null; iUniqueId = new Long(-1); iType = DatePattern.sTypes[0]; 
		iIsUsed = false; iVisible = false; iName = ""; iIsDefault = false;
		iDp = null; iDepartmentId = null; iDepartmentIds.clear(); iParentId = null; iParentIds.clear(); 
	}
	
	public void load(DatePattern dp) {
		if (dp==null) {
			reset(null, null);
			iOp = "Save";
			iVisible = true; iIsUsed = false; iIsDefault = false;
		} else {
			setName(dp.getName());
			setVisible(dp.isVisible().booleanValue());
			setIsUsed(dp.isUsed());
			setTypeInt(dp.getType().intValue());
			setUniqueId(dp.getUniqueId());
			setIsDefault(dp.isDefault());
			setSessionId(dp.getSession().getUniqueId());
			
			iParentIds.clear();
			TreeSet parents = new TreeSet(dp.getParents());
			for (Iterator i=parents.iterator();i.hasNext();) {
				DatePattern d = (DatePattern)i.next();
				iParentIds.add(d.getUniqueId());
			}
			iDepartmentIds.clear();
			TreeSet depts = new TreeSet(dp.getDepartments());
			for (Iterator i=depts.iterator();i.hasNext();) {
				Department d = (Department)i.next();
				iDepartmentIds.add(d.getUniqueId());
			}
			iOp = "Update";
		}
	}
	
	public void update(DatePattern dp, HttpServletRequest request, org.hibernate.Session hibSession) throws Exception {
		dp.setName(getName());
		dp.setVisible(new Boolean(getVisible()));
		dp.setType(new Integer(getTypeInt()));
		dp.setPatternAndOffset(request);
		
		HashSet oldParents = new HashSet(dp.getParents());
		for (Enumeration e=iParentIds.elements();e.hasMoreElements();) {
			Long parentId = (Long)e.nextElement();
			DatePattern d = (new DatePatternDAO()).get(parentId,hibSession);
			if (d==null) continue;
			if (oldParents.remove(d)) {
				//not changed -> do nothing
			} else {
				dp.getParents().add(d);				
				hibSession.saveOrUpdate(dp);				
			}
		}
		for (Iterator i=oldParents.iterator();i.hasNext();) {
			DatePattern d = (DatePattern)i.next();
			dp.getParents().remove(d);			
			hibSession.saveOrUpdate(d);
		}
		
		HashSet oldDepts = new HashSet(dp.getDepartments());
		for (Enumeration e=iDepartmentIds.elements();e.hasMoreElements();) {
			Long departmentId = (Long)e.nextElement();
			Department d = (new DepartmentDAO()).get(departmentId,hibSession);
			if (d==null) continue;
			if (oldDepts.remove(d)) {
				//not changed -> do nothing
			} else {
				dp.getDepartments().add(d);
				d.getDatePatterns().add(dp);
				hibSession.saveOrUpdate(d);
			}
		}
		for (Iterator i=oldDepts.iterator();i.hasNext();) {
			Department d = (Department)i.next();
			dp.getDepartments().remove(d);
			d.getDatePatterns().remove(dp);
			hibSession.saveOrUpdate(d);
		}
		hibSession.saveOrUpdate(dp);
	}
	
	public DatePattern create(HttpServletRequest request, org.hibernate.Session hibSession) throws Exception {
		DatePattern dp = new DatePattern();
		dp.setName(getName());
		dp.setSession(SessionDAO.getInstance().get(getSessionId(), hibSession));
		dp.setVisible(new Boolean(getVisible()));
		dp.setType(new Integer(getTypeInt()));
		dp.setPatternAndOffset(request);
		
		HashSet newParents = new HashSet();
		for (Enumeration e=iParentIds.elements();e.hasMoreElements();) {
			Long parentId = (Long)e.nextElement();
			DatePattern d = (new DatePatternDAO()).get(parentId,hibSession);
			if (d==null) continue;
			newParents.add(d);
		}
		dp.setParents(newParents);
		HashSet newDepts = new HashSet();
		for (Enumeration e=iDepartmentIds.elements();e.hasMoreElements();) {
			Long departmentId = (Long)e.nextElement();
			Department d = (new DepartmentDAO()).get(departmentId,hibSession);
			if (d==null) continue;
			newDepts.add(d);
		}
		dp.setDepartments(newDepts);
		hibSession.save(dp);
		for (Iterator i=newDepts.iterator();i.hasNext();) {
			Department d = (Department)i.next();
			d.getDatePatterns().add(dp);
			hibSession.saveOrUpdate(d);
		}
		setUniqueId(dp.getUniqueId());
		return dp;
	}
	
	public DatePattern saveOrUpdate(HttpServletRequest request, org.hibernate.Session hibSession) throws Exception {
		DatePattern dp = null;
		if (getUniqueId().longValue()>=0)
			dp = (new DatePatternDAO()).get(getUniqueId());
		if (dp==null)
			dp = create(request, hibSession);
		else 
			update(dp, request, hibSession);
		return dp;
	}
	
	public void delete(org.hibernate.Session hibSession) throws Exception {
		if (getUniqueId().longValue()<0) return;
		if (getIsUsed()) return;
		DatePattern dp = (new DatePatternDAO()).get(getUniqueId(), hibSession);
		for (Iterator i=dp.getDepartments().iterator();i.hasNext();) {
			Department d = (Department)i.next();
			d.getDatePatterns().remove(dp);
			hibSession.saveOrUpdate(d);
		}
		for (Iterator i=dp.findChildren().iterator();i.hasNext();) {
			DatePattern d = (DatePattern)i.next();
			d.getParents().remove(dp);
			hibSession.saveOrUpdate(d);
		}
		dp.getParents().clear();
		hibSession.saveOrUpdate(dp);
		hibSession.delete(dp);
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }
	public String getType() { return iType; }
	public void setType(String type) { iType = type; }
	public String[] getTypes() { return DatePattern.sTypes; }
	public int getTypeInt() {
		for (int i=0;i<DatePattern.sTypes.length;i++)
			if (DatePattern.sTypes[i].equals(iType)) return i;
		return -1;
	}
	public void setTypeInt(int type) { iType = (type<0?"":DatePattern.sTypes[type]); }
	public boolean getIsUsed() { return iIsUsed; }
	public void setIsUsed(boolean isUsed) { iIsUsed = isUsed; }
	public boolean getVisible() { return iVisible; }
	public void setVisible(boolean visible) { iVisible = visible; }
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	public boolean getIsDefault() { return iIsDefault; }
	public void setIsDefault(boolean isDefault) { iIsDefault = isDefault; }
	public Vector getDepartmentIds() { return iDepartmentIds; }
	public void setDepartmentIds(Vector departmentIds) { iDepartmentIds = departmentIds; }
	public Long getDepartmentId() { return iDepartmentId; }
	public void setDepartmentId(Long deptId) { iDepartmentId = deptId; }
	public Long getParentId() {return iParentId;}
	public void setParentId(Long parentId) {iParentId = parentId;}
	public Vector getParentIds() {return iParentIds;}
	public void setParentIds(Vector parentIds) {iParentIds = parentIds;}
	
	public Long getSessionId() { return iSessionId; }
	public void setSessionId(Long sessionId) { iSessionId = sessionId; }
	
	public DatePattern getDatePattern(HttpServletRequest request) throws Exception {
		if (getUniqueId()!=null) {
			iDp = (new DatePatternDAO()).get(getUniqueId());
			if (iDp!=null) iDp = (DatePattern)iDp.clone();
		}
		if (iDp==null) {
			iDp = new DatePattern();
		}
		if (iDp.getSession()==null) {
			iDp.setSession(SessionDAO.getInstance().get(getSessionId()));
		}
		if (request.getParameter("cal_select")!=null) {
			iDp.setName(getName());
			iDp.setVisible(new Boolean(getVisible()));
			iDp.setType(new Integer(getTypeInt()));
			iDp.setPatternAndOffset(request);
		}
		return iDp;
	}
}
