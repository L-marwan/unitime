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
package org.unitime.timetable.gwt.client;

import java.util.List;

import org.unitime.timetable.gwt.services.MenuService;
import org.unitime.timetable.gwt.services.MenuServiceAsync;
import org.unitime.timetable.gwt.shared.MenuInterface;
import org.unitime.timetable.gwt.shared.ToolBox;
import org.unitime.timetable.gwt.widgets.LoadingWidget;
import org.unitime.timetable.gwt.widgets.PageLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.dom.client.FrameElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Window.ScrollEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class UniTimeMenuBar extends Composite {
	protected final MenuServiceAsync iService = GWT.create(MenuService.class);
	
	private MenuBar iMenu;
	private Timer iTimer = null;
	private SimplePanel iSimple = null;

	public UniTimeMenuBar(boolean absolute) {
		iMenu = new MenuBar();
		iMenu.setVisible(false);
		iMenu.addStyleName("unitime-NoPrint");
		iMenu.addStyleName("unitime-Menu");
		initWidget(iMenu);
		
		if (absolute) {
			DOM.setStyleAttribute(iMenu.getElement(), "position", "absolute");
			iMenu.setWidth(String.valueOf(Window.getClientWidth() - 2));
			Window.addResizeHandler(new ResizeHandler() {
				@Override
				public void onResize(ResizeEvent event) {
					iMenu.setWidth(String.valueOf(Window.getClientWidth() - 2));
				}
			});
			final Timer showTimer = new Timer() {
				@Override
				public void run() {
					DOM.setStyleAttribute(iMenu.getElement(), "left", String.valueOf(Window.getScrollLeft()));
					DOM.setStyleAttribute(iMenu.getElement(), "top", String.valueOf(Window.getScrollTop()));
					iMenu.setVisible(true);
				}
			};
			Window.addWindowScrollHandler(new Window.ScrollHandler() {
				@Override
				public void onWindowScroll(ScrollEvent event) {
					iMenu.setVisible(false);
					showTimer.schedule(100);
				}
			});
			iSimple = new SimplePanel();
		}
		
		iService.getMenu(new AsyncCallback<List<MenuInterface>>() {
			@Override
			public void onSuccess(List<MenuInterface> result) {
				initMenu(iMenu, result, 0);
				iMenu.setVisible(true);
				if (iSimple != null)
					iSimple.setHeight(String.valueOf(iMenu.getOffsetHeight()));
			}
			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}
	
	private void initMenu(MenuBar menu, List<MenuInterface> items, int level) {
		MenuItemSeparator lastSeparator = null;
		for (final MenuInterface item: items) {
			if (item.isSeparator()) {
				lastSeparator = new MenuItemSeparator();
				menu.addSeparator(lastSeparator);
			} else if (item.hasSubMenus()) {
				if (item.getPage() == null) {
					MenuBar m = new MenuBar(true);
					initMenu(m, item.getSubMenus(), level + 1);
					menu.addItem(new MenuItem(item.getName().replace(" ", "&nbsp;"), true, m));
				} else {
					menu.addItem(new MenuItem(item.getName().replace(" ", "&nbsp;"), true, new Command() {
						@Override
						public void execute() {
							if (item.isGWT()) 
								//openPageAsync(item.getPage());
								openUrl(item.getName(), "gwt.jsp?page=" + item.getPage(), item.getTarget());
							else {
								openUrl(item.getName(), item.getPage(), item.getTarget());
							}
						}
					}));
					initMenu(menu, item.getSubMenus(), level);
				}
			} else {
				menu.addItem(new MenuItem(item.getName().replace(" ", "&nbsp;"), true, new Command() {
					@Override
					public void execute() {
						if (item.getPage() != null) {
							if (item.isGWT()) 
								//openPageAsync(item.getPage());
								openUrl(item.getName(), "gwt.jsp?page=" + item.getPage(), item.getTarget());
							else {
								openUrl(item.getName(), item.getPage(), item.getTarget());
							}
						}
					}
				}));
			}
		}
		if (level == 0 && lastSeparator != null) {
			lastSeparator.setStyleName("unitime-BlankSeparator");
			lastSeparator.setWidth("100%");
		}
	}
	
	protected void openUrl(final String name, final String url, String target) {
		/*
		if (url.indexOf('?') >= 0)
			url += "&gwt.codesvr=127.0.0.1:9997";
		else
			url += "?gwt.codesvr=127.0.0.1:9997";
		*/
		if (target == null)
			LoadingWidget.getInstance().show();
		if ("dialog".equals(target)) {
			final DialogBox dialog = new MyDialogBox();
			dialog.setAutoHideEnabled(true);
			dialog.setModal(true);
			final Frame frame = new MyFrame(name);
			frame.getElement().getStyle().setBorderWidth(0, Unit.PX);
			dialog.setGlassEnabled(true);
			dialog.setAnimationEnabled(true);
			dialog.setWidget(frame);
			dialog.setText(name);
			frame.setUrl(url);
			frame.setSize(String.valueOf(Window.getClientWidth() * 3 / 4), String.valueOf(Window.getClientHeight() * 3 / 4));
			
			iTimer = new Timer() {
				@Override
				public void run() {
					if (LoadingWidget.getInstance().isShowing())
						LoadingWidget.getInstance().fail(name + " does not seem to load, " +
								"please check <a href='" + url + "' style='white-space: nowrap;'>" + url + "</a> for yourself.");
				}
			};

			dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
				@Override
				public void onClose(CloseEvent<PopupPanel> event) {
					if (LoadingWidget.getInstance().isShowing())
						LoadingWidget.getInstance().hide();
				}
			});

			dialog.center();
			iTimer.schedule(30000);
		} else if ("download".equals(target)) {
			ToolBox.open(url);
		} else {
			ToolBox.open(GWT.getHostPageBaseURL() + url);
		}
	}
	
	protected void openPageAsync(final String page) {
		LoadingWidget.getInstance().show();
		if (RootPanel.get("UniTimeGWT:Content") == null || RootPanel.get("UniTimeGWT:TitlePanel") == null) {
			ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=" + page);
			return;
		}
		RootPanel.get("UniTimeGWT:Content").clear();
		RootPanel.get("UniTimeGWT:Content").getElement().setInnerHTML(null);
		RootPanel.get("UniTimeGWT:TitlePanel").clear();
		RootPanel.get("UniTimeGWT:TitlePanel").getElement().setInnerHTML(null);
		GWT.runAsync(new RunAsyncCallback() {
			public void onSuccess() {
				openPage(page);
				LoadingWidget.getInstance().hide();
			}
			public void onFailure(Throwable reason) {
				Label error = new Label("Failed to load the page (" + reason.getMessage() + ")");
				error.setStyleName("unitime-ErrorMessage");
				RootPanel.get("UniTimeGWT:Content").add(error);
				LoadingWidget.getInstance().hide();
			}
		});
	}
	
	protected void openPage(String page) {
		try {
			for (Pages p: Pages.values()) {
				if (p.name().equals(page)) {
					LoadingWidget.getInstance().setMessage("Loading " + p.title() + " ...");
					RootPanel title = RootPanel.get("UniTimeGWT:Title");
					if (title != null) {
						title.clear();
						PageLabel label = new PageLabel(); label.setPageName(p.title());
						title.add(label);
					}
					RootPanel.get("UniTimeGWT:Content").add(p.widget());
					return;
				}
			}
			Label error = new Label("Failed to load the page (" + (page == null ? "page not provided" : "page " + page + " not registered" ) + ")");
			error.setStyleName("unitime-ErrorMessage");
			RootPanel.get("UniTimeGWT:Content").add(error);
		} catch (Exception e) {
			Label error = new Label("Failed to load the page (" + e.getMessage() + ")");
			error.setStyleName("unitime-ErrorMessage");
			RootPanel.get("UniTimeGWT:Content").add(error);
		}
	}
	
	public void insert(final RootPanel panel) {
		panel.add(this);
		panel.setVisible(true);
		if (iSimple != null) {
			iSimple.setHeight(String.valueOf(iMenu.getOffsetHeight()));
			panel.add(iSimple);
		}
	}
	
	protected class MyDialogBox extends DialogBox {
		private MyDialogBox() { super(); }
		protected void onPreviewNativeEvent(NativePreviewEvent event) {
			super.onPreviewNativeEvent(event);
			if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_ESCAPE)
				MyDialogBox.this.hide();
		}
	}
	
	public static void notifyFrameLoaded() {
		LoadingWidget.getInstance().hide();
	}
	
	public class MyFrame extends Frame {
		private String iName;
		
		public MyFrame(String name) {
			super();
			iName = name;
			hookFremaLoaded((FrameElement)getElement().cast());
		}
		
		public void onLoad() {
			super.onLoad();
			LoadingWidget.getInstance().show("Loading " + iName + " ...");
		}
	}
	
	public native void hookFremaLoaded(FrameElement element) /*-{
		element.onload = function() {
			@org.unitime.timetable.gwt.client.UniTimeMenuBar::notifyFrameLoaded()();
		}
	}-*/;
}
