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

import com.controlj.green.addonsupport.InvalidConnectionRequestException;
import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.access.util.LocationSort;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

public class TreeDataServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        final String id = req.getParameter("id");
        String treeString = req.getParameter("type");
        final SystemTree tree;
        if (treeString == null || treeString.equals("geo")) {
            tree = SystemTree.Geographic;
        } else if (treeString.equals("net")) {
            tree = SystemTree.Network;
        } else {
            tree = SystemTree.Geographic;
        }

        final PrintWriter writer = resp.getWriter();

        try {
            SystemConnection connection = DirectAccess.getDirectAccess().getUserSystemConnection(req);

            connection.runReadAction(new ReadAction() {
                @Override
                public void execute(@NotNull SystemAccess access) throws Exception {
                    JSONArray arrayData = new JSONArray();
                    Collection<Location> children;
                    if (id == null) {
                        children = new ArrayList<Location>(1);
                        children.add(access.getTree(tree).getRoot());
                    }
                    else {
                        children = access.getTree(tree).resolve(id).getChildren(LocationSort.PRESENTATION);
                    }


                    for (Location child : children) {
                        if (child.getType() == LocationType.Microblock) {
                            continue;
                        }
                        JSONObject next = new JSONObject();
                        if (!child.hasParent()) {   // if this is the root, make it selected
                            next.put("activate", true);
                        }
                        next.put("title", child.getDisplayName());
                        next.put("key", child.getTransientLookupString());
                        next.put("path", child.getDisplayPath());
                        next.put("hideCheckbox", false);
                        next.put("isLazy", false);

                        if (!child.getChildren().isEmpty()) {
                            if (child.getType() != LocationType.Equipment) {
                                next.put("isLazy", true);
                            }
                        }

                        next.put("icon", getIconForType(child.getType()));
                        arrayData.put(next);
                    }
                    arrayData.write(writer);
                }
            });
        } catch (InvalidConnectionRequestException e) {
            // None of these should occur without programmer error - need to log when logging facility is available
            // This is as good as printing to System.err
            e.printStackTrace();
        } catch (SystemException e) {
            e.printStackTrace();
        } catch (ActionExecutionException e) {
            e.printStackTrace();
        }
    }

    private String getIconForType(LocationType type) {
        String urlBase = "../../../_common/lvl5/skin/graphics/type/";
        String image;

        switch (type) {
            case System:
                image = "system.gif";
                break;
            
            case Area:
                image = "area.gif";
                break;

            case Site:
                image = "site.gif";
                break;

            case Network:
                image = "network.gif";
                break;

            case Device:
                image = "hardware.gif";
                break;

            case Driver:
                image = "dir.gif";
                break;

            case Equipment:
                image = "equipment.gif";
                break;

            default:
                image = "unknown.gif";
                break;
        }

        return urlBase + image;
    }
}
