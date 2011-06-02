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


Modstat =
{
    startGatherResult : function(data) {
        //id = data
        Modstat.getStatus()
    },

    startGatherError : function(xhr, textStatus, error) {
        alert("Error starting gather:"+xhr.statusText)
    },

    statusResult : function(data) {
        if (data.error) {
            $('#progtext').text(data.error)
        }
        else if (data.stopped) {
            Modstat.showTabs()
            $('#progtext').html("<a href=\"servlets/zip\">Download Modstat Zip</a>")
        } else {
            var numComplete = data.percent;
            if (numComplete != undefined) {
                $('#progtext').text(numComplete+"% complete");
                Modstat.getStatus() // kick off another check
            }
        }
    },

    getStatus : function() {
        setTimeout(function() {
            $.get('servlets/longrunning', { action: 'status'}, Modstat.statusResult, "json")
        }, 1000)
    },

    treNavigated : function() {
        $('#progtext').hide()
        $('#gatherbutton').show()
        $('#tabs:visible').hide("blind", {}, 300)
    },

    showTabs : function() {
        $('#tabs').tabs('load', 1).show("blind", {}, 300)
        $('#gatherbutton').hide()
    }
}



$(function(){
    // Attach the dynatree widget to an existing <div id="tree"> element
    // and pass the tree options as an argument to the dynatree() function:
    $("#tree").dynatree({
        title: "System",
        selectMode:1,

        initAjax: {
            url: "servlets/treedata",
            data: { type:'net' }
        },

        onLazyRead: function(dtnode) {
            dtnode.appendAjax({
                url:"servlets/treedata",
                data: {
                    id:dtnode.data.key,
                    type: 'net'
                }
            })
        },

        onActivate: function(node) {
            Modstat.treNavigated()
        },

        cache: false
    });



    $('#actionbutton').button().bind('click', function() {
        $.ajax({url:'servlets/longrunning',
                data: {'id':id,
                       action: 'start'
                },
                success: function() { Modstat.startGatherResult() },
                error: function() { Modstat.startGatherError() }})
    })

    $("#test").bind('click', function() {
        Modstat.showTabs();
    })


    $("#tabs").tabs({ load: function() {eval("initConfig()") } });
                                                                    
});



