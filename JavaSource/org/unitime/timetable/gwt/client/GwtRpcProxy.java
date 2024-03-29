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
package org.unitime.timetable.gwt.client;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;

import com.google.gwt.http.client.Request;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.impl.RemoteServiceProxy;
import com.google.gwt.user.client.rpc.impl.RpcStatsContext;
import com.google.gwt.user.client.rpc.impl.Serializer;
import com.google.gwt.user.client.rpc.impl.RequestCallbackAdapter.ResponseReader;

/**
 * @author Tomas Muller
 */
public class GwtRpcProxy extends RemoteServiceProxy {
	
	public GwtRpcProxy(String moduleBaseURL, String remoteServiceRelativePath, String serializationPolicyName, Serializer serializer) {
		super(moduleBaseURL, remoteServiceRelativePath, serializationPolicyName, serializer);
	}
	
	@Override
    protected <T> Request doInvoke(ResponseReader responseReader, final String methodName, RpcStatsContext statsContext, String requestData, final AsyncCallback<T> callback) {
		return super.doInvoke(responseReader, methodName, statsContext, requestData, new AsyncCallback<T>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error("Request " + methodName.replace("_Proxy", "") + " failed: " + caught.getMessage(), caught);
				callback.onFailure(caught);
			}
			@Override
			public void onSuccess(T result) {
				callback.onSuccess(result);
			}
		});
	}
}
