/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
package org.unitime.timetable.form;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.util.ComboBoxLookup;


/** 
 * @author Tomas Muller
 */
public class ClassesForm extends ActionForm {
	private static final long serialVersionUID = -2473101585994845220L;
	private String iOp = null;
	private Long iSession = null;
	private String iSubjectArea = null;
	private String iCourseNumber = null;
	private Collection iSubjectAreas = null;
	private Vector iSessions = null;
	private String iTable = null;
	private int iNrColumns;
	private int iNrRows;
	private String iMessage;
	
	private String iUser, iPassword;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null;
		iTable = null;
		iNrRows = iNrColumns = 0;
		iSession = null; 
		iUser = null;
		iPassword = null;
		iMessage = null;
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
	
	public String getSubjectArea() { return iSubjectArea; }
	public void setSubjectArea(String subjectArea) { iSubjectArea = subjectArea; } 
	public Collection getSubjectAreas() { return iSubjectAreas; }
	
	public String getCourseNumber() { return iCourseNumber; }
	public void setCourseNumber(String courseNumber) { iCourseNumber = courseNumber; }
	
	public Long getSession() { return iSession; }
	public void setSession(Long session) { iSession = session; }
	public Collection getSessions() { return iSessions; }
	
	public void load(HttpSession session) {
	    setSubjectArea(session.getAttribute("Classes.subjectArea")==null?null:(String)session.getAttribute("Classes.subjectArea"));
        setCourseNumber(session.getAttribute("Classes.courseNumber")==null?null:(String)session.getAttribute("Classes.courseNumber"));
	    iSessions = new Vector();
        setSession(session.getAttribute("Classes.session")==null?iSessions.isEmpty()?null:Long.valueOf(((ComboBoxLookup)iSessions.lastElement()).getValue()):(Long)session.getAttribute("Classes.session"));
        boolean hasSession = false;
	    for (Iterator i=Session.getAllSessions().iterator();i.hasNext();) {
	        Session s = (Session)i.next();
	        if (s.getStatusType()!=null && s.getStatusType().canNoRoleReportClass() && Solution.hasTimetable(s.getUniqueId())) {
	            if (s.getUniqueId().equals(getSession())) hasSession = true;
	            iSessions.add(new ComboBoxLookup(s.getLabel(),s.getUniqueId().toString()));
	        }
	    }
	    if (!hasSession) { setSession(null); setSubjectArea(null); }
	    if (getSession()==null && !iSessions.isEmpty()) setSession(Long.valueOf(((ComboBoxLookup)iSessions.lastElement()).getValue()));
	    iSubjectAreas = new TreeSet(new SubjectAreaDAO().getSession().createQuery("select distinct sa.subjectAreaAbbreviation from SubjectArea sa").setCacheable(true).list());
	}
	    
    public void save(HttpSession session) {
        if (getSubjectArea()==null)
            session.removeAttribute("Classes.subjectArea");
        else
            session.setAttribute("Classes.subjectArea", getSubjectArea());
        if (getCourseNumber()==null)
            session.removeAttribute("Classes.courseNumber");
        else
            session.setAttribute("Classes.courseNumber", getCourseNumber());
        if (getSession()==null)
            session.removeAttribute("Classes.session");
        else
            session.setAttribute("Classes.session", getSession());
    }
    
    public void setTable(String table, int cols, int rows) {
        iTable = table; iNrColumns = cols; iNrRows = rows;
    }
    
    public String getTable() { return iTable; }
    public int getNrRows() { return iNrRows; }
    public int getNrColumns() { return iNrColumns; }
    
    public String getUsername() { return iUser; }
    public void setUsername(String user) { iUser = user; }
    public String getPassword() { return iPassword; }
    public void setPassword(String password) { iPassword = password; }
    public String getMessage() { return iMessage; }
    public void setMessage(String message) { iMessage = message; }
    
    public String getSessionLabel() {
        if (iSessions==null) return "";
        for (Enumeration e=iSessions.elements();e.hasMoreElements();) {
            ComboBoxLookup s = (ComboBoxLookup)e.nextElement();
            if (Long.valueOf(s.getValue()).equals(getSession())) return s.getLabel();
        }
        return "";
    }
}
