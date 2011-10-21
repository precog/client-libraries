# Copyright (C) 2011 by ReportGrid, Inc. All rights reserved.
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# No portion of this Software shall be used in any application which does not
# use the ReportGrid platform to provide some subset of its functionality.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
#

import httplib
import json
import logging
import posixpath
import time


__app_name__     = 'reportgrid'
__version__      = '2011.06.15'
__author__       = 'Michael T. Conigliaro'
__author_email__ = 'mike [at] reportgrid [dot] com'
__description__  = 'Python client library for ReportGrid (http://www.reportgrid.com)'
__url__          = 'https://github.com/reportgrid/client-libraries/'


class API:
    """API server constants"""

    Host = 'api.reportgrid.com'
    Port = 80

class Path:
    """Path constants"""

    class Analytics:
        Root   = '/services/analytics/v1'
        Tokens = '/tokens'
        VFS    = '/vfs'
        Search = '/search'

class Time:
    """Time constants"""

    Zero     = 0
    Eternity = 2147483647

class Periodicity:
    """Periodicity constants"""

    Minute   =  'minute'
    Hour     =  'hour'
    Day      =  'day'
    Week     =  'week'
    Year     =  'year'
    Eternity =  'eternity'

class ReportGridError(Exception):
    """Base exception for all ReportGrid errors"""

    def __init__(self, message, log_level='error'):
        getattr(logging.getLogger(__name__), log_level)(message)
        super(ReportGridError, self).__init__(message)

class HttpResponseError(ReportGridError):
    """Raised on HTTP response errors"""

class HttpClient(object):
    """Simple HTTP client for ReportGrid API"""

    def __init__(self, token_id, host, port, path_prefix):
        """Initialize an HTTP connection"""

        self.log         = logging.getLogger(__name__)

        self.token_id    = token_id
        self.host        = host
        self.port        = port
        self.path_prefix = path_prefix

        self.conn        = httplib.HTTPConnection("%s:%d" % (host, int(port)))

    def __getattr__(self, name):
        """Send an HTTP request"""

        name = name.upper()

        def do(self, path='', body='', headers={}):

            # Add token id to path and set headers
            path = "%s%s?tokenId=%s" % (self.path_prefix, path, self.token_id)
            headers.update({'Content-Type': 'application/json'})

            # Set up message
            message = "%s to %s:%s%s with headers (%s)" % \
                      (name, self.host, self.port, path, headers)
            if body:
                body = json.dumps(body)
                message += " and body (%s)" % body

            # Send request and get response
            try:
                self.conn.request(name, path, body, headers)
                response = self.conn.getresponse()
                response_data = response.read()
            except StandardError, e:
                message += " failed (%s)" % (e)
                raise HttpResponseError(message)

            # Check HTTP status code
            if response.status != 200:
                message += " returned non-200 status (%d): %s" % \
                           (response.status, response_data)
                raise HttpResponseError(message)

            # Try parsing JSON response
            if len(response_data) > 0:
                try:
                    response_data = json.loads(response_data)
                except ValueError, e:
                    message += " returned invalid JSON (%s): %s" % \
                               (e, response_data)
                    raise HttpResponseError(message)

            message += " returned: %s" % response_data
            self.log.info(message)

            return response_data

        return do.__get__(self)

class ReportGrid(object):
    """ReportGrid base class"""

    def __init__(self, token_id=''):
        """Initialize an API client"""

        self.analytics = HttpClient(token_id=token_id,
                                    host=API.Host, port=API.Port,
                                    path_prefix=Path.Analytics.Root)

    def new_token(self, path):
        """Create a new token"""

        return self.analytics.post('%s/' % Path.Analytics.Tokens, {'path':path})

    def token(self, token_id):
        """Return information about a token"""

        return self.analytics.get('%s/%s' % (Path.Analytics.Tokens, token_id))

    def tokens(self):
        """Return all child tokens"""

        return self.analytics.get('%s/' % Path.Analytics.Tokens)

    def delete_token(self, token_id):
        """Delete a token"""

        return self.analytics.delete('%s/%s' % (Path.Analytics.Tokens, token_id))

    def track(self, path, name, properties, rollup=False,
              timestamp=time.time(), count=1):
        """Track an event"""

        # Sanitize path
        if not path.startswith(Path.Analytics.VFS):
            path = '%s/%s' % (Path.Analytics.VFS, path)
        path = self.__sanitize_path(path)

        # Add the timestamp to the properties dictionary
        properties['#timestamp'] = timestamp
        
        # Track event
        self.analytics.post(path, { name: properties })

        # Roll up to parents if necessary
        parent_path = self.__sanitize_path('%s/../' % path)
        if rollup and parent_path.startswith(Path.Analytics.VFS) and \
           parent_path != Path.Analytics.VFS:
            self.track(parent_path, name, properties, rollup, timestamp, count)

    def children(self, path, property='', type='all'):
        """Return children of the specified path"""

        property = self.__sanitize_property(property)
        path = '%s/%s/%s' % (Path.Analytics.VFS, path, property)

        children = self.analytics.get(self.__sanitize_path(path))
        if type == 'path':
            children = filter(lambda x: x.endswith('/'), children)
        elif type == 'property':
            children = filter(lambda x: x.startswith('.'), children)
        elif property:
            children = filter(lambda x: x == property, children)

        return children

    def property_count(self, path, property):
        """Return count of the specified property"""

        property = self.__sanitize_property(property)
        path = '%s/%s/%s/count' % (Path.Analytics.VFS, path, property)
        return self.analytics.get(self.__sanitize_path(path))

    def property_series(self, path, property, periodicity=Periodicity.Eternity):
        """Return time series counts for the specified property"""

        property = self.__sanitize_property(property)
        path = '%s/%s/%s/series/%s' % (Path.Analytics.VFS, path, property, periodicity)
        return self.analytics.get(self.__sanitize_path(path))

    def property_values(self, path, property):
        """Return all values of the specified property"""

        property = self.__sanitize_property(property)
        path = '%s/%s/%s/values/' % (Path.Analytics.VFS, path, property)
        return self.analytics.get(self.__sanitize_path(path))

    def property_value_count(self, path, property, value):
        """Return count of the specified value for the specified property"""

        property = self.__sanitize_property(property)
        path = '%s/%s/%s/values/%s/count' % (Path.Analytics.VFS, path, property, value)
        return self.analytics.get(self.__sanitize_path(path))

    def property_value_series(self, path, property, value, periodicity=Periodicity.Eternity):
        """Return time series counts for the specified value for the specified property"""

        property = self.__sanitize_property(property)
        path = '%s/%s/%s/values/%s/series/%s' % (Path.Analytics.VFS, path, property,
                                                 value, periodicity)
        return self.analytics.get(self.__sanitize_path(path))

    def search_count(self, path, where={}):
        """Return a count by searching across a range of conditions"""

        return self.analytics.post(Path.Analytics.Search, body={
            'select': 'count',
            'from'  : self.__sanitize_path(path),
            'where' : where
        })

    def search_series(self, path, periodicity=Periodicity.Eternity, where={},
                      start=Time.Zero, end=Time.Eternity):
        """Return time series counts by searching across a range of conditions"""

        return self.analytics.post(Path.Analytics.Search, body={
            'select': 'series/%s' % periodicity,
            'from'  : self.__sanitize_path(path),
            'where' : where,
            'start' : start,
            'end'   : end
        })

    def __sanitize_path(self, path):
        """Sanitize a URL path"""

        normpath = posixpath.normpath(path)
        if path.endswith('/') and not normpath.endswith('/'):
            normpath += '/'
        return normpath

    def __sanitize_property(self, property):
        """Properties must always be prefixed with a period"""

        if property and not property.startswith('.'):
            property = '.%s' % property
        return property
