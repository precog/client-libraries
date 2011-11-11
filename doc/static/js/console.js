$(function() {
  var r = function(x) { return Math.floor(Math.random() * x); };

  var rdist = function(values) {
    var id = Math.floor((r(values.length) + r(values.length)) / 2)
    return values[id];
  };

  var randomTime = function() { 
    var date = new Date();
    date.setHours(Math.floor(Math.random() * date.getHours()) + 1); 
    date.setMinutes(Math.floor(Math.random() * 60) + 1); 
    return Math.random() < 0.90 ? date.getTime() : true;
  };

  var tracking_sample = function() {
    return  "ReportGrid.track(\n"+
            " '/callcenter/217',\n"+
            " {\n"+
            "   customer_support : {\n"+
            "     type: '"+ rdist(['call', 'email']) +"',\n"+
            "     duration: "+ Math.floor(Math.random() * 180) +",\n"+
            "     representative: '"+ rdist(['Alice Brewer', 'Charles Davis', 'Ed Frink', 'George Harrison']) +"',\n"+
            "     resolution: {\n"+
            "       type: '"+ rdist(['resolved', 'escalated', 'deferred'])+"',\n"+
            "       to:   '"+ rdist(['Candice Deming', 'Emily Fair', 'Gene Hunter']) +"'\n"+
            "     },\n"+
            "     '#timestamp': (new Date()).getTime(),\n"+
            "   }\n"+
            " }\n"+
            ");";
  };

  var pie_sample = "ReportGrid.pieChart('#output', {\n"+
  "  path: '/callcenter/217',\n"+
  "  event: 'customer_support',\n"+
  "  property: '.type',\n"+
  "});";

  var line_sample = "ReportGrid.lineChart('#output', {\n"+
  "  path: '/callcenter/217',\n"+
  "  event: 'customer_support',\n"+
  "  property: 'type',\n"+
  "  start: '1 day ago',\n"+
  "  end: 'now'\n"+
  "});";

  var heat_sample = "ReportGrid.heatGrid('#output', {\n"+
  "  axes : [\n"+
  "    { type : '.type' }, \n"+
  "    { type : '.representative' },\n"+
  "    { type : 'count' }\n"+
  "  ],\n"+
  "  data: {\n"+
  "    src: [{\n"+
  "      path : '/callcenter/217',\n"+
  "      event : 'customer_support',\n"+
  "      query: '.type * .representative',\n"+
  "      start: '1 day ago',\n"+
  "      end: 'now'\n"+
  "    }]\n"+
  "  },\n"+
  "  options: {\n"+
  "    color: 'interpolated-#FFFFFF,#FF7F0E'\n"+
  "  }\n"+
  "});";

  var setupTestConsole = function() {
    var draw = function() {
      eval($('#inputconsole').val());
    };

    $('#track_sample').click(function(ev) {
      $('#inputconsole').val(tracking_sample());
      draw();
    });

    $('#pie_sample').click(function(ev) {
      $('#inputconsole').val(pie_sample);
      draw();
    });

    $('#line_sample').click(function(ev) {
      $('#inputconsole').val(line_sample);
      draw();
    });

    $('#heat_sample').click(function(ev) {
      $('#inputconsole').val(heat_sample);
      draw();
    });

    $('#inputconsole').keypress(function(ev) {
      if ( ev.which == 13 && !ev.shiftKey ) {
        ev.preventDefault();
        draw();
      }
    });
  };

  setupTestConsole();
});
