/**
 * Copyright 2017 Djer13

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
limitations under the License.
 */
package org.zeroclick.meeting.client.api.microsoft.service;

import org.zeroclick.meeting.client.api.microsoft.data.Calendar;
import org.zeroclick.meeting.client.api.microsoft.data.Event;
import org.zeroclick.meeting.client.api.microsoft.data.PagedResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @author djer
 *
 */
public interface CalendarService {

	@GET("/v1.0/me/events")
	Call<PagedResult<Event>> getEvents(@Query("$orderby") String orderBy, @Query("$select") String select,
			@Query("$top") Integer maxResults);

	@GET("/v1.0/me/calendar/calendarView")
	Call<PagedResult<Event>> getEvents(@Query("startDateTime") String start, @Query("endDateTime") String end,
			@Query("$orderby") String orderBy, @Query("$select") String select, @Query("$top") Integer maxResults);

	@PATCH("/v1.0/me/events/{eventId}")
	Call<Event> updateEvent(@Path("eventId") String eventId, Event modifiedEvent);

	@GET("/v1.0/me/events/{eventId}")
	Call<Event> getEvent(@Path("eventId") String eventId);

	@GET("/v1.0/me/calendars")
	Call<PagedResult<Calendar>> getCalendars();

}
