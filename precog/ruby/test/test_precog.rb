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
  class << self
    attr_reader :root_token_id, :test_token_id, :test_host, :test_port, :test_path, :test_root
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
      @root_token_id = '2D36035A-62F6-465E-A64A-0E37BCC5257E'

      @test_host = 'beta2012v1.precog.io'
      @test_port = 80
      @test_path = '/v1'
      @test_root = '/unit_test/beta/ruby_test'

      api = build_root_client
      token = Precog::Token.readwrite(@test_root)
      response = api.new_token(token)

      puts(response['uid'])
      raise "Did not obtain a new test token." unless response['uid'].length == @root_token_id.length

      @test_token_id = response['uid']

      api = build_test_client
      api.store("#{@test_root}/", {'test' => 123})
      api.store("#{@test_root}/rg-client/subdir/subsub", {'test' => 456})
      sleep(20)
    end

    def shutdown
      api = Precog::Precog.new(@root_token_id, @test_host, @test_port, @test_path)
      response = api.delete_token(@test_token_id)
      begin
        api.token
      rescue Precog::HttpResponseError => e
        raise e unless e.code == 404 
      end
    end

    def build_root_client
      Precog::Precog.new(@root_token_id, @test_host, @test_port, @test_path)  
    end

    def build_test_client
      Precog::Precog.new(@test_token_id, @test_host, @test_port, @test_path)  
    end
  end

  def assert_include(collection, value)
    assert collection.include?(value), "#{collection.inspect} does not include the value #{value.inspect}"
  end

  def test_token
    api = PrecogClientTest.build_test_client
    response = api.token
    assert_equal response.class, Hash
    assert_include response, 'uid'
    assert_equal response['uid'], PrecogClientTest.test_token_id
  end

  def test_query
    api = PrecogClientTest.build_test_client
    response = api.query('/', "/#{PrecogClientTest.test_root}")
    assert_equal response.class, Array
    assert_equal response[0].class, Hash
    assert_include response[0], 'test'
    assert_equal response[0]['test'], 123
  end

  # def test_children
  #   api = PrecogClientTest.build_test_client
  #   response = api.children('/')
  #   assert_equal response.class, Array
  #   assert_include response, '.test'
  # end

  # def test_deep_children
  #   api = PrecogClientTest.build_test_client
  #   response = api.children('/rg-client/subdir', :type => :path)
  #   assert_equal response.class, Array
  #   assert_include response, 'subsub'
  # end
end
