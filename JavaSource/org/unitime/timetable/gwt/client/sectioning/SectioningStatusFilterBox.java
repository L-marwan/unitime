/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.sectioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unitime.timetable.gwt.client.aria.AriaSuggestBox;
import org.unitime.timetable.gwt.client.events.UniTimeFilterBox;
import org.unitime.timetable.gwt.client.widgets.FilterBox;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Suggestion;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Tomas Muller
 */
public class SectioningStatusFilterBox extends UniTimeFilterBox<SectioningStatusFilterBox.SectioningStatusFilterRpcRequest> {
	private static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	
	private boolean iOnline;
	
	private AriaSuggestBox iCourse;
	private Chip iLastCourse;
	
	private AriaSuggestBox iStudent;
	private Chip iLastStudent;
	
	public SectioningStatusFilterBox(boolean online) {
		super(null);
		
		iOnline = online;

		FilterBox.StaticSimpleFilter mode = new FilterBox.StaticSimpleFilter("mode");
		mode.setMultipleSelection(false);
		addFilter(mode);
		
		addFilter(new FilterBox.StaticSimpleFilter("type"));
		
		addFilter(new FilterBox.StaticSimpleFilter("status"));
		addFilter(new FilterBox.StaticSimpleFilter("approver"));

		
		addFilter(new FilterBox.StaticSimpleFilter("area"));
		addFilter(new FilterBox.StaticSimpleFilter("major"));
		addFilter(new FilterBox.StaticSimpleFilter("classification"));
		addFilter(new FilterBox.StaticSimpleFilter("group"));
		addFilter(new FilterBox.StaticSimpleFilter("accommodation"));
		
		addFilter(new FilterBox.StaticSimpleFilter("assignment"));
		
		addFilter(new FilterBox.StaticSimpleFilter("consent"));
		
		FilterBox.StaticSimpleFilter op = new FilterBox.StaticSimpleFilter("operation");
		op.setMultipleSelection(true);
		addFilter(op);

		final TextBox curriculum = new TextBox();
		curriculum.setStyleName("unitime-TextArea");
		curriculum.setMaxLength(100); curriculum.setWidth("200px");
		
		curriculum.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				boolean removed = removeChip(new Chip("curriculum", null), false);
				if (curriculum.getText().isEmpty()) {
					if (removed)
						fireValueChangeEvent();
				} else {
					addChip(new Chip("curriculum", curriculum.getText()), true);
				}
			}
		});
		
		Label courseLab = new Label(MESSAGES.propCourse());
		iCourse = new AriaSuggestBox(new CourseOracle());
		iCourse.setStyleName("unitime-TextArea");
		iCourse.setWidth("200px");
		addFilter(new FilterBox.StaticSimpleFilter("course"));
		iCourse.getValueBox().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				courseChanged(true);
			}
		});
		iCourse.getValueBox().addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						courseChanged(false);
					}
				});
			}
		});
		iCourse.getValueBox().addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE)
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							courseChanged(false);
						}
					});
			}
		});
		iCourse.getValueBox().addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				courseChanged(true);
			}
		});
		iCourse.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<com.google.gwt.user.client.ui.SuggestOracle.Suggestion> event) {
				courseChanged(true);
			}
		});
		
		
		Label studentLab = new Label(MESSAGES.propStudent());
		studentLab.getElement().getStyle().setMarginLeft(10, Unit.PX);
		iStudent = new AriaSuggestBox(new StudentOracle());
		iStudent.setStyleName("unitime-TextArea");
		iStudent.setWidth("200px");
		addFilter(new FilterBox.StaticSimpleFilter("student"));
		iStudent.getValueBox().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				studentChanged(true);
			}
		});
		iStudent.getValueBox().addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						studentChanged(false);
					}
				});
			}
		});
		iStudent.getValueBox().addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE)
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							studentChanged(false);
						}
					});
			}
		});
		iStudent.getValueBox().addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				studentChanged(true);
			}
		});
		iStudent.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<com.google.gwt.user.client.ui.SuggestOracle.Suggestion> event) {
				studentChanged(true);
			}
		});
		
		addFilter(new FilterBox.CustomFilter("Other", courseLab, iCourse, studentLab, iStudent) {
			@Override
			public void getSuggestions(final List<Chip> chips, final String text, AsyncCallback<Collection<FilterBox.Suggestion>> callback) {
				if (text.isEmpty()) {
					callback.onSuccess(null);
				} else {
					List<FilterBox.Suggestion> suggestions = new ArrayList<FilterBox.Suggestion>();
					Chip old = null;
					for (Chip c: chips) { if (c.getCommand().equals("limit")) { old = c; break; } }
					try {
						Integer.parseInt(text);
						suggestions.add(new Suggestion(new Chip("limit", text), old));
					} catch (NumberFormatException e) {}
					callback.onSuccess(suggestions);
				}
			}
		});
		
		addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iLastCourse = getChip("course");
				iLastStudent = getChip("student");

				if (!isFilterPopupShowing()) {
					Chip chip = getChip("curriculum");
					if (chip == null)
						curriculum.setText("");
					else
						curriculum.setText(chip.getValue());

					Chip course = getChip("course");
					if (course == null)
						iCourse.setText("");
					else
						iCourse.setText(course.getValue());

					Chip student = getChip("student");
					if (student == null)
						iStudent.setText("");
					else
						iStudent.setText(student.getValue());
				}
				
				init(false, getAcademicSessionId(), new Command() {
					@Override
					public void execute() {
						if (isFilterPopupShowing())
							showFilterPopup();
					}
				});
			}
		});
	}
	
	private void courseChanged(boolean fireChange) {
		Chip oldChip = getChip("course");
		if (iCourse.getText().isEmpty()) {
			if (oldChip != null)
				removeChip(oldChip, fireChange);
		} else {
			Chip newChip = new Chip("course", iCourse.getText());
			if (oldChip != null) {
				if (newChip.equals(oldChip)) {
					if (fireChange && !newChip.equals(iLastCourse)) fireValueChangeEvent();
					return;
				}
				removeChip(oldChip, false);
			}
			addChip(newChip, fireChange);
		}
	}
	
	private void studentChanged(boolean fireChange) {
		Chip oldChip = getChip("student");
		if (iStudent.getText().isEmpty()) {
			if (oldChip != null)
				removeChip(oldChip, fireChange);
		} else {
			Chip newChip = new Chip("student", iStudent.getText());
			if (oldChip != null) {
				if (newChip.equals(oldChip)) {
					if (fireChange && !newChip.equals(iLastStudent)) fireValueChangeEvent();
					return;
				}
				removeChip(oldChip, false);
			}
			addChip(newChip, fireChange);
		}
	}
	
	@Override
	protected boolean populateFilter(FilterBox.Filter filter, List<FilterRpcResponse.Entity> entities) {
		if (filter != null && filter instanceof FilterBox.StaticSimpleFilter) {
			FilterBox.StaticSimpleFilter simple = (FilterBox.StaticSimpleFilter)filter;
			List<FilterBox.Chip> chips = new ArrayList<FilterBox.Chip>();
			if (entities != null) {
				for (FilterRpcResponse.Entity entity: entities)
					chips.add(new FilterBox.Chip(filter.getCommand(), entity.getAbbreviation(), entity.getName(), entity.getCount() <= 0 ? null : "(" + entity.getCount() + ")"));
			}
			simple.setValues(chips);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public SectioningStatusFilterRpcRequest createRpcRequest() {
		SectioningStatusFilterRpcRequest req = new SectioningStatusFilterRpcRequest();
		req.setOption("online", iOnline ? "true" : "false");
		return req;
	}
	
	public static class SectioningStatusFilterRpcRequest extends FilterRpcRequest {
		private static final long serialVersionUID = 1L;

		public SectioningStatusFilterRpcRequest() {}
	}
	
	public class CourseSuggestion implements SuggestOracle.Suggestion {
		private FilterBox.Suggestion iSuggestion;
		
		CourseSuggestion(FilterBox.Suggestion suggestion) {
			iSuggestion = suggestion;
		}

		@Override
		public String getDisplayString() {
			return iSuggestion.getChipToAdd().getName() + " <span class='item-hint'>" + iSuggestion.getChipToAdd().getHint() + "</span>";
		}

		@Override
		public String getReplacementString() {
			return iSuggestion.getChipToAdd().getValue();
		}
	}
	
	public class CourseOracle extends SuggestOracle {

		@Override
		public void requestSuggestions(final Request request, final Callback callback) {
			if (!request.getQuery().isEmpty()) {
				iFilter.getWidget().getSuggestionsProvider().getSuggestions(iFilter.getWidget().getChips(null), request.getQuery(), new AsyncCallback<Collection<FilterBox.Suggestion>>() {

					@Override
					public void onFailure(Throwable caught) {
					}

					@Override
					public void onSuccess(Collection<FilterBox.Suggestion> result) {
						if (result == null) return;
						List<CourseSuggestion> suggestions = new ArrayList<CourseSuggestion>();
						for (FilterBox.Suggestion suggestion: result) {
							if (suggestion.getChipToAdd() != null && "course".equals(suggestion.getChipToAdd().getCommand())) {
								suggestions.add(new CourseSuggestion(suggestion));
							}
						}
						callback.onSuggestionsReady(request, new Response(suggestions));
					}
				});
			}
		}
		
		@Override
		public boolean isDisplayStringHTML() {
			return true;
		}
	}
	
	public class StudentSuggestion implements SuggestOracle.Suggestion {
		private FilterBox.Suggestion iSuggestion;
		
		StudentSuggestion(FilterBox.Suggestion suggestion) {
			iSuggestion = suggestion;
		}

		@Override
		public String getDisplayString() {
			return iSuggestion.getChipToAdd().getName();
		}

		@Override
		public String getReplacementString() {
			return iSuggestion.getChipToAdd().getValue();
		}
	}
	
	public class StudentOracle extends SuggestOracle {

		@Override
		public void requestSuggestions(final Request request, final Callback callback) {
			if (!request.getQuery().isEmpty()) {
				iFilter.getWidget().getSuggestionsProvider().getSuggestions(iFilter.getWidget().getChips(null), request.getQuery(), new AsyncCallback<Collection<FilterBox.Suggestion>>() {

					@Override
					public void onFailure(Throwable caught) {
					}

					@Override
					public void onSuccess(Collection<FilterBox.Suggestion> result) {
						if (result == null) return;
						List<StudentSuggestion> suggestions = new ArrayList<StudentSuggestion>();
						for (FilterBox.Suggestion suggestion: result) {
							if (suggestion.getChipToAdd() != null && "student".equals(suggestion.getChipToAdd().getCommand())) {
								suggestions.add(new StudentSuggestion(suggestion));
							}
						}
						callback.onSuggestionsReady(request, new Response(suggestions));
					}
				});
			}
		}
	}

}
