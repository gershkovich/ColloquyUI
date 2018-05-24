function buildChronologyChart(divId, dataIn, documentType) {


    var margin = {top: 20, right: 20, bottom: 80, left: 50},
        margin2 = {top: 150, right: 20, bottom: 20, left: 50},
        width = 1160 - margin.left - margin.right,
        height = 200 - margin.top - margin.bottom,
        height2 = 200 - margin2.top - margin2.bottom;

    var parseDate = d3.timeParse("%Y-%m-%d");

    var formatDate = d3.timeFormat("%Y-%m-%d");

    var monthFormatter = d3.timeFormat('%Y-%b');

    var weekFormatter = d3.timeFormat('%Y %W');

    var parseMonth = d3.timeParse("%Y-%b");

    var parseWeek = d3.timeParse("%Y %W");

    var x = d3.scaleTime().range([0, width]),
        x2 = d3.scaleTime().range([0, width]),
        y = d3.scaleLinear().range([height, 0]),
        y2 = d3.scaleLinear().range([height2, 0]);

    var xAxis = d3.axisBottom(x),
        xAxis2 = d3.axisBottom(x2),
        yAxis = d3.axisLeft(y).ticks(5);

    var brush = d3.brushX()
        .extent([[0, 0], [width, height2]])
        .on("brush", brushed);

    var svg = d3.select(divId).append("svg")
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


    var y_event = d3.scaleLinear()
        .domain([0, 10])
        .range([height - margin.bottom, margin.bottom]);

    var duration = 500;

    var  data_event =  [
        {x: "1870-2-24", y: 1},
        {x: "1877-2-24", y: 1},
        {x: "1877-2-24", y: 0},
        {x: "1870-2-24", y: 0}
    ];

    var  events =  [
        {x: "1870-2-24", y: 0.5, name: "Anna Karenina: conception "},
        {x: "1873-12-13", y: 0.5, name: "First full draft, 3rd period of intense work"},
        {x: "1877-2-24", y: 0.5, name: "Anna Karenina published"}


    ];

    var div = d3.select("body").append("div")
        .attr("class", "tooltip")
        .style("opacity", 0);


    var dataByMonth = [];

    getAggregationPerMonth(dataByMonth);


    var dataByWeek = [];

    getAggregationPerWeek(dataByWeek);

    //now rewrite data

    // document.write(dataFromString.date);

    x.domain(d3.extent(dataByMonth, function (d) {
        return d.date;
    }));
    y.domain([0, d3.max(dataByMonth, function (d) {
        return d.letters;
    })]);
    x2.domain(x.domain());
    y2.domain(y.domain());

// append scatter plot to main chart area

    var bars = focus.append("g");

    bars.attr("clip-path", "url(#clip)");


    bars.selectAll("bar")
        .data(dataByMonth)
        .enter().append("rect")
        .attr('class', 'dot')
        .style("fill", "steelblue")
        .attr("x", function (d) {
            return x(d.date) +margin.left;
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
            console.log(x.invert(position[0]));
        });

    focus.append("g")
        .attr("class", "axis axis--x")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

    focus.append("g")
        .attr("class", "axis axis--y")
        .call(yAxis);

    focus.append("text")
        .attr('class', 'y_label')
        .attr("transform", "rotate(-90)")
        .attr("y", 0 - margin.left)
        .attr("x", 0 - (height / 2))
        .attr("dy", "1em")
        .style("text-anchor", "middle")
        .text(documentType).style("font-size","0.7em").style("fill","#113f6c");

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
        .attr('class', 'dotContext')
        .style("fill", "gray")
        .attr("x", function (d) {
            return x2(d.date) +margin.left;
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

    // });

//create brush function redraw scatterplot with selection


    function brushed() {

        var selection = d3.event.selection;

        x.domain(selection.map(x2.invert, x2));

        render(1);

        //depending on distance between  x2.invert, x2 change data aggregation

        var monthsDiff = d3.timeMonth.count(x.domain()[0],x.domain()[1]);

        //todo call function to load letters by range and selected text


     //   tstUplink(formatDate(x.domain()[0]) , formatDate(x.domain()[1]));
    

       // console.log("month diff: " + monthsDiff);

        if (monthsDiff <= 40 && agg !== "days")
        {
            agg = "days";

            //switch to days

            y.domain([0, d3.max(data, function (d) {
                return d.letters;
            })]);
            update(data, "Letter per day", "DarkKhaki");


        }  else if ( monthsDiff > 40 && monthsDiff < 100  && agg !== "weeks")
        {
            agg = "weeks";

            y.domain([0, d3.max(dataByWeek, function (d) {
                return d.letters;
            })]);
            //switch to moths
            update(dataByWeek, "Letter per week", "CadetBlue");

        }
        else if (monthsDiff >= 100 && agg !== "months")
        {
            agg = "months";

            y.domain([0, d3.max(dataByMonth, function (d) {
                return d.letters;
            })]);
            //switch to moths
            update(dataByMonth, "Letter per month", "SteelBlue");

        } 
       // console.log(x.invert(position[0]));

        focus.selectAll(".dot")
            .attr("x", function (d) {
                return x(d.date)+margin.left;
            })
            .attr("width", 5)
            .attr("y", function (d) {
                return y(d.letters);
            })
            .attr("height", function (d) {
                return height - y2(d.letters);
            });

        focus.select(".axis--x").call(xAxis);


        
    }

    function update(updateData, label, fillStyle)
    {
        bars.selectAll("rect").remove();
        bars.selectAll("bar").remove();

        focus.selectAll(".y_label").text(label);

        // focus.selectAll(".axis#axis--y").remove();
        //
        // focus.append("g")
        //     .attr("class", "axis axis--y")
        //     .call(yAxis);

        focus.selectAll(".axis")
            .call(yAxis);

        bars.selectAll("bar")
            .data(updateData)
            .enter().append("rect")
            .attr('class', 'dot')
            .style("fill", fillStyle)
            .attr("x", function (d) {
                return x(d.date)+margin.left;
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
                console.log(x.invert(position[0]));
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

  //  tstUplink(formatDate(x.domain()[0]) , formatDate(x.domain()[1]));

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



    renderAxes(svg);

   // console.log("here");

    render(1);



    function render(tension){

        var line = d3.line()
            .curve(d3.curveCardinal.tension(tension)) // <-A
            .x(function(d){return x(parseDate(d.x)) + margin.left;})
            .y(function(d){return y_event(d.y);});

        svg.selectAll("path.line_box")
            .data([tension])
            .enter()
            .append("path")
            .attr("class", "line_box");

        svg.selectAll("path.line_box")
            .data([tension])
            .transition().duration(duration)
            .ease(d3.easeLinear) // <-B
            .attr("d", function(d){
                return line(data_event); // <-C
            });



        svg.selectAll("ellipse")
            .data(events)
            .enter().append("ellipse")
            .attr("class", "circle_event")
            .on("mouseover", function(d) {
            div.transition()
                .duration(200)
                .style("opacity", .9);
            div.html(d.name )
                .style("left", (d3.event.pageX) + "px")
                .style("top", (d3.event.pageY - 30) + "px");
        })
            .on("mouseout", function(d) {
                div.transition()
                    .duration(500)
                    .style("opacity", 0);
            });



        svg.selectAll("ellipse")
            .data(events)
            .transition().duration(duration)
            .ease(d3.easeLinear).attr("cx", function(d) { return x(parseDate(d.x)) +margin.left; })
            .attr("cy", function(d) { return y_event(d.y); })
            .attr("rx", 5)           // set the x radius
            .attr("ry", 4);

    }

    function renderAxes(svg){


        var yAxis_event = d3.axisLeft()
            .scale(d3.scaleLinear().range([10, 0])).ticks(1);


        // svg.append("g")
        //     .attr("class", "y_axis_event")
        //     .attr("transform", function(){
        //         return "translate(" + margin.left + "," + 10 + ")";
        //     })
        //     .call(yAxis_event);
    }

    function xStart(){
        return margin.left;
    }


    function yEnd(){
        return margin.left;
    }


    function quadrantHeight(){
        return height;
    }

}


