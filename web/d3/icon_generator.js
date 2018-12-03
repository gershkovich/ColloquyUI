function generateSiteIcons(divId, labelText, helpText) {

    var div = d3.select(divId);

    var toolTipdiv = d3.select("body").append("div")
        .attr("class", "tooltip")
        .style("opacity", 0);

    var svg = d3.select(".circleContainer").append("svg").attr("width", 160)
        .attr("height", 140)
    var circle_g = svg.append("g");

    circle_g.attr('transform', 'translate(36, 10)');

    var filter = svg.append("defs")
        .append("filter")
        .attr("id", "drop-shadow");


    filter.append("feGaussianBlur")
        .attr("stdDeviation", 3)
        .attr("in", "SourceAlpha");

    filter.append("feOffset")
        .attr("dx", 2)
        .attr("dy", 4);

    var feMerge = filter.append("feMerge");
    feMerge.append("feMergeNode");

    feMerge.append("feMergeNode")
        .attr("in", "SourceGraphic");


    var circle = circle_g.append("circle")
        .attr("class", "enter_circle")
        .attr("cx", 60).attr("cy", 60)
        .attr("r", 50);


    circle.on("mouseover", function (d, i) {
        d3.select(this).style("filter", "url(#drop-shadow)");

    });

    circle.on("mouseout", function (d, i) {
        d3.select(this).style("filter", null);

    });


    var helpText = circle_g.append("text")
        .attr("class", "enter_text")
        .attr("x", 60)
        .attr("y", 60)
        .text(labelText);

    helpText.on("mouseover", function (d) {

        toolTipdiv.transition()
            .duration(100)
            .style("opacity", .9);
        toolTipdiv.html("Please see video introduction for more details.")
            .style("left", (d3.event.pageX) - 120 + "px")
            .style("top", (d3.event.pageY - 10) + "px");
    })
        .on("mouseout", function (d) {
            toolTipdiv.transition()
                .duration(500)
                .style("opacity", 0);
        });



};

function addDiscussBlock() {
     

/**
 *  RECOMMENDED CONFIGURATION VARIABLES: EDIT AND UNCOMMENT THE SECTION BELOW TO INSERT DYNAMIC VALUES FROM YOUR PLATFORM OR CMS.
 *  LEARN WHY DEFINING THESE VARIABLES IS IMPORTANT: https://disqus.com/admin/universalcode/#configuration-variables*/


var disqus_config = function () {
this.page.url = "colloquy.us";  // Replace PAGE_URL with your page's canonical URL variable
this.page.identifier = "comments"; // Replace PAGE_IDENTIFIER with your page's unique identifier variable
};

(function() { // DON'T EDIT BELOW THIS LINE
    var d = document, s = d.createElement('script');
    s.src = 'https://colloquy-us.disqus.com/embed.js';
    s.setAttribute('data-timestamp', +new Date());
    (d.head || d.body).appendChild(s);
})();

 }

//generateSiteIcons("circleContainer", "ENTER", "Use introduction for more info.");