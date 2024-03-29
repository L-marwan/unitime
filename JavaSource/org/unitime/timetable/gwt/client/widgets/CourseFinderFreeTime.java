/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.widgets;

import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.widgets.CourseFinder.CourseFinderCourseDetails;
import org.unitime.timetable.gwt.client.widgets.CourseFinder.ResponseEvent;
import org.unitime.timetable.gwt.client.widgets.CourseFinder.ResponseHandler;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.FreeTime;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Tomas Muller
 */
public class CourseFinderFreeTime extends VerticalPanel implements CourseFinder.CourseFinderTab<List<CourseRequestInterface.FreeTime>> {
	protected static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	
	private FreeTimePicker iFreeTimePicker;
	private Label iFreeTimeError, iFreeTimeTip;
	private FreeTimeParser iDataProvider;
	
	public CourseFinderFreeTime() {
		setSpacing(10);
		iFreeTimePicker = new FreeTimePicker();
		iFreeTimePicker.addValueChangeHandler(new ValueChangeHandler<List<FreeTime>>() {
			@Override
			public void onValueChange(ValueChangeEvent<List<FreeTime>> event) {
				ValueChangeEvent.fire(CourseFinderFreeTime.this, iDataProvider.freeTimesToString(event.getValue()));
				iFreeTimeError.setVisible(false);
			}
		});
		add(iFreeTimePicker);

		iFreeTimeTip = new Label(CONSTANTS.freeTimeTips()[(int)(Math.random() * CONSTANTS.freeTimeTips().length)]);
		iFreeTimeTip.setStyleName("unitime-Hint");
		ToolBox.disableTextSelectInternal(iFreeTimeTip.getElement());
		iFreeTimeTip.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				String oldText = iFreeTimeTip.getText();
				do {
					iFreeTimeTip.setText(CONSTANTS.freeTimeTips()[(int)(Math.random() * CONSTANTS.freeTimeTips().length)]);
				} while (oldText.equals(iFreeTimeTip.getText()));
			}
		});
		add(iFreeTimeTip);

		iFreeTimeError = new Label();
		iFreeTimeError.setStyleName("unitime-ErrorMessage");
		iFreeTimeError.setVisible(false);
		add(iFreeTimeError);
	}

	@Override
	public String getValue() {
		List<CourseRequestInterface.FreeTime> ret = iFreeTimePicker.getValue();
		if (ret == null || ret.isEmpty()) return null;
		return iDataProvider.freeTimesToString(ret);
	}
	
	@Override
	public void setValue(String value) {
		setValue(value, false);
	}

	@Override
	public void setValue(String value, final boolean fireEvents) {
		if (value == null || value.isEmpty()) {
			iFreeTimePicker.setValue(null);
			iFreeTimeError.setText(MESSAGES.courseSelectionNoFreeTime());
			iFreeTimeError.setVisible(true);
		} else {
			iDataProvider.getData(value, new AsyncCallback<List<FreeTime>>() {
				@Override
				public void onSuccess(List<FreeTime> freeTimes) {
					iFreeTimePicker.setValue(freeTimes, fireEvents);
					iFreeTimeError.setVisible(false);
					String status = "";
					for (CourseRequestInterface.FreeTime ft: freeTimes) {
						status += ft.toAriaString(CONSTANTS.longDays(), CONSTANTS.useAmPm()) + " ";
					}
					if (!status.isEmpty())
						AriaStatus.getInstance().setText(ARIA.courseFinderSelectedFreeTime(status));
					ResponseEvent.fire(CourseFinderFreeTime.this, true);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					iFreeTimePicker.setValue(null);
					iFreeTimeError.setText(caught.getMessage());
					iFreeTimeError.setVisible(true);
					ResponseEvent.fire(CourseFinderFreeTime.this, false);
				}
			});
		}
	}
	
	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public HandlerRegistration addSelectionHandler(SelectionHandler<String> handler) {
		return addHandler(handler, SelectionEvent.getType());
	}

	@Override
	public String getName() {
		return MESSAGES.courseSelectionFreeTime();
	}

	@Override
	public void setDataProvider(DataProvider<String, List<FreeTime>> provider) {
		iDataProvider = (FreeTimeParser)provider;
	}

	@Override
	public boolean isCourseSelection() {
		return false;
	}

	@Override
	public void setCourseDetails(CourseFinderCourseDetails... details) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void onKeyUp(KeyUpEvent event) {
	}

	@Override
	public HandlerRegistration addResponseHandler(ResponseHandler handler) {
		return addHandler(handler, ResponseEvent.getType());
	}
	
	@Override
	public void changeTip() {
		iFreeTimeTip.setText(CONSTANTS.freeTimeTips()[(int)(Math.random() * CONSTANTS.freeTimeTips().length)]);
	}
}
