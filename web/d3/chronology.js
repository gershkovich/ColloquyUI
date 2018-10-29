var dots = null;

var margin = {top: 20, right: 20, bottom: 80, left: 50},
    margin2 = {top: 340, right: 20, bottom: 0, left: 50},
    width = 1160 - margin.left - margin.right,
    height = 400 - margin.top - margin.bottom,
    height2 = 30;

var x = d3.scaleUtc().range([0, width]),
    x2 = d3.scaleUtc().range([0, width]),
    x3 = d3.scaleUtc().range([0, width]),
    y = d3.scaleLinear().range([height, height / 1.5]),
    y2 = d3.scaleLinear().range([height2, 0]),
    z = d3.scaleLinear().range([height, height - 50]);

var svg;


var book_titles = [];


function buildChronologyChart(divId, dataIn, dataForEvents, yAxisLabel, startAndEndDates, location) {

    var parseDate = d3.timeParse("%Y-%m-%d");

    var formatDate = d3.timeFormat("%Y-%m-%d");

    var monthFormatter = d3.timeFormat('%Y-%b');

    var weekFormatter = d3.timeFormat('%Y %W');

    var parseMonth = d3.timeParse("%Y-%b");

    var parseWeek = d3.timeParse("%Y %W");


    var xAxis = d3.axisBottom(x),
        xAxis2 = d3.axisBottom(x2),
        yAxis = d3.axisLeft(y).ticks(5),
        zAxis = d3.axisRight(z).ticks(0);

    var brush = d3.brushX()
        .extent([[0, 0], [width, height2]])
        .on("brush end", brushed);

    svg = d3.select(divId).append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom);

    // var svg = d3.select(divId)
    //     .append("svg")
    //     .attr("preserveAspectRatio", "xMinYMin meet")
    //     .attr("viewBox", "0 0 100 200")
    //     .classed("svg-content", true);

    svg.append("defs").append("clipPath")
        .attr("id", "clip")
        .append("rect")
        .attr("width", width)
        .attr("height", height);

    var focus = svg.append("g")
        .attr("class", "focus")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var eventFocus = svg.append("g")
        .attr("class", "evemtFocus")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var context = svg.append("g")
        .attr("class", "context")
        .attr("transform", "translate(" + margin2.left + "," + margin2.top + ")");

    var agg = "months";

    var data = d3.csvParse(dataIn, function (d) {
        return {
            date: d.date = parseDate(d.date),
            letters: d.letters = +d.letters

        };
    });


    var selected_line_coordinates;


    var bookUl = d3.select("#book-list");


    var div = d3.select("body").append("div")
        .attr("class", "tooltip")
        .style("opacity", 0);

    var toolTip = d3.select('.tooltip');

    if (toolTip.empty()) {
        toolTip = d3.select("body").append("div")
            .attr("class", "tooltip")
            .style("opacity", 0);
    } else {

        toolTip.transition()
            .duration(0)
            .style("opacity", 0);
        toolTip.html("")
    }


    var dataByMonth = [];

    getAggregationPerMonth(dataByMonth);

    var dataByWeek = [];

    getAggregationPerWeek(dataByWeek);

    //now rewrite data

    // x.domain(d3.extent(dataByMonth, function (d) {
    //
    //     return d.date;
    //
    // }));


    x.domain([new Date(1840, 1, 1), new Date(1911, 1, 1)]); //we hardcode it to avoid shift in diaries vs letters

    //console.log(JSON.stringify(x.domain(), null, 2));

    y.domain([0, d3.max(dataByMonth, function (d) {
        return d.letters;
    })]);
    x2.domain(x.domain());
    y2.domain(y.domain());
    x3.domain(x.domain());


    z.domain([1, Math.log10(1000)]);

    var bars = focus.append("g");

    dots = focus.append("g");

    bars.attr("clip-path", "url(#clip)");

    bars.selectAll("bar")
        .data(dataByMonth)
        .enter().append("rect")
        .attr('class', 'bar')
        .style("fill", "steelblue")
        .attr("x", function (d) {
            return x(d.date);
        })
        .attr("width", 5)
        .attr("y", function (d) {
            return y(d.letters);
        })
        .attr("height", function (d) {
            return height - y(d.letters);
        })
        .on("mouseover", function () { //<-A
            var position = d3.mouse(svg.node());
            //console.log(x.invert(position[0]));
        });

    focus.append("g")
        .attr("class", "axis axis--x")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

    focus.append("g")
        .attr("class", "axis axis--y")
        .call(yAxis);

    //zAxis  should be moved right
    // focus.append("g")
    //     .attr("class", "axis axis--z")
    //     .attr("transform", "translate(" + width + ",0)")
    //     .attr("hidden", true)
    //     .call(zAxis);


    focus.append("text")
        .attr('class', 'y_label')
        .attr("transform", "rotate(-90)")
        .attr("y", 0 - margin.left)
        .attr("x", 0 - (height - 50))
        .attr("dy", "1em")
        .style("text-anchor", "middle")
        .text(yAxisLabel[0]).style("font-size", "0.7em").style("fill", "#113f6c");

    svg.append("text")
        .attr("transform",
            "translate(" + ((width + margin.right + margin.left) / 2) + " ," +
            (height + margin.top + margin.bottom) + ")")
        .style("text-anchor", "middle");
    // .text("Date");

    var bars2 = context.append("g");

    bars2.attr("clip-path", "url(#clip)");

    bars2.selectAll("bars2")
        .data(dataByMonth)
        .enter().append("rect")
        .attr('class', 'barContext')
        .style("fill", "gray")
        .attr("x", function (d) {
            return x2(d.date);
        })
        .attr("width", 5)
        .attr("y", function (d) {
            return y2(d.letters);
        })
        .attr("height", function (d) {
            return height2 - y2(d.letters);
        });

    context.append("g")
        .attr("class", "axis axis--x")
        .attr("transform", "translate(0," + height2 + ")")
        .call(xAxis2);

    context.append("g")
        .attr("class", "brush")
        .call(brush)
        .call(brush.move, x.range());


    // d3.selectAll(".help_label").transition(t).remove();
    //
    // d3.selectAll(".context_area").transition(t).style("fill-opacity", "1");


    var lineEventFunction = d3.line()
        .defined(function (d) {
            return d !== null;
        })
        .x(function (d) {

            return x(d.x);
        })
        .y(function (d) {
            return d.y;
        });


    //create brush function redraw scatterplot with selection
    function brushed() {

        var selection = d3.event.selection;

        x.domain(selection.map(x2.invert, x2));

        //render(1);

        //depending on distance between  x2.invert, x2 change data aggregation

        var monthsDiff = d3.timeMonth.count(x.domain()[0], x.domain()[1]);

        //todo call function to load letters by range and selected text and facets

        us.colloquy.tolstoy.client.uplink.DataUplink.getDocumentsByRange(formatDate(x.domain()[0]), formatDate(x.domain()[1]));

        if (monthsDiff <= 40 && agg !== "days") {
            agg = "days";

            //switch to days

            y.domain([0, d3.max(data, function (d) {
                return d.letters;
            })]);
            update(data, yAxisLabel[2], "DarkKhaki");


        } else if (monthsDiff > 40 && monthsDiff < 100 && agg !== "weeks") {
            agg = "weeks";

            y.domain([0, d3.max(dataByWeek, function (d) {
                return d.letters;
            })]);
            //switch to moths
            update(dataByWeek, yAxisLabel[1], "CadetBlue");

        }
        else if (monthsDiff >= 100 && agg !== "months") {
            agg = "months";

            y.domain([0, d3.max(dataByMonth, function (d) {
                return d.letters;
            })]);
            //switch to moths
            update(dataByMonth, yAxisLabel[0], "SteelBlue");

        }
        // console.log(x.invert(position[0]));

        focus.selectAll(".bar")
            .attr("x", function (d) {
                return x(d.date);
            })
            .attr("width", 5)
            .attr("y", function (d) {
                return y(d.letters);
            })
            .attr("height", function (d) {
                return height - y2(d.letters);
            });

        focus.select(".axis--x").call(xAxis);

        focus.selectAll(".dot")

            .attr("cx", function (d) {
                return x(d.date);
            })
            .attr("cy", function (d) {

                if (d.words > 1000) {
                    return z(Math.log10(1000));
                } else {
                    return z(Math.log10(d.words));
                }

            });

        //lineEventFunction takes data that already exists with the path object classed as line_event and passes it to lineEventFunction
        eventFocus.selectAll(".line_event").attr("d", lineEventFunction);

        eventFocus.selectAll("ellipse")
            .attr("cx", function (d) {
                return x(d.x);
            })
            .attr("cy", function (d) {
                return d.y;
            })

        if (selected_line_coordinates) {
            eventFocus.selectAll(".highlighter_line").attr("x1", x(selected_line_coordinates.x1))
                .attr("y1", selected_line_coordinates.y)
                .attr("x2", x(selected_line_coordinates.x2))
                .attr("y2", selected_line_coordinates.y);
        }


        var local_books = book_titles.filter(function (d) {
            //return x(d["value"]["min_extent"]) < width && x(d["value"]["max_extent"]) > 0;


            var matchingPeriod = false;

            matchingPeriod = d.value.events.some(function (el, i) {
                //return test.call(ctx, el, i, arr) ? ((result = el), true) : false;

                return x(new Date(el.start)) < width && x(new Date(el.start)) > 0;

            });

            return matchingPeriod;

        });
        //load list of books in scroll panel

        bookUl.selectAll("div").remove();
        bookUl.selectAll("div")
            .data(local_books)
            .enter()
            .append("div")
            .attr('class', 'book_list_items')
            .style("color", function (d) {

                if (selected_line_coordinates && d.key == selected_line_coordinates.key) {
                    return '#ff0014';
                } else {
                    return '#256f6a';
                }

            })
            .text(function (d) {
                if (location != "ru") {
                    return d["value"]["en_title"];
                } else
                {
                    return d["value"]["ru_title"];
                }
            })
            .on('mouseover', function (d) {
                book_mouseover(d);
            })
            .on('mouseout', function (d) {
                book_mouseout(d);
            })
            .on('click', function (d) {

                bookUl.selectAll("div").style('color', '#256f6a');
                eventFocus.selectAll(".highlighter_line").remove();

                if (selected_line_coordinates && d.key == selected_line_coordinates.key) {

                    selected_line_coordinates.key="xyz";

                } else {
                    d3.select(this).style('color', '#ff0014');

                    book_click(d);
                }


            }).on('dblclick', selectTimelineForPeriod);;

    }

    var myBrash = d3.selectAll(".brush");

    // var help  =    context.append("text")
    //     .attr('class', 'ask')
    //     .attr("y", -15)
    //     .attr("x", width + 10)
    //     .attr("dy", "1em")
    //     .style("text-anchor", "middle")
    //     .style("fill", "#7aa75e")
    //     .html("&nbsp;?&nbsp;").on("click", function (d)
    //     {
    //         showFunctionaligy();
    //
    //     }).transition()       // apply a transition
    //     .duration(60000).remove();

    function showFunctionaligy() {
        var rightArrow = context.append("text")
            .attr('class', 'help_label')
            .attr("y", 5)
            .attr("x", width - 1)
            .attr("dy", "1em")
            .style("text-anchor", "middle")
            .style("fill", "#ff782b")
            .html("&#8596; ");

        var leftArrow = context.append("text")
            .attr('class', 'help_label')

            .attr("y", 5)
            .attr("x", -11)
            .attr("dy", "1em")
            .style("text-anchor", "top")
            .style("fill", "#ff782b")
            .html("&#8596; Use mouse pointer on the slider boundaries to zoom in or out, focus on grey area to slide");


        leftArrow.transition()        // apply a transition
            .duration(3000)
            .attr('x', 45)     // move arrow
            .transition()        //
            .duration(2000)      //
            .attr('x', -11)     // move arrow
            .transition()        // apply a transition
            .duration(2000).transition()
            .duration(600)
            .remove();      // apply it over 2000 milliseconds

        rightArrow.transition()        // apply a transition
            .duration(3000)
            .attr('x', width - 48)     // move arrow
            .transition()        //
            .duration(2000)      //
            .attr('x', width - 1)     // move arrow
            .transition()        // apply a transition
            .duration(2000).transition()
            .duration(600)
            .remove();      // apply it over 2000 milliseconds


        myBrash.transition()       // apply a transition
            .duration(3000)
            .call(brush.move, [x.range()[0] + 50, x.range()[1] - 50])
            .transition()        // apply a transition again
            .duration(2000)
            .call(brush.move, x.range());


    }


    function selectTimelineForWork(d) {


        var coordinates = d3.mouse(this);
        var mx = Math.round(coordinates[0]);

        var selectedDate = x.invert(mx);

        // console.log("range: " + x3(x.invert(mx)));

        if (Array.isArray(d)) {

            var minR;
            var maxR;

            for (let i = 0; i < d.length; i++) {

                if (d[i] != null && selectedDate <= d[i].x && i > 0) {
                    maxR = d[i].x; //found max
                    break;

                } else if (d[i] != null) {
                    minR = d[i].x; //found previous min
                }
            }

            // console.log( "min - " + minR + " " + maxR);
        }

        //we found date and then need to find the range and position

        myBrash.transition()       // apply a transition
            .duration(2000)
            .call(brush.move, [x3(d3.timeDay.offset(minR, -10)), x3(d3.timeDay.offset(maxR, +10))]);

    }

    function selectTimelineForPeriod(d) {

        console.log( JSON.stringify(d, null, 2));


        myBrash.transition()       // apply a transition
            .duration(2000)
            .call(brush.move, [x3(d3.timeDay.offset(d.value.min_extent, -10)), x3(d3.timeDay.offset(d.value.max_extent, +10))]);

    }

    function update(updateData, label, fillStyle) {
        bars.selectAll("rect").remove();
        bars.selectAll("bar").remove();

        focus.selectAll(".y_label").text(label);

        focus.selectAll(".axis")
            .call(yAxis);

        bars.selectAll("bar")
            .data(updateData)
            .enter().append("rect")
            .attr('class', 'bar')
            .style("fill", fillStyle)
            .attr("x", function (d) {
                return x(d.date);
            })
            .attr("width", 5)
            .attr("y", function (d) {
                return y(d.letters);
            })
            .attr("height", function (d) {
                return height - y(d.letters);
            })
            .on("mouseover", function () { //<-A
                var position = d3.mouse(svg.node());
                // console.log(x.invert(position[0]));
            });

        // focus.append("text")
        //     .attr('class', 'y_label')
        //     .attr("transform", "rotate(-90)")
        //     .attr("y", 0 - margin.left)
        //     .attr("x", 0 - (height / 2))
        //     .attr("dy", "1em")
        //     .style("text-anchor", "middle")
        //     .text("Letters Updated");


    }

    function type(d) {
        d.date = parseDate(d.date);
        d.letters = +d.letters;
        return d;
    }

    function getAggregationPerMonth(dataByMonth) {

        var dataMonth = d3.nest()
            .key(function (d) { // <- A
                return monthFormatter(d.date);
            }).rollup(function (leaves) {
                return {
                    "letters": d3.sum(leaves, function (d) {
                        return d.letters;
                    })
                }
            })
            .entries(data); // <- C


        dataMonth.forEach(function (d) {

            var obj = {
                date: d.date = parseMonth(d.key),
                letters: d.letters = +d.value.letters
            };

            dataByMonth.push(obj);

        });
    }

    function getAggregationPerWeek(dataByWeek) {

        var dataWeek = d3.nest()
            .key(function (d) { // <- A
                return weekFormatter(d.date);
            }).rollup(function (leaves) {
                return {
                    "letters": d3.sum(leaves, function (d) {
                        return d.letters;
                    })
                }
            })
            .entries(data); // <- C

        dataWeek.forEach(function (d) {

            var obj = {
                date: d.date = parseWeek(d.key),
                letters: d.letters = +d.value.letters
            };

            dataByWeek.push(obj);

        });
    }


    function book_mouseout(book) {
        svg.selectAll('rect.continuation')
            .attr('class', 'continuation');
        svg.selectAll('rect.work-period')
            .attr('class', 'work-period');
    }

    function book_mouseover(book) {

        // console.log(JSON.stringify(eventFocus.selectAll("ellipse")
        //     .data(), null, 2));

        // eventFocus.selectAll("line_event")
        //     .attr("class", function (d) {
        //
        //         console.log(JSON.stringify(d, null, 2));
        //         // if (d["ru_title"] === book["key"]) {
        //         //     return "continuation highlight";
        //         // }
        //         // else {
        //         //     return "continuation";
        //         // }
        //     });


    }

    function book_click(book) {

        // console.log(JSON.stringify(book, null, 2));

        eventFocus.selectAll(".highlighter_line").remove();


        // eventFocus.selectAll("path")
        //     .attr("stroke", function (d, i) {
        //
        //         console.log(i + " - " + JSON.stringify(d, null, 2));
        //         return "gray";
        //     });
        //insert("div",":first-child");
        var highlighter_line = eventFocus.insert("line", ":first-child").attr('class', 'highlighter_line');

        selected_line_coordinates = {
            key: book.key,
            x1: book.value.min_extent,
            x2: book.value.max_extent,
            y: book.value.band_y_pos,
            title: book.value.en_title
        };

        highlighter_line.attr("x1", x(selected_line_coordinates.x1))
            .attr("y1", selected_line_coordinates.y)
            .attr("x2", x(selected_line_coordinates.x2))
            .attr("y2", selected_line_coordinates.y);

        highlighter_line.on("mouseover", function (d) {

            toolTip.transition()
                .duration(200)
                .style("opacity", .9);
            if (location != "ru") {

                toolTip.text(selected_line_coordinates.title)
                    .style("left", (d3.event.pageX) + "px")
                    .style("top", (d3.event.pageY - 30) + "px");
            } else
            {
                toolTip.text(selected_line_coordinates.key)
                    .style("left", (d3.event.pageX) + "px")
                    .style("top", (d3.event.pageY - 30) + "px");
            }

        })
            .on("mouseout", function (d) {
                toolTip.transition()
                    .duration(500)
                    .style("opacity", 0);
            })


    }


    function buildAllEvents() {


        var bandScale = d3.scaleLinear()
            .domain([0, 20])
            .range([height / 1.4, 0]);

        var bandMap = new Map();

        //todo total # of bands should be calculated
        for (let i = 1; i < 25; i++) {

            bandMap.set(Math.round(bandScale(i)), []);
            //bandMap.set(bandScale(i), []);
        }

        var format = d3.timeFormat("%Y-%m-%d");

        var duration = 500;

        var z = d3.scaleOrdinal(d3.schemePaired);

        // var div = d3.select("body").append("div")
        //     .attr("class", "tooltip")
        //     .style("opacity", 0);


        //we need to send here lines and titles - x position needs to be figured out
        function renderEvents(tension, ind, listOfEvents, events, color, title) {

            var lineGraph = eventFocus.append("g");

            lineGraph.attr("clip-path", "url(#clip)");

            lineGraph.append("path")
                .data([listOfEvents])
                .attr("class", "line_event")
                .attr("d", lineEventFunction)
                .attr("stroke", color)
                .attr("stroke-width", 6)
                .attr("fill", "none").on("click", selectTimelineForWork);

            var eventElipse = eventFocus.append("g");

            eventElipse.attr("clip-path", "url(#clip)");

            eventElipse.selectAll("ellipse.circle_event" + ind)
                .data(events)
                .enter().append("ellipse")
                .attr("class", "circle_event" + ind)
                .on("mouseover", function (d) {
                    div.transition()
                        .duration(200)
                        .style("opacity", .9);
                    div.html(title + " - " + d.name + " " + d.comment)
                        .style("left", (d3.event.pageX) + "px")
                        .style("top", (d3.event.pageY - 30) + "px");
                })
                .on("mouseout", function (d) {
                    div.transition()
                        .duration(500)
                        .style("opacity", 0);
                });

            eventElipse.selectAll("ellipse.circle_event" + ind)
                .data(events)
                .transition().duration(duration)
                .ease(d3.easeLinear).attr("cx", function (d) {
                return x(d.x);
            })
                .attr("cy", function (d) {
                    return d.y;
                })
                .attr("rx", 4.2)           // set the x radius
                .attr("ry", 4.1).attr("stroke", color).attr("fill", color);

        }

        function mapWorkEvents(d) {

            if (book_titles) {
                book_titles.length = 0;
            }
            //sort all works based on fist event
            d.sort((a, b) => {
                var aMin = d3.min(a.events, function (f) {
                    return new Date(f.start);
                });

                var bMin = d3.min(b.events, function (f) {
                    return new Date(f.start);
                });

                return aMin - bMin;

            });


            d.forEach((work, i) => {

                var events = [];    //for dots

                var listOfEvents = [];  //for lines

                //find range of events for the entire work
                var eMin = d3.min(work.events, function (f) {
                    return new Date(f.start);
                });

                var eMax = d3.max(work.events, function (f) {
                    return new Date(f.end);
                });


                var eventCompleteScale = d3.scaleUtc()
                    .domain([eMin, eMax])
                    .range([0, 1]);


                //find band for each work. It can occupy same line if they are not overlapping
                var bandYpos = freeBand(eMin, eMax);

                bandMap.get(bandYpos).push(eventCompleteScale);

                work.events.forEach(event => {
                    //console.log(new Date(event.start));

                    //draw a line from start to end
                    var startPoint = {
                        x: new Date(event.start),
                        y: bandYpos
                    };
                    var endPoint = {
                        x: new Date(event.end),
                        y: bandYpos
                    };

                    listOfEvents.push(startPoint);
                    listOfEvents.push(endPoint);
                    listOfEvents.push(null);

                    events.push({
                        x: new Date(event.start),
                        y: bandYpos,
                        name: event.event_title,
                        comment: event.comment + "[" + format(new Date(event.start)) + ":" + format(new Date(event.end)) + "]"
                    });
                    events.push({
                        x: new Date(event.end),
                        y: bandYpos,
                        name: event.event_title,
                        comment: event.comment + "[" + format(new Date(event.start)) + ":" + format(new Date(event.end)) + "]"
                    });


                });

                var book_entry = {
                    key: work.oritinalTitle,
                    value: {
                        max_extent: eMax,
                        min_extent: eMin,
                        en_title: work.translation.en,
                        ru_title: work.oritinalTitle,
                        band_y_pos: bandYpos,
                        events: work.events
                    }
                };

                book_titles.push(book_entry);

                var workTitle = work.oritinalTitle;

                if (location != "ru") {
                    //get translation from another language
                    workTitle = work.translation[location];
                }


                renderEvents(1, i, listOfEvents, events, z(work.oritinalTitle), workTitle);

            });

            book_titles.sort(function (a, b) {
                if (location != "ru") {
                    a_title = a["value"]["en_title"];
                    b_title = b["value"]["en_title"];
                    return a_title.localeCompare(b_title, 'en');
                } else
                {
                    a_title = a["value"]["ru_title"];
                    b_title = b["value"]["ru_title"];
                    return a_title.localeCompare(b_title, 'ru');
                }
            });


        }

        var freeBand = function testIfInBand(myDate1, myDate2) {

            for (const key of bandMap.keys()) {

                var notInBand = true;

                bandMap.get(key).forEach(m => {

                    var inRange1 = m(myDate1);

                    var inRange2 = m(myDate2);

                    if ((inRange1 >= 0 && inRange1 <= 1) || (inRange2 >= 0 && inRange2 <= 1)) {
                        notInBand = false
                    }
                });

                if (notInBand) {
                    return key;
                }
            }
        };


        // d3.json("data/events.json").then(function (d) {

        //console.log(JSON.stringify(allEventsParsed, null, 2));

        mapWorkEvents(JSON.parse(dataForEvents));


        // });
    }


    function buildTitlesList() {

        var local_books = book_titles.filter(function (d) {
            //return x(d["value"]["min_extent"]) < width && x(d["value"]["max_extent"]) > 0;

            var matchingPeriod = false;

            matchingPeriod = d.value.events.some(function (el, i) {
                //return test.call(ctx, el, i, arr) ? ((result = el), true) : false;

                return x(new Date(el.start)) < width && x(new Date(el.start)) > 0;

            });

            return matchingPeriod;

        });
        //load list of books in scroll panel

        bookUl.selectAll("div").remove();
        bookUl.selectAll("div")
            .data(local_books)
            .enter()
            .append("div")
            .attr('class', 'book_list_items')
            .style("color", function (d) {

                if (selected_line_coordinates && d.key == selected_line_coordinates.key) {
                    return '#ff0014';
                } else {
                    return '#256f6a';
                }

            })
            .text(function (d) {
                if (location != "ru") {
                    return d["value"]["en_title"];
                } else
                {
                    return d["value"]["ru_title"];
                }
            })
            .on('mouseover', function (d) {
                book_mouseover(d);
            })
            .on('mouseout', function (d) {
                book_mouseout(d);
            })
            .on('click', function (d) {
                bookUl.selectAll("div").style('color', '#256f6a');

                eventFocus.selectAll(".highlighter_line").remove();

                if (selected_line_coordinates && d.key == selected_line_coordinates.key) {
                    selected_line_coordinates.key="xyz";

                } else {
                    d3.select(this).style('color', '#ff0014');

                    book_click(d);
                }

            }).on('dblclick', selectTimelineForPeriod);

    }

    //finally if we have a date range already selected in session use it
    if (Array.isArray(startAndEndDates) && startAndEndDates[0] !== null && startAndEndDates[0].length > 0) {
        myBrash
            .call(brush.move, [x3(parseDate(startAndEndDates[0])), x3(parseDate(startAndEndDates[1]))]);
    }

    buildAllEvents();

    buildTitlesList();


}


function buildScatterPlotChart(divId, dataIn, replace) {

    var toolTip = d3.select('.tooltip');

    if (toolTip.empty()) {
        toolTip = d3.select("body").append("div")
            .attr("class", "tooltip")
            .style("opacity", 0);
    } else {

        toolTip.transition()
            .duration(0)
            .style("opacity", 0);
        toolTip.html("")
    }


    if (replace) {
        dots.selectAll(".dot").remove();
    }


    if (dataIn) {
        var parseDate = d3.timeParse("%Y-%m-%d");     //converts to date
        //that creates a json data object that contains objects with date and number of words as params and corresponding values
        data2 = d3.csvParse(dataIn, function (d) {
            return {
                date: d.date = parseDate(d.date),
                words: d.words = +d.words,
                id: d.id,
                info: d.info
            };
        });


        dots.attr("clip-path", "url(#clip)");

        dots.selectAll("dot")
            .data(data2)
            .enter().append("circle")
            .attr('class', 'dot')
            .attr("r", 4)
            .style("opacity", .9)
            .style('fill', '#ff8400')


            .attr("cx", function (d) {
                return x(d.date);
            })
            .attr("cy", function (d) {
                if (d.words > 1000) {
                    return z(Math.log10(1000));
                } else {
                    return z(Math.log10(d.words));
                }
            })
            // .on("mouseover", function () { //<-A
            //     var position = d3.mouse(dots.node());
            //     // console.log(x.invert(position[0]));
            // })
            .on("mouseover", function (d) {

                var position = d3.mouse(dots.node());
                //   console.log(d3.event.pageX);
                toolTip.transition()
                    .duration(200)
                    .style("opacity", .9);
                toolTip.text(d.info)
                    .style("left", (d3.event.pageX) + "px")
                    .style("top", (d3.event.pageY - 30) + "px");
            })
            .on("mouseout", function (d) {
                toolTip.transition()
                    .duration(500)
                    .style("opacity", 0);
            })
            .on("click", function (d) {
                toolTip.transition()
                    .duration(500)
                    .style("opacity", 0);
                d3.select(this).style('fill', '#f2014f');

                // console.log(JSON.stringify(d, null, 2));
                goToAnchor(d.id);
                // us.colloquy.tolstoy.client.uplink.DataUplink.lookupDocument(d.id);

            });
    }
}

function goToAnchor(anchor) {
    // var loc = document.location.toString().split('#')[0];
    //
    // document.location = loc + '#' + anchor;
    // return false;


    location.href = '#' + anchor;
    // var elmnt = document.getElementById(anchor);
    // // elmnt.scrollIntoView();
    //
    // var event = new Event('target');
    // elmnt.dispatchEvent(event);

    return false;
}