/*
 * Copyright (c) 2011 Automated Logic Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.alcshare.alarmcleanup.servlets;

import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.alarmmanager.Alarm;
import com.controlj.green.addonsupport.alarmmanager.AlarmFilter;
import com.controlj.green.addonsupport.alarmmanager.AlarmFilterFactory;
import static com.controlj.green.addonsupport.alarmmanager.AlarmFilterFactory.Clusive.*;
import com.controlj.green.addonsupport.alarmmanager.AlarmManager;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class CleanupServlet extends HttpServlet
{
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            // extract params
            final String locationId = request.getParameter("id");
            final String dateString = request.getParameter("days");
            final DateFormat df = new SimpleDateFormat( "MM/dd/yyyy");
            Date tempDate = df.parse(dateString);

            // convert to calendar and add one day (time of day is 00:00:00) from which we want all alarms before **exclusively**
            // this gives us alarms <= the user supplied date at 23:59:59
            final Calendar exclEndDate = new GregorianCalendar();
            exclEndDate.setTime(tempDate);
            exclEndDate.add(Calendar.DATE, 1);

            // get connection from HTTP request
            final SystemConnection connection = DirectAccess.getDirectAccess().getUserSystemConnection(request);

            // we need to resolve location. This must be done in a read action
            connection.runReadAction( FieldAccessFactory.newDisabledFieldAccess(), new ReadAction()
                {
                   public void execute(@NotNull SystemAccess access) throws Exception
                   {
                       // resolve our supplied location from tree picker
                       Location treeLoc = access.getTree(SystemTree.Geographic).resolve(locationId);

                       // get AlarmManager instance and set filter criteria
                       AlarmManager mgr = AlarmManager.getAlarmManager(connection);

                       // at tree location, (from start of time,) to given date, by ack pending
                       AlarmFilter filter = AlarmFilterFactory.newInstance()
                             .at(treeLoc)
                             .to(exclEndDate.getTime(), Exclusive)
                             .byAcknowledgePending()
                             .create();

                       // get the alarms - will be forward by generation time order
                       int count = 0;
                       final Iterator<Alarm> iter = mgr.getAlarms(filter);
                       // while we have a record...
                       while ( iter.hasNext() )
                       {
                           Alarm a = iter.next();
                           // anything that is never configured to return to normal
                           // or anything pending return to normal gets acknowledged
                           if ( a.isConfiguredToReturnToNormal() == false || a.isReturnedToNormal() == false)
                           {
                               a.doAcknowledge();
                               count++;
                           }
                           // else must be expecting a return to normal that has not occurred
                       }
                       // the "response" will be shown in an "alert()" browser dialog
                       if (count > 0)
                           response.getWriter().print("Cleaned " + count + " alarm(s) on/before " + dateString);
                       else
                           response.getWriter().print("No alarms found on/before " + dateString);
                   }
                } );

        }
        catch (ParseException e)
        {
            // bad user input - shouldn't normally be possible
            response.getWriter().print( "Invalid date" );
        }
        catch (Exception e)
        {
            // wow, something really bad happened
            response.getWriter().print(e.getMessage());
        }
    }
}
