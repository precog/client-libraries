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
require 'net/https'

require 'pp'
require 'time'
require 'uri'

require 'rubygems'
require 'json'

module Precog

  # API server constants
  module API
    HOST = 'devapi.precog.com'
    PORT = 443
  end

  module Paths
    FS = '/fs'
  end

  # Services constants
  module Services
    ANALYTICS = '/analytics/v1'
    ACCOUNTS = '/accounts/v1'
    INGEST = '/ingest/v1'
  end

  
  
  # Base exception for all Precog errors
  class PrecogError < StandardError
    def intialize(message)
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
      @conn       = Net::HTTP.new(host, port)
      if @port == 443
        @conn.use_ssl = true
        @conn.verify_mode = OpenSSL::SSL::VERIFY_NONE
      end
    end

    def basic_auth(user, password)
      { :auth =>{ :user=>user, :password=>password }}
    end

    def action_url(service, action )
       "#{service}/#{action}"
    end

    # Sanitize a URL path
    def sanitize_path(path)
      newpath = path.gsub(/\/+/, '/')
      while newpath.gsub!(%r{([^/]+)/\.\./?}) { |match|
        $1 == '..' ? match : ''
      } 
      end
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
      options[:auth] ||= {}

      # Add api key to path and set headers
      path = action_url(service,action)
      path = sanitize_path(path)

     
      if !@api_key.nil? && !@api_key.empty?
        path +="?apiKey=#{@api_key}"
      end

      options[:parameters].each do |k, v|
        path += "&#{k}=#{URI.escape(v)}"
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
        if options[:auth]
          request.basic_auth options[:auth][:user],options[:auth][:password]
        end
        options[:headers].each { |k,v| request.add_field(k, v) }
        response = @conn.request(request, body)
      rescue StandardError => e
        message += " failed (#{e.inspect})"
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

    def self.from_heroku(token)
      values=Utils.from_token(token)
      no_key_api=Precog.new(nil, values[:host])
      response =no_key_api.describe_account(values[:user],values[:pwd],values[:account_id])
      Precog.new(response['apiKey'], values[:host])
    end

    ######################
    ###    ACCOUNTS    ###
    ######################

    #Creates a new account ID, accessible by the specified email address and password, or returns the existing account ID.
    def create_account(email, password)
      @api.post(Services::ACCOUNTS,"accounts/",:body => { :email=> email, :password=> password } )
    end
    
    #Retrieves the details about a particular account. This call is the primary mechanism by which you can retrieve your master API key.
    def describe_account(email, password, account_id)
      @api.get(Services::ACCOUNTS,"accounts/#{account_id}",@api.basic_auth(email, password) )
    end

    #Adds a grant to an account's API key.
    def add_grant_to_account(email, password, account_id, grant_id)
      @api.post(Services::ACCOUNTS,"accounts/grants/",@api.basic_auth(email, password).merge!({ :body => { :grantId => grant_id  } }))
    end

    #Describe Plan
    def describe_plan(email, password, account_id)
      @api.get(Services::ACCOUNTS,"accounts/#{account_id}/plan",@api.basic_auth(email, password))
    end

    #Changes an account's plan (only the plan type itself may be changed). Billing for the new plan, if appropriate, will be prorated.
    def change_plan(email, password, account_id, type)
      @api.put(Services::ACCOUNTS,"accounts/#{account_id}/plan",@api.basic_auth(email, password).merge!({ :body => { :type => type } }))
    end

    #Changes your account access password. This call requires HTTP Basic authentication using the current password.
    def change_password(email, password, account_id, new_password)
      @api.put(Services::ACCOUNTS,"accounts/#{account_id}/password", @api.basic_auth(email, password).merge!({:body => { :password => new_password  } }))
    end

    #Deletes an account's plan. This is the same as switching a plan to the free plan.
    def delete_plan(email, password, account_id)
      @api.delete(Services::ACCOUNTS,"accounts/#{account_id}/plan",@api.basic_auth(email, password))
    end

    ######################
    ###     INGEST     ###
    ######################
    def get_api_key(options)
      { :apiKey => (options['apiKey']) || @api_key }
    end


    # Ingests csv or json data at the specified path
    # (mode = batch, receipt = true), 
    # (mode = batch, receipt = false), 
    # (mode = streaming)
    def ingest(path, content, type, options={})
      if !content
        raise Error.new("argument 'content' must contain a non empty value formatted as described by type")
      end

      parameters={}
      case(type.downcase) 
        when 'application/x-gzip','gz','gzip' then
          type = 'application/x-gzip'
        when 'zip' then 
          type = 'application/zip'
        when 'application/json','json' then
          type = 'application/json'
        when 'text/csv','csv' then
          type = 'text/csv'
          if options[:delimiter]
            parameters['delimiter'] = options[:delimiter]
          end
          if options[:quote]
            parameters['quote'] = options[:quote]
          end
          if options[:escape]
            parameters['escape'] = options[:escape]
          end
        else
          raise "argument 'type' must be 'json' or 'csv'"
      end
      
      parameters['mode']=options[:mode]
      if options[:mode]=="batch"
        parameters['receipt']="#{options[:receipt]}"
      end

      if options[:owner_account_id]
          parameters['owner_account_id'] = options[:owner_account_id]
      end
      action = @api.sanitize_path("/#{Paths::FS}/#{path}")
      @api.post(Services::INGEST,action, 
        { :parameters=> parameters, :body => content },type)
    end

    def ingest_batch(path,content,type, receipt, options={})
      ingest(path, content, type, options.merge!({:mode=> 'batch' , :receipt=> receipt}))
    end

    def ingest_stream(path,content,type, options={})
      ingest(path, content, type, options.merge!({:mode=> 'streaming' }))
    end

    # Store a record at the specified path
    def store(path, event, options = {})
        ingest(path, event, "json", options.merge!({:mode=> 'batch' , :receipt=> 'true'}))
    end


    def delete(path)
      action = @api.sanitize_path("#{Paths::FS}/#{path}")
      @api.delete(Services::INGEST,action)
    end

    # Send a quirrel query to be evaluated relative to the specified base path.
    def query(path, query)
      path = "#{Paths::FS}/#{path}" unless path.start_with?(Paths::FS)
      path = @api.sanitize_path(path)
      options={  :parameters => { :q => query } } 
      @api.get(Services::ANALYTICS, path, options)
    end

    private :ingest

  end

  class Utils
    def self.to_token(user,pwd,host, account_id, api_key, root_path)
      Base64.encode64("#{user}:#{pwd}:#{host}:#{account_id}:#{api_key}:#{root_path}").gsub(/\n/, '')
    end

    def self.from_token(token)
      values=Base64.decode64(token).split(":")
      { :user=>values[0], :pwd=>values[1], :host=>values[2], :account_id=>values[3], :api_key=> values[4], :root_path=> values[5] }
    end
  end
end
