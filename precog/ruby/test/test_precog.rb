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

class PrecogClientTest < Test::Unit::TestCase

    HOST = 'beta.precog.com'
    PORT = 80
    ROOT_API_KEY = '2D36035A-62F6-465E-A64A-0E37BCC5257E'

  class << self
    attr_reader :account_id, :api_key, :no_key_api, :api
    def suite
      mysuite = super
      def mysuite.run(*args)
        PrecogClientTest.startup()
        super
        PrecogClientTest.shutdown()
      end
      mysuite
    end

    def startup
      require 'precog'
      @no_key_api=Precog::Precog.new(nil, HOST, PORT)

      #create test user and extract key
      response = @no_key_api.create_account("test-rb@precog.com","password")
      @account_id=response['accountId']
      response =@no_key_api.describe_account("test-rb@precog.com","password",account_id)
      @api_key=response['apiKey']
      @api=Precog::Precog.new(@api_key, HOST, PORT)
    end

    def shutdown
    end
  end

  def assert_include(collection, value)
    assert collection.include?(value), "#{collection.inspect} does not include the value #{value.inspect}"
  end

  # ACCOUNTS 
  
  def test_create_account_existing
    response = PrecogClientTest.no_key_api.create_account("test-rb@precog.com","password")
    assert_equal Hash, response.class
    assert_include response, 'accountId'
    assert_equal PrecogClientTest.account_id, response['accountId']
  end
  
  def test_create_account_new
    email= "test-rb#{rand(1000000)}@precog.com"
    response = PrecogClientTest.no_key_api.create_account(email,"password")
    assert_equal Hash, response.class
    assert_include response, 'accountId'
  end

  def test_describe_account
    response = PrecogClientTest.no_key_api.describe_account("test-rb@precog.com","password","0000000305")
    assert_equal Hash, response.class
    assert_include response, 'accountId'
    assert_equal '0000000305', response['accountId']
    assert_include response, 'email'
    assert_equal 'test-rb@precog.com', response['email']
  end


  # def test_add_grant_to_account
  #   #   TODO once security API is complete
  #   #   PrecogClientTest.no_key_api.add_grant_to_account("test-rb@precog.com","password","0000000305", xxxxxx)
  # end

  def test_describe_plan
    response=PrecogClientTest.no_key_api.describe_plan("test-rb@precog.com","password","0000000305")
    assert_equal Hash, response.class
    assert_include response, 'type'
    assert_equal 'Free', response['type']
  end

  #Changes an account's plan (only the plan type itself may be changed). Billing for the new plan, if appropriate, will be prorated.
  def test_change_plan
    response=PrecogClientTest.no_key_api.change_plan("test-rb@precog.com","password","0000000305", "Bronze")
    
    response=PrecogClientTest.no_key_api.describe_plan("test-rb@precog.com","password","0000000305")
    assert_include response, 'type'
    assert_equal 'Bronze', response['type']

    response=PrecogClientTest.no_key_api.change_plan("test-rb@precog.com","password","0000000305", "Free")
  end

  #Changes your account access password. This call requires HTTP Basic authentication using the current password.
  def test_change_password
    response=PrecogClientTest.no_key_api.change_password("test-rb@precog.com","password","0000000305", "xyzzy")
    response=PrecogClientTest.no_key_api.change_password("test-rb@precog.com","xyzzy","0000000305", "password")
  end

  #Deletes an account's plan. This is the same as switching a plan to the free plan.
  def test_delete_plan
    response=PrecogClientTest.no_key_api.change_plan("test-rb@precog.com","password","0000000305", "Bronze")
    response=PrecogClientTest.no_key_api.delete_plan("test-rb@precog.com","password","0000000305")
    #test it's free after delete
    response=PrecogClientTest.no_key_api.describe_plan("test-rb@precog.com","password","0000000305")
    assert_equal Hash, response.class
    assert_include response, 'type'
    assert_equal 'Free', response['type']
  end

  def test_ingest_csv
    response=PrecogClientTest.api.ingest(PrecogClientTest.account_id,  "blah,\n\n", "csv")
    assert_equal 1, response['ingested']
  end

  def test_ingest_json
    json_data = "{ 'user': 'something' 'json_dta': { 'nested': 'blah'} }"
    response=PrecogClientTest.api.ingest(PrecogClientTest.account_id, json_data, "json")
    assert_equal 1, response['ingested']
  end

  def test_ingest_async
    options = {:delimiter => ",", :quote =>"'", :escape => "\\", :async => true }
    response=PrecogClientTest.api.ingest(PrecogClientTest.account_id, "blah,blah\n", "csv", options)
    #async just returns 202 result code
    assert_equal "", response
  end

  def test_store
    response=PrecogClientTest.api.store(PrecogClientTest.account_id, { :user => 'something' })
    assert_equal 1, response['ingested']
  end

  def test_query
    #just test the query was sent and executed sucessfully
    response=PrecogClientTest.api.query(PrecogClientTest.account_id, "count(//"+PrecogClientTest.account_id+")")
    assert_equal Array, response.class
    assert_equal 0, response[0]
  end

end
