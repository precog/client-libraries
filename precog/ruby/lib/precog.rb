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

require 'logger'
require 'net/http'
require 'pp'
require 'time'
require 'uri'

require 'rubygems'
require 'json'
require 'base64'

module Precog

  # API server constants
  module API
    HOST = 'api.precog.io'
    PORT = 80
    VERSION = '1'
  end

  module Paths
    #TOKENS = '/auth/tokens'
    FS = '/fs'
  end

  # Services constants
  module Services
    ANALYTICS = '/analytics'
    ACCOUNTS = '/accounts'
    INGEST = '/ingest'
  end

  
  
  # Base exception for all Precog errors
  class PrecogError < StandardError
    def intialize(message)
      #log.error(message)
      super
    end
  end

  # Raised on HTTP response errors
  class HttpResponseError < PrecogError
    attr_reader :code
    def initialize(message, code)
      super(message)
      @code = code
    end
  end

  # Simple HTTP client for Precog API
  class HttpClient

    # Initialize an HTTP connection
    def initialize(api_key, host, port)
      @api_key    = api_key
      @host       = host
      @port       = port
      @version    = API::VERSION #version
      @conn       = Net::HTTP.new(host, port)
    end

    def basic_auth(user, password)
      { "Authorization" => "Basic " + Base64.encode64(user + ':' + password).chomp }
    end

    def action_url(service, action )
      return "#{service}/v#{@version}/#{action}";
    end

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
    

    # Send an HTTP request
    def method_missing(name, service, action, options={}, content_type = 'application/json')
      options[:body]    ||= ''
      options[:headers] ||= {}
      options[:parameters] ||= {}

      # Add api key to path and set headers
      path = action_url(service,action)
      path = sanitize_path(path)

     
      if (!@api_key.nil? && !@api_key.empty?)
        path +="?apiKey=#{@api_key}"
      end

      options[:parameters].each do |k, v|
        path += "&#{k}=#{v}"
      end

      options[:headers].merge!({'Content-Type' => content_type})

      # Set up message
      message = "#{name.to_s.upcase} to #{@host}:#{@port}#{path} with headers (#{options[:headers].to_json })"
      if options[:body]
        body = (content_type=='application/json')? options[:body].to_json : options[:body]
        message += " and body (#{body})"
      end

      # Send request and get response
      begin
        request = Net::HTTP.const_get(name.to_s.capitalize).new(path)
        options[:headers].each { |k,v| request.add_field(k, v) }
        response = @conn.request(request, body)
      rescue StandardError => e
        message += " failed (#{e})"
        raise HttpResponseError.new(message, 500)
      end

      # Check HTTP status code
      if ![200,202].include?(response.code.to_i)
        message += " returned non-200 status (#{response.code}): #{response.inspect}"
        raise HttpResponseError.new(message, response.code.to_i)
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

      response_data
    end

  end

  # Precog base class
  class Precog

    # Initialize an API client
    def initialize(api_key, host = API::HOST, port = API::PORT)
      @api_key = api_key
      @api = HttpClient.new(api_key, host, port)
    end

    ######################
    ###    ACCOUNTS    ###
    ######################

    #Creates a new account ID, accessible by the specified email address and password, or returns the existing account ID.
    def create_account(email, password)
      @api.post(Services::ACCOUNTS,"accounts/",:body => { :email=> email, :password=> password } )
    end
    
    #Retrieves the details about a particular account. This call is the primary mechanism by which you can retrieve your master API key.
    def describe_account(email, password, accountId)
      @api.get(Services::ACCOUNTS,"accounts/#{accountId}",:headers =>  @api.basic_auth(email, password) )
    end

    #Adds a grant to an account's API key.
    def add_grant_to_account(email, password, accountId, grantId)
      @api.post(Services::ACCOUNTS,"accounts/grants/",{ :headers =>  @api.basic_auth(email, password),:body => { :grantId => grantId  } })
    end

    #Describe Plan
    def describe_plan(email, password, accountId)
      @api.get(Services::ACCOUNTS,"accounts/#{accountId}/plan",:headers =>  @api.basic_auth(email, password))
    end

    #Changes an account's plan (only the plan type itself may be changed). Billing for the new plan, if appropriate, will be prorated.
    def change_plan(email, password, accountId, type)
      @api.put(Services::ACCOUNTS,"accounts/#{accountId}/plan",{ :headers =>  @api.basic_auth(email, password), :body => { :type => type } })
    end

    #Changes your account access password. This call requires HTTP Basic authentication using the current password.
    def change_password(email, password, accountId, newPassword)
      @api.put(Services::ACCOUNTS,"accounts/#{accountId}/password",{ :headers =>  @api.basic_auth(email, password), :body => { :password => newPassword  } })
    end

    #Deletes an account's plan. This is the same as switching a plan to the free plan.
    def delete_plan(email, password, accountId)
      @api.delete(Services::ACCOUNTS,"accounts/#{accountId}/plan",{ :headers =>  @api.basic_auth(email, password) })
    end

    ######################
    ###     INGEST     ###
    ######################
    def get_api_key(options)
      { :apiKey => (options['apiKey']) || @api_key }
    end


    # Ingests csv or json data at the specified path
    def ingest(path, content, type, options={})
      path = @api.sanitize_path(path);
      if(!content) 
        raise Error.new("argument 'content' must contain a non empty value formatted as described by type")
      end

      parameters={}
      case(type.downcase) 
        when 'application/x-gzip','gz','gzip':
          type = 'application/x-gzip'
        when 'zip':
          type = 'application/zip'
        when 'application/json','json':
          type = 'application/json'
        when 'text/csv','csv':
          type = 'text/csv';
          if(options[:delimiter])
            parameters['delimiter'] = options[:delimiter]
          end
          if(options[:quote])
            parameters['quote'] = options[:quote]
          end
          if(options[:escape])
            parameters['escape'] = options[:escape]
          end
        else
          raise "argument 'type' must be 'json' or 'csv'"
      end    
      if(options[:ownerAccountId])
          parameters['ownerAccountId'] = options[:ownerAccountId]
      end

      action = @api.sanitize_path("#{options[:async] ? "async" : "sync" }/#{Paths::FS}/#{path}")
      @api.post(Services::INGEST,action, 
        { :headers => parameters, :body => content },type)
    end

    # Store a record at the specified path
    def store(path, event, options = {})
        ingest(path, event.to_json, "application/json", options);
    end


    def delete(path)
      action = @api.sanitize_path("sync/#{Paths::FS}/#{path}")
      @api.delete(Services::INGEST,path);
    end

    # Send a quirrel query to be evaluated relative to the specified base path.
    def query(path, query)
      path = "#{Paths::FS}/#{path}" unless path.start_with?(Paths::FS)
      path = @api.sanitize_path(path)
      options={  :parameters => { :q => query } } 
      @api.get(Services::ANALYTICS, path, options)
    end

  end
end
