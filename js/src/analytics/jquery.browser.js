// ReportGrid browser version normalization

(function ($) {
  // Returns a string such as 'IE6', 'IE7', 'FF2', 'Webkit534', etc.
  $.normalized_browser = function () {
    var name = $.browser.msie    ? 'IE' :
               $.browser.mozilla ? 'FF' :
               $.browser.opera   ? 'Opera' : 'Webkit';
    var version = parseInt($.browser.version);
    return name + version;
  };
})(jQuery);
