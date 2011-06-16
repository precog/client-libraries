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

require 'logger'
require 'net/http'
require 'pp'
require 'time'
require 'uri'

require 'rubygems'
require 'json'


module ReportGrid

  NAME         = 'reportgrid'
  VERSION      = '2011.06.15'
  AUTHOR       = 'Michael T. Conigliaro'
  AUTHOR_EMAIL = 'mike [at] reportgrid [dot] com'
  DESCRIPTION  = 'Ruby client library for ReportGrid (http://www.reportgrid.com)'
  URL          = 'https://github.com/reportgrid/client-libraries/'

  #log          = Logger.new(STDOUT)

  # API server constants
  module API
    HOST = 'api.reportgrid.com'
    PORT = 80
  end

  # Path constants
  module Path

    module Analytics
      ROOT   = '/services/analytics/v0'
      TOKENS = '/tokens'
      VFS    = '/vfs'
      SEARCH = '/search'
    end

  end

  # Time constants
  module Time
    ZERO     = 0
    ETERNITY = 2147483647
  end

  # Periodicity constants
  module Periodicity
    MINUTE   =  'minute'
    HOUR     =  'hour'
    DAY      =  'day'
    WEEK     =  'week'
    YEAR     =  'year'
    ETERNITY =  'eternity'
  end

  # Base exception for all ReportGrid errors
  class ReportGridError < StandardError

    def intialize(message)
      #log.error(message)
      super
    end
  end

  # Raised on HTTP response errors
  class HttpResponseError < ReportGridError; end

  # Simple HTTP client for ReportGrid API
  class HttpClient

    # Initialize an HTTP connection
    def initialize(token_id, host, port, path_prefix)
      @token_id    = token_id
      @host        = host
      @port        = port
      @path_prefix = path_prefix
      @conn        = Net::HTTP.new(host, port)
    end

    # Send an HTTP request
    def method_missing(name, path, options={})
      options[:body]    ||= ''
      options[:headers] ||= {}

      # Add token id to path and set headers
      path = "#{@path_prefix}#{path}?tokenId=#{@token_id}"
      options[:headers].merge!({'Content-Type' => 'application/json'})

      # Set up message
      message = "#{name.to_s.upcase} to #{@host}:#{@port}#{path} with headers (#{options[:headers].to_json })"
      if options[:body].length > 0
        body = options[:body].to_json
        message += " and body (#{body})"
      end

      # Send request and get response
      begin
        request = Net::HTTP.const_get(name.to_s.capitalize).new(path)
        options[:headers].each { |k,v| request.add_field(k, v) }
        response = @conn.request(request, body)
      rescue StandardError => e
        message += " failed (#{e})"
        raise HttpResponseError.new(message)
      end

      # Check HTTP status code
      if response.code.to_i != 200
        message += " returned non-200 status (#{response.code}): #{response.body}"
        raise HttpResponseError.new(message)
      end

      # Try parsing JSON response
      begin
        response_data = JSON.parse(response.body)
      rescue StandardError => e
        response_data = response.body
        if response_data.start_with?('"') && response_data.end_with?('"')
          response_data = response_data[1...-1]
        end
      end

      message += " returned: #{response_data}"
      #log.info(message)

      response_data
    end

  end

  # ReportGrid base class
  class ReportGrid

    # Initialize an API client
    def initialize(token_id='')
      @analytics = HttpClient.new(token_id, API::HOST, API::PORT, Path::Analytics::ROOT)
    end

    # Create a new token
    def new_token(path)
      @analytics.post("#{Path::Analytics::TOKENS}/", :body=>{:path => path})
    end

    # Return information about a token
    def token(token_id)
      @analytics.get("#{Path::Analytics::TOKENS}/#{token_id}")
    end

    # Return all child tokens
    def tokens
      @analytics.get("#{Path::Analytics::TOKENS}/")
    end

    # Delete a token
    def delete_token(token_id)
      @analytics.delete("#{Path::Analytics::TOKENS}/#{token_id}")
    end

    # Track an event
    def track(path, name, properties, options={})
      options[:rollup]    ||= False
      options[:timestamp] ||= Time::ETERNITY
      options[:count]     ||= 1

      # Sanitize path
      unless path.start_with?(Path::Analytics::VFS)
        path = "#{Path::Analytics::VFS}/#{path}"
      end
      path = sanitize_path(path)

      # Track event
      @analytics.post(path, :body=>{
        :events    => { :name => properties },
        :count     => options[:count],
        :timestamp => options[:timestamp]
      })

      # Roll up to parents if necessary
      parent_path = sanitize_path("#{path}/../")
      if options[:rollup] && parent_path.start_with?(Path::Analytics::VFS) &&
        parent_path != Path::Analytics::VFS
          track(parent_path, name, properties, options)
      end
    end

    # Return children of the specified path
    def children(path, options={})
      options[:property] ||= ''
      options[:type]     ||= 'all'

      options[:property] = sanitize_property(options[:property])
      path = "#{Path::Analytics::VFS}/#{path}/#{options[:property]}"

      children = @analytics.get(sanitize_path(path))
      if options[:type] == 'path'
        children.select {|obj| obj.end_with?('/')}
      elsif options[:type] == 'property'
        children.select {|obj| obj.start_with?('.')}
      elsif options[:property].length > 0
          children.reject {|obj| obj == options[:property]}
      end
    end

    # Return count of the specified property
    def property_count(path, property)
      property = sanitize_property(property)
      path = "#{Path::Analytics::VFS}/#{path}/#{property}/count"
      @analytics.get(sanitize_path(path))
    end

    # Return time series counts for the specified property
    def property_series(path, property, options={})
      options[:periodicity] ||= Periodicity::ETERNITY

      property = sanitize_property(property)
      path = "#{Path::Analytics::VFS}/#{path}/#{property}/series/#{options[:periodicity]}"
      @analytics.get(sanitize_path(path))
    end

    # Return all values of the specified property
    def property_values(path, property)
      property = sanitize_property(property)
      path = "#{Path::Analytics::VFS}/#{path}/#{property}/values/"
      @analytics.get(sanitize_path(path))
    end

    # Return count of the specified value for the specified property
    def property_value_count(path, property, value)
      property = sanitize_property(property)
      path = "#{Path::Analytics::VFS}/#{path}/#{property}/values/#{value}/count"
      @analytics.get(sanitize_path(path))
    end

    # Return time series counts for the specified value for the specified property
    def property_value_series(path, property, value, options={})
      options[:periodicity] ||= Periodicity::ETERNITY

      property = sanitize_property(property)
      path = "#{Path::Analytics::VFS}/#{path}/#{property}/values/#{value}/series/#{options[:periodicity]}"
      @analytics.get(sanitize_path(path))
    end

    # Return a count by searching across a range of conditions
    def search_count(path, where={})
      @analytics.post(Path::Analytics::SEARCH, :body=>{
        :select => 'count',
        :from   => sanitize_path(path),
        :where  => where
      })
    end

    # Return time series counts by searching across a range of conditions
    def search_series(path, options)
      options[:periodicity] ||= Periodicity::ETERNITY
      options[:where]       ||= {}
      options[:start]       ||= Time::ZERO
      options[:end]         ||= Time::ETERNITY

      @analytics.post(Path::Analytics::SEARCH, :body=>{
          :select => "series/#{options[:periodicity]}",
          :from   => sanitize_path(path),
          :where  => options[:where],
          :start  => options[:start],
          :end    => options[:end]
      })
    end

    private

    # Sanitize a URL path
    def sanitize_path(path)
      newpath = path.gsub(/\/+/, '/')
      while newpath.gsub!(%r{([^/]+)/\.\./?}) { |match|
        $1 == '..' ? match : ''
      } do end
      newpath.gsub(%r{/\./}, '/').sub(%r{/\.\z}, '/')
    end

    # Properties must always be prefixed with a period
    def sanitize_property(property)
      if property && !property.start_with?('.')
        property = ".#{property}"
      end
      property
    end

  end

end

class ReportGrid::ReportGrid
  include ReportGrid
end
