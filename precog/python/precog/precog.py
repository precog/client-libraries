# Copyright (C) 2012 by Precog, Inc. All rights reserved.
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
# use the Precog platform to provide some subset of its functionality.
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
import datetime
import base64


__app_name__     = 'precog'
__version__      = '2012.10.23'
__author__       = 'Gabriel Claramunt'
__author_email__ = 'gabriel [at] precog [dot] com'
__description__  = 'Python client library for Precog (http://www.precog.com)'
__url__          = 'https://github.com/reportgrid/client-libraries/precog/python'


class API:
    """API server constants"""

    Host = 'api.precog.io'
    Port = 443
    Version = 1

class Path:
    """Path constants"""

    FS= "/fs"

    class Services:
        Analytics   = '/analytics'
        Ingest      = '/ingest'
        Accounts = '/accounts'

class PrecogError(Exception):
    """Base exception for all Precog errors"""

    def __init__(self, message, log_level='error'):
        getattr(logging.getLogger(__name__), log_level)(message)
        super(PrecogError, self).__init__(message)

class HttpResponseError(PrecogError):
    """Raised on HTTP response errors"""

class HttpClient(object):
    """Simple HTTP client for Precog API"""

    def __init__(self, api_key, host, port):
        """Initialize an HTTP connection"""

        self.log         = logging.getLogger(__name__)

        self.api_key     = api_key
        self.host        = host
        self.port        = port
        self.version     = 1

    def __getattr__(self, name):
        """Send an HTTP request"""

        # New connection per request, to avoid issues with servers closing connections
        # We ran into this while testing against the dev cluster. Tests would fail with 
        # a long stack trace and the error CannotSendRequest
        if (self.port == 443 ):
            self.conn = httplib.HTTPSConnection("%s:%d" % (self.host, int(self.port)))
        else :
            self.conn = httplib.HTTPConnection("%s:%d" % (self.host, int(self.port)))

        name = name.upper()

        def do(self, service='', action='', body='', parameters = {}, headers = {}, content_type='application/json'):

            path= self.sanitize_path("%s/v%s/%s" % (service,API.Version,action))
            # Add token id to path and set headers
            path = "%s?apiKey=%s" % (path, self.api_key)
            for key, value in parameters.items():
                path = "%s&%s=%s" % (path, key, value)

            if headers == None:
                headers={'Content-Type': content_type}
            else:
                headers.update({'Content-Type': content_type})

            # Set up message
            message = "%s to %s:%s%s with headers (%s)" % \
                      (name, self.host, self.port, path, headers)
            if body:
                if content_type=='application/json':
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
            if response.status != 200 and response.status != 202:
                message += " returned non-200 status (%d): %s [%s]" % \
                           (response.status, response.reason, response_data)
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

    def sanitize_path(self, path):
        """Sanitize a URL path"""

        normpath = posixpath.normpath(path)
        if path.endswith('/') and not normpath.endswith('/'):
            normpath += '/'
        return normpath

    def basic_auth(self,user, password):
      return { "Authorization": "Basic %s" % (base64.standard_b64encode("%s:%s" % (user,password))) }

class Precog(object):
    """Precog base class"""

    def __init__(self, api_key='', host = API.Host, port = API.Port):
        """Initialize an API client"""

        self.api = HttpClient(api_key = api_key, host = host, port = port )

#service='', action='', body='', parameters = {}, headers = {}

    def create_account(self, email, password):
        """Creates a new account ID, accessible by the specified email address and password, or returns the existing account ID."""
        return self.api.post(Path.Services.Accounts, "accounts/", body={ "email": email, "password": password } )

    def describe_account(self,email, password, accountId):
        """Retrieves the details about a particular account. This call is the primary mechanism by which you can retrieve your master API key."""
        return self.api.get(Path.Services.Accounts,"accounts/%s" % (accountId), headers=self.api.basic_auth(email, password) )

    def property_count(self, path, property, start = None, end = None):
        """Return count of the specified property"""

        parameters = {}
        if start and end:
            parameters = self.__time_parameters(start, end)

        property = self.__sanitize_property(property)
        path = '%s/%s/%s/count' % (Path.Analytics.VFS, path, property)
        return self.api.get(self.__sanitize_path(path), parameters = parameters)
    
    def ingest(self, path, content, type, options={}):
        """Ingests csv or json data at the specified path"""
        if not content:
            raise Error.new("argument 'content' must contain a non empty value formatted as described by type")

        parameters={}

        content_type=type.lower()
        if (content_type == 'application/x-gzip' or content_type =='gz' or content_type =='gzip'):
            type = 'application/x-gzip'
        elif content_type == 'zip':
            type = 'application/zip'
        elif content_type ==  'application/json' or content_type =='json':
            type = 'application/json'
        elif content_type ==  'text/csv' or content_type =='csv':
            type = 'text/csv';
            if 'delimiter' in options:
                parameters['delimiter'] = options['delimiter']
            if 'quote' in options:
                parameters['quote'] = options['quote']
            if 'escape' in options:
                parameters['escape'] = options['escape']
        else:
            raise "argument 'type' must be 'json' or 'csv'"    
        if 'ownerAccountId' in options:
            parameters['ownerAccountId'] = options['ownerAccountId']

    	if 'async' in options and options['async']:
    	    sync='async'
    	else:
    	    sync='sync'
        action = self.api.sanitize_path("%s/%s/%s" % (sync,Path.FS,path))
        return self.api.post(Path.Services.Ingest,action, content, parameters, {}, type)

    def store(self,path, event, options = {}):
        """Store a record at the specified path"""
        return self.ingest(path, json.dumps(event), "application/json", options);

    def delete(self,path):
        return self.api.delete(Path.Services.Ingest,"sync/%s/%s" % (Path.FS,path));

    def query(self,path, query):
        """Send a quirrel query to be evaluated relative to the specified base path."""
        if not path.startswith(Path.FS):
            path = "%s/%s" % (Path.FS,path)
        return self.api.get(Path.Services.Analytics, path, parameters= { "q": query })


    def sanitize_property(self, property):
        """Properties must always be prefixed with a period"""

        if property and not property.startswith('.'):
            property = '.%s' % property
        return property
