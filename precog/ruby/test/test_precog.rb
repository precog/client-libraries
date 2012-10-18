# Copyright (C) 2011 by Precog, Inc. All rights reserved.
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
    VERSION = '/v1'
    ROOT_API_KEY = '2D36035A-62F6-465E-A64A-0E37BCC5257E'

  class << self

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
    end

    def shutdown
    end
  end

  def assert_include(collection, value)
    assert collection.include?(value), "#{collection.inspect} does not include the value #{value.inspect}"
  end

  # ACCOUNTS 
  def account_api
   return Precog::Precog.new(nil, HOST, PORT, "/accounts/v1")
  end

  def test_create_account
    response = account_api.create_account("test-rb@precog.com","password")
    assert_equal Hash, response.class
    assert_include response, 'accountId'
    assert_equal '0000000305', response['accountId']
  end

  def test_describe_account
    response = account_api.describe_account("test-rb@precog.com","password","0000000305")
    assert_equal Hash, response.class
    assert_include response, 'accountId'
    assert_equal '0000000305', response['accountId']
    assert_include response, 'email'
    assert_equal 'test-rb@precog.com', response['email']
  end


  def test_add_grant_to_account
    #   TODO once security API is complete
    #   account_api.add_grant_to_account("test-rb@precog.com","password","0000000305", xxxxxx)
  end

  def test_describe_plan
    response=account_api.describe_plan("test-rb@precog.com","password","0000000305")
    assert_equal Hash, response.class
    assert_include response, 'type'
    assert_equal 'Free', response['type']
  end

  #Changes an account's plan (only the plan type itself may be changed). Billing for the new plan, if appropriate, will be prorated.
  def test_change_plan
    response=account_api.change_plan("test-rb@precog.com","password","0000000305", "Bronze")
    
    response=account_api.describe_plan("test-rb@precog.com","password","0000000305")
    assert_include response, 'type'
    assert_equal 'Bronze', response['type']

    response=account_api.change_plan("test-rb@precog.com","password","0000000305", "Free")
  end

  #Changes your account access password. This call requires HTTP Basic authentication using the current password.
  def test_change_password
    response=account_api.change_password("test-rb@precog.com","password","0000000305", "xyzzy")
    response=account_api.change_password("test-rb@precog.com","xyzzy","0000000305", "password")
  end

  #Deletes an account's plan. This is the same as switching a plan to the free plan.
  def test_delete_plan
    response=account_api.change_plan("test-rb@precog.com","password","0000000305", "Bronze")
    response=account_api.delete_plan("test-rb@precog.com","password","0000000305")
    #test it's free after delete
    response=account_api.describe_plan("test-rb@precog.com","password","0000000305")
    assert_equal Hash, response.class
    assert_include response, 'type'
    assert_equal 'Free', response['type']
  end
end
