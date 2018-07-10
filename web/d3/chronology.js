function buildChronologyChart(divId, dataIn, documentType) {


        var margin = { top: 20, right: 20, bottom: 80, left: 50 },
                margin2 = { top: 150, right: 20, bottom: 20, left: 50 },
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

        var data_event = [
                { x: "1870-2-24", y: 1 },
                { x: "1877-2-24", y: 1 },
                { x: "1877-2-24", y: 0 },
                { x: "1870-2-24", y: 0 }
        ];

        var workRecord = [{ "title": { "en": "Childhood", "ru": "\u0414\u0435\u0442\u0441\u0442\u0432\u043e" },
                        "pub": 1852.0, 
                        "work": [{ "precision": "accurate", "detail": NaN, "breaks": false, "start": "1851-01-18", "activity": "work", "end": "1852-07-03" }] },
                      { "title": { "en": "A History of Yesterday", "ru": "\u0418\u0441\u0442\u043e\u0440\u0438\u044f \u0432\u0447\u0435\u0440\u0430\u0448\u043d\u0435\u0433\u043e \u0434\u043d\u044f" }, "pub": 1928.0, "work": [{ "precision": "accurate", "detail": NaN, "breaks": false, "start": "1851-03", "activity": "work", "end": "1851-03" }] }, { "title": { "en": "Boyhood ", "ru": "\u041e\u0442\u0440\u043e\u0447\u0435\u0441\u0442\u0432\u0430" }, "pub": 1854.0, "work": [{ "precision": "accurate", "detail": NaN, "breaks": false, "start": "1852-11-29", "activity": "work", "end": "1854-03-14" }] }, { "title": { "en": "Youth ", "ru": "\u042e\u043d\u043e\u0441\u0442\u044c" }, "pub": 1857.0, "work": [{ "precision": "accurate", "detail": NaN, "breaks": false, "start": "1855-03-12", "activity": "work", "end": "1856-09-24" }] }, { "title": { "en": "The Raid", "ru": "\u041d\u0430\u0431\u0435\u0433" }, "pub": 1853.0, "work": [{ "precision": "accurate", "detail": NaN, "breaks": false, "start": "1852-06-15", "activity": "work", "end": "1852-12-24" }] }, { "title": { "en": "A Billiard-Marker's Notes", "ru": "\u0417\u0430\u043f\u0438\u0441\u043a\u0438 \u043c\u0430\u0440\u043a\u0451\u0440\u0430" }, "pub": 1855.0, "work": [{ "precision": "accurate", "detail": NaN, "breaks": false, "start": "1853-09-13", "activity": "work", "end": "1854-02" }] }, { "title": { "en": "The Wood-Felling", "ru": "\u0420\u0443\u0431\u043a\u0430 \u043b\u0435\u0441\u0430" }, "pub": 1855.0, "work": [{ "precision": "approximate", "detail": NaN, "breaks": false, "start": "1854-07-01", "activity": "work", "end": "1854-09-01" }, { "precision": "accurate", "detail": NaN, "breaks": false, "start": "1855-06-01", "activity": "work", "end": "1855-07-18" }] }, { "title": { "en": "Sevastopol in December 1854", "ru": "\u0421\u0435\u0432\u0430\u0441\u0442\u043e\u043f\u043e\u043b\u044c \u0432 \u0434\u0435\u043a\u0430\u0431\u0440\u0435 \u043c\u0435\u0441\u044f\u0446\u0435" }, "pub": 1855.0, "work": [{ "precision": "accurate", "detail": NaN, "breaks": false, "start": "1855-03-15", "activity": "work", "end": "1855-04-25" }] }, { "title": { "en": "Sevastopol in May 1855", "ru": "\u0421\u0435\u0432\u0430\u0441\u0442\u043e\u043f\u043e\u043b\u044c \u0432 \u043c\u0430\u0435" }, "pub": 1855.0, "work": [{ "precision": "accurate", "detail": NaN, "breaks": false, "start": "1855-07-18", "activity": "work", "end": "1855-07-26" }] }, { "title": { "en": "Sevastopol in August 1855", "ru": "\u0421\u0435\u0432\u0430\u0441\u0442\u043e\u043f\u043e\u043b\u044c \u0432 \u0430\u0432\u0433\u0443\u0441\u0442\u0435 1855 \u0433\u043e\u0434\u0430" }, "pub": 1856.0, "work": [{ "precision": "accurate", "detail": NaN, "breaks": false, "start": "1855-09-19", "activity": "work", "end": "1855-12-27" }] }, { "title": { "en": "The Snowstorm", "ru": "\u041c\u0435\u0442\u0435\u043b\u044c" }, "pub": 1856.0, "work": [{ "precision": "accurate", "detail": NaN, "breaks": false, "start": "1854-01-24", "activity": "conception ", "end": "1854-01-24" }, { "precision": "accurate", "detail": NaN, "breaks": false, "start": "1856-01-15", "activity": "work", "end": "1856-02-12" }] }, { "title": { "en": "Two Hussars", "ru": "\u0414\u0432\u0430 \u0433\u0443\u0441\u0430\u0440\u0430" }, "pub": 1856.0, "work": [{ "precision": "accurate", "detail": NaN, "breaks": false, "start": "1856-03-12", "activity": "work", "end": "1856-04-14" }] }, { "title": { "en": NaN, "ru": "\u0418\u0437 \u043a\u0430\u0432\u043a\u0430\u0437\u0441\u043a\u0438\u0445 \u0432\u043e\u0441\u043f\u043e\u043c\u0438\u043d\u0430\u043d\u0438\u0439. \u0420\u0430\u0437\u0436\u0430\u043b\u043e\u0432\u0430\u043d\u043d\u044b\u0439" }, "pub": 1856.0, "work": [{ "precision": "approximate", "detail": NaN, "breaks": false, "start": "1853-12-15", "activity": "conception", "end": "1854-12-15" }, { "precision": "accurate", "detail": NaN, "breaks": false, "start": "1856-11-01", "activity": "work", "end": "1856-11-30" }] }, { "title": { "en": "A Landlord's Morning", "ru": "\u0423\u0442\u0440\u043e \u043f\u043e\u043c\u0435\u0449\u0438\u043a\u0430" }, "pub": 1856.0, "work": [{ "precision": "approximate", "detail": NaN, "breaks": false, "start": "1852-06-15", "activity": "conception", "end": "1852-08-15" }, { "precision": "approximate", "detail": NaN, "breaks": true, "start": "1852-08-15", "activity": "work", "end": "1856-11-29" }] }, { "title": { "en": "Lucerne", "ru": "\u041b\u044e\u0446\u0435\u0440\u043d" }, "pub": 1857.0, "work": [{ "precision": "accurate", "detail": "written in Switzerland ", "breaks": false, "start": "1857-07-07", "activity": "work", "end": "1857-07-11" }] }, { "title": { "en": "Albert ", "ru": "\u0410\u043b\u044c\u0431\u0435\u0440\u0442" }, "pub": 1858.0, "work": [{ "precision": "accurate", "detail": NaN, "breaks": false, "start": "1857-01-07", "activity": "conception", "end": "1858-01-07" }, { "precision": "accurate", "detail": NaN, "breaks": true, "start": "1857-01-07", "activity": "work ", "end": "1858-03-17" }] }, { "title": { "en": "Three Deaths", "ru": "\u0422\u0440\u0438 \u0441\u043c\u0435\u0440\u0442\u0438" }, "pub": 1859.0, "work": [{ "precision": "accurate", "detail": NaN, "breaks": false, "start": "1858-01-15", "activity": "work", "end": "1858-01-24" }] }, { "title": { "en": "Family Happiness", "ru": "\u0421\u0435\u043c\u0435\u0439\u043d\u043e\u0435 \u0441\u0447\u0430\u0441\u0442\u0438\u0435" }, "pub": 1859.0, "work": [{ "precision": "approximate", "detail": NaN, "breaks": false, "start": "1857-08-16", "activity": "conception", "end": "1857-08-16" }, { "precision": "approximate", "detail": NaN, "breaks": false, "start": "1858-10-30", "activity": "work", "end": "1858-04-09" }] }, { "title": { "en": "The Cossacks", "ru": "\u041a\u0430\u0437\u0430\u043a\u0438" }, "pub": 1864.0, "work": [{ "precision": "approximate", "detail": "first concieved in verse", "breaks": false, "start": "1852-05-10", "activity": "conception", "end": "1853-10-21" }, { "precision": "approximate", "detail": NaN, "breaks": true, "start": "1853-08-28", "activity": "work (1st period)", "end": "1854-01-01" }, { "precision": "approximate", "detail": NaN, "breaks": true, "start": "1857-05-15", "activity": "work (2nd  period)", "end": "1858-12-31" }, { "precision": "approximate", "detail": NaN, "breaks": false, "start": "1863-12-01", "activity": "work (3rd period)", "end": "1864-01-01" }] }, { "title": { "en": "Polik\u00fashka", "ru": "\u041f\u043e\u043b\u0438\u043a\u0443\u0448\u043a\u0430" }, "pub": 1863.0, "work": [{ "precision": "approximate", "detail": NaN, "breaks": false, "start": "1861-03-15", "activity": "conception", "end": "1862-03-15" }, { "precision": "approximate", "detail": NaN, "breaks": true, "start": "1861-05-06", "activity": "work", "end": "1862-10-15" }] }, { "title": { "en": "The Decembrists", "ru": "\u0414\u0435\u043a\u0430\u0431\u0440\u0438\u0441\u0442\u044b" }, "pub": NaN, "work": [{ "precision": NaN, "detail": NaN, "breaks": false, "start": "", "activity": NaN, "end": "" }] }, { "title": { "en": "Tikhon and Malania", "ru": "\u0422\u0438\u0445\u043e\u043d \u0438 \u041c\u0430\u043b\u0430\u043d\u044c\u044f" }, "pub": 1911.0, "work": [{ "precision": "approximate", "detail": "Unfinished", "breaks": true, "start": "1860-11-15", "activity": "work", "end": "1862-12-15" }] }, { "title": { "en": NaN, "ru": "\u0418\u0434\u0438\u043b\u043b\u0438\u044f: \u041e\u043d\u043e \u0437\u0430\u0440\u0430\u0431\u043e\u0442\u043a\u0438 \u0445\u043e\u0440\u043e\u0448\u043e, \u0434\u0430 \u0438 \u0433\u0440\u0435\u0445 \u0431\u044b\u0432\u0430\u0435\u0442 \u043e\u0442 \u0442\u043e\u0433\u043e" }, "pub": 1911.0, "work": [{ "precision": "approximate", "detail": "Unfinished", "breaks": true, "start": "1860-05-25", "activity": "work", "end": "1860-10-28" }] }, { "title": { "en": "War and Peace ", "ru": "\u0412\u043e\u0439\u043d\u0430 \u0438 \u043c\u0438\u0440 " }, "pub": NaN, "work": [{ "precision": "approximate", "detail": NaN, "breaks": false, "start": "1863-02-15", "activity": "work on what Tolstoy referred to as the novel of \"1812\" ", "end": "1864-02-15" }, { "precision": "accurate", "detail": "Part I completed, send to Russkii Vestnik", "breaks": false, "start": "1864-02-15", "activity": "work", "end": "1865-01-03" }, { "precision": "approximate", "detail": "correcting proofs of part I", "breaks": false, "start": "1865-01-03", "activity": "work", "end": "1865-02-28" }, { "precision": "accurate", "detail": "Part III", "breaks": false, "start": "1865-11-01", "activity": "work", "end": "1866-01-01" }, { "precision": "approximate", "detail": "Part IV", "breaks": false, "start": "1866-01-15", "activity": "work", "end": "1866-02-15" }] }, { "title": { "en": "War and Peace ", "ru": "\u0432\u043e\u0439\u043d\u0430 \u0438 \u043c\u0438\u0440 " }, "pub": NaN, "work": [{ "precision": "accurate", "detail": "Part II", "breaks": false, "start": "1865-03-01", "activity": "work", "end": "1865-11-01" }] }]

        var work_rectangles = getWorkRectangles(workRecord);

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
                        return x(d.date) + margin.left;
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
                .text(documentType).style("font-size", "0.7em").style("fill", "#113f6c");

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
                        return x2(d.date) + margin.left;
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

                var monthsDiff = d3.timeMonth.count(x.domain()[0], x.domain()[1]);

                //todo call function to load letters by range and selected text


                //   tstUplink(formatDate(x.domain()[0]) , formatDate(x.domain()[1]));


                // console.log("month diff: " + monthsDiff);

                if (monthsDiff <= 40 && agg !== "days") {
                        agg = "days";

                        //switch to days

                        y.domain([0, d3.max(data, function (d) {
                                return d.letters;
                        })]);
                        update(data, "Letter per day", "DarkKhaki");


                } else if (monthsDiff > 40 && monthsDiff < 100 && agg !== "weeks") {
                        agg = "weeks";

                        y.domain([0, d3.max(dataByWeek, function (d) {
                                return d.letters;
                        })]);
                        //switch to moths
                        update(dataByWeek, "Letter per week", "CadetBlue");

                }
                else if (monthsDiff >= 100 && agg !== "months") {
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
                                return x(d.date) + margin.left;
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

        function update(updateData, label, fillStyle) {
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
                                return x(d.date) + margin.left;
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

        function getWorkRectangles(work_periods){
                work_rect = []
                for (title in work_periods){
                        title_work = {"title": title, rect: []};
                        title_work_rect = []
                        for (period in title["work"]){
                                title_work_rect.push([
                                        { x: period["start"], y: 1},
                                        { x: period["end"], y: 1 },
                                        { x: period["end"], y: 0 },
                                        { x: period["start"], y: 0 }
                                ])
                        }
                        work_rect.push(title_rect);
                }
                return null;
        }

        renderAxes(svg);

        // console.log("here");

        render(1);



        function render(tension) {

                var line = d3.line()
                        .curve(d3.curveCardinal.tension(tension)) // <-A
                        .x(function (d) { return x(parseDate(d.x)) + margin.left; })
                        .y(function (d) { return y_event(d.y); });

                svg.selectAll("path.line_box")
                        .data([tension])
                        .enter()
                        .append("path")
                        .attr("class", "line_box");

                svg.selectAll("path.line_box")
                        .data([tension])
                        .transition().duration(duration)
                        .ease(d3.easeLinear) // <-B
                        .attr("d", function (d) {
                                return line(data_event); // <-C
                        });



                svg.selectAll("ellipse")
                        .data(events)
                        .enter().append("ellipse")
                        .attr("class", "circle_event")
                        .on("mouseover", function (d) {
                                div.transition()
                                        .duration(200)
                                        .style("opacity", .9);
                                div.html(d.name)
                                        .style("left", (d3.event.pageX) + "px")
                                        .style("top", (d3.event.pageY - 30) + "px");
                        })
                        .on("mouseout", function (d) {
                                div.transition()
                                        .duration(500)
                                        .style("opacity", 0);
                        });



                svg.selectAll("ellipse")
                        .data(events)
                        .transition().duration(duration)
                        .ease(d3.easeLinear).attr("cx", function (d) { return x(parseDate(d.x)) + margin.left; })
                        .attr("cy", function (d) { return y_event(d.y); })
                        .attr("rx", 5)           // set the x radius
                        .attr("ry", 4);

        }

        function renderAxes(svg) {


                var yAxis_event = d3.axisLeft()
                        .scale(d3.scaleLinear().range([10, 0])).ticks(1);


                // svg.append("g")
                //     .attr("class", "y_axis_event")
                //     .attr("transform", function(){
                //         return "translate(" + margin.left + "," + 10 + ")";
                //     })
                //     .call(yAxis_event);
        }

        function xStart() {
                return margin.left;
        }


        function yEnd() {
                return margin.left;
        }


        function quadrantHeight() {
                return height;
        }

}


