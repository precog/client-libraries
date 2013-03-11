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

require 'test/unit'
require 'base64'

class PrecogClientTest < Test::Unit::TestCase

    HOST = 'beta.precog.com'
    PORT = 443
    ROOT_API_KEY = '2D36035A-62F6-465E-A64A-0E37BCC5257E'

    attr_reader :account_id, :api_key, :no_key_api, :api


  def setup
      require 'precog'
      #connection without api key
      @no_key_api=Precog::Precog.new(nil, HOST, PORT)
      #create test user and extract key
      response = @no_key_api.create_account("test-rb@precog.com","password")
      @account_id=response['accountId']
      response =@no_key_api.describe_account("test-rb@precog.com","password",account_id)
      @api_key=response['apiKey']
      #connection with api key
      @api=Precog::Precog.new(@api_key, HOST, PORT)
    end

    def teardown
      #nothing :)
    end

  def assert_include(collection, value)
    assert collection.include?(value), "#{collection.inspect} does not include the value #{value.inspect}"
  end

  # ACCOUNTS 
  
  def test_create_account_existing
    response = @no_key_api.create_account("test-rb@precog.com","password")
    assert_equal Hash, response.class
    assert_include response, 'accountId'
    assert_equal @account_id, response['accountId']
  end
  
  def test_create_account_new
    email= "test-rb#{rand(1000000)}@precog.com"
    response = @no_key_api.create_account(email,"password")
    assert_equal Hash, response.class
    assert_include response, 'accountId'
  end

  def test_describe_account
    response = @no_key_api.describe_account("test-rb@precog.com","password",@account_id)
    assert_equal Hash, response.class
    assert_include response, 'accountId'
    assert_equal @account_id, response['accountId']
    assert_include response, 'email'
    assert_equal 'test-rb@precog.com', response['email']
  end


  # def test_add_grant_to_account
  #   #   TODO once security API is complete
  #   #   @no_key_api.add_grant_to_account("test-rb@precog.com","password",@account_id, xxxxxx)
  # end

  def test_describe_plan
    response=@no_key_api.describe_plan("test-rb@precog.com","password",@account_id)
    assert_equal Hash, response.class
    assert_include response, 'type'
    assert_equal 'Free', response['type']
  end

  #Changes an account's plan (only the plan type itself may be changed). Billing for the new plan, if appropriate, will be prorated.
  def test_change_plan
    response=@no_key_api.change_plan("test-rb@precog.com","password",@account_id, "Bronze")
    
    response=@no_key_api.describe_plan("test-rb@precog.com","password",@account_id)
    assert_include response, 'type'
    assert_equal 'Bronze', response['type']

    response=@no_key_api.change_plan("test-rb@precog.com","password",@account_id, "Free")
  end

  #Changes your account access password. This call requires HTTP Basic authentication using the current password.
  def test_change_password
    response=@no_key_api.change_password("test-rb@precog.com","password",@account_id, "xyzzy")
    response=@no_key_api.change_password("test-rb@precog.com","xyzzy",@account_id, "password")
  end

  #Deletes an account's plan. This is the same as switching a plan to the free plan.
  def test_delete_plan
    response=@no_key_api.change_plan("test-rb@precog.com","password",@account_id, "Bronze")
    response=@no_key_api.delete_plan("test-rb@precog.com","password",@account_id)
    #test it's free after delete
    response=@no_key_api.describe_plan("test-rb@precog.com","password",@account_id)
    assert_equal Hash, response.class
    assert_include response, 'type'
    assert_equal 'Free', response['type']
  end

  def test_ingest_csv
    response=@api.ingest(@account_id,  "blah,\n\n", "csv")
    assert_equal 1, response['ingested']
  end

  def test_ingest_json
    json_data = "{ 'user': 'something' 'json_dta': { 'nested': 'blah'} }"
    response=@api.ingest(@account_id, json_data, "json")
    assert_equal 1, response['ingested']
  end

  def test_ingest_async
    options = {:delimiter => ",", :quote =>"'", :escape => "\\", :async => true }
    response=@api.ingest(@account_id, "blah,blah\n", "csv", options)
    #async just returns 202 result code
    assert_equal "", response
  end

  def test_store
    response=@api.store(@account_id, { :user => 'something' })
    assert_equal 1, response['ingested']
  end

  def test_query
    #just test the query was sent and executed sucessfully
    response=@api.query(@account_id, "count(//"+@account_id+")")
    assert_equal Array, response.class
    assert_equal 0, response[0]
  end

  def test_from_heroku
      #connection with Heroku token
      token=Precog::Utils.to_token("test-rb@precog.com","password","#{HOST}","#{@account_id}")
      assert_equal Precog::Utils.from_token(token) { :user=>"test-rb@precog.com", :pwd=>"password", :host=>"#{HOST}", :account_id=>"#{@account_id}" }
      heroku_api=Precog::Precog.from_heroku(token)
      response =heroku_api.describe_account("test-rb@precog.com","password",@account_id)
      assert_equal @api_key, response['apiKey']
  end

end
