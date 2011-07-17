// ReportGrid identification plugin

(function ($) {
  // Toplevel identity function.
  // Call this to retrieve the user's identity as a UUID string.

  $.identity = function () {
    var id = $.cookie('reportgrid_identity') || $.uuid();
    $.cookie('reportgrid_identity', id, {expire: 36500});       // 100 years
    return id;
  };

  $.uuid = function () {
    for (var i = 0, result = ''; i < 32; ++i)
      result += (Math.random() * 16 >>> 0).toString(16);
    return result;
  };
})(jQuery);
