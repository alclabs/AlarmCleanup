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

$(function(){
    // Attach the dynatree widget to an existing <div id="tree"> element
    // and pass the tree options as an argument to the dynatree() function:
    // Currently hardcoded to GEO tree
    $("#tree").dynatree({
        title: "System",
        selectMode:1,

        initAjax: {
            url: "servlets/treedata",
            data: { type:'geo' }
        },

        onLazyRead: function(dtnode) {
            dtnode.appendAjax({
                url:"servlets/treedata",
                data: {
                    id:dtnode.data.key,
                    type: 'geo'
                }
            })
        },

        cache: false
    });


    // get the GEO location ID and also the date from the calendar picker
    $('#actionbutton').button().bind('click', function() {
        var id = ''
        var node = $('#tree').dynatree('getActiveNode')
        if (node) {
            id = node.data.key
        }
        $.ajax({url:'servlets/cleanup',
                data: {'id':id,
                       'days': $('#datepicker').val()
                },
                success: function(result) { alert(result) },
                error:   function() { alert("Unknown server failure" )}})
    })
});



