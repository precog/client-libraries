// ReportGrid path plugin

(function ($) {
  // Invoke this to get a structured path from the current document location.
  // This removes the query string, www, and protocol to return a normalized
  // URL.

  $.path = function (href) {
    return [].join.call(/^(?:https?:\/\/)?(?:www\.)?([^\/]+)\/([^?#]+)(?:\?[^#]*)?(#.*)?$/i.exec(href || document.location.href), '/');
  };
})(jQuery);
