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

require 'test/unit'

class ReportGridClientTest < Test::Unit::TestCase
  class << self
    attr_reader :root_token_id, :test_token_id, :test_host, :test_port, :test_path
    def suite
      mysuite = super
      def mysuite.run(*args)
        ReportGridClientTest.startup()
        super
        ReportGridClientTest.shutdown()
      end
      mysuite
    end

    def startup
      require 'reportgrid'
      @root_token_id = 'A3BC1539-E8A9-4207-BB41-3036EC2C6E6D'

      @test_host = 'devapi.reportgrid.com'
      @test_port = 80
      @test_path = ReportGrid::Path::Analytics::ROOT

      api = build_root_client
      response = api.new_token('/ruby_test')
      raise "Did not obtain a new test token." unless response.length == @root_token_id.length

      @test_token_id = response

      api = build_test_client
      api.track('/', 'test', {'test' => 123}, :rollup => true)
      api.track('/rg-client/subdir/subsub', 'test', {'test' => 456}, :rollup => true)
      sleep(20)
    end

    def shutdown
      api = ReportGrid::ReportGrid.new(@root_token_id, @test_host, @test_port, @test_path)
      api.delete_token(@test_token_id)
      raise "Token failed to delete correctly." unless !api.tokens.include?(@test_token_id)
    end

    def build_root_client
      ReportGrid::ReportGrid.new(@root_token_id, @test_host, @test_port, @test_path)  
    end

    def build_test_client
      ReportGrid::ReportGrid.new(@test_token_id, @test_host, @test_port, @test_path)  
    end
  end

  def assert_include(collection, value)
    assert collection.include?(value), "#{collection.inspect} does not include the value #{value.inspect}"
  end

  def test_token
    api = ReportGridClientTest.build_root_client
    response = api.token(ReportGridClientTest.test_token_id)
    assert_equal response.class, Hash
    assert_include response, 'tokenId'
    assert_equal response['tokenId'], ReportGridClientTest.test_token_id
  end

  def test_tokens
    api = ReportGridClientTest.build_root_client
    response = api.tokens
    assert_equal response.class, Array
    assert_include response, ReportGridClientTest.test_token_id
  end

  def test_children
    api = ReportGridClientTest.build_test_client
    response = api.children('/')
    assert_equal response.class, Array
    assert_include response, '.test'
  end

  def test_children_with_type_path
    api = ReportGridClientTest.build_test_client
    response = api.children('/', :type => :path)
    assert_equal response.class, Array
    assert_include response, 'rg-client'
  end

  def test_deep_children
    api = ReportGridClientTest.build_test_client
    response = api.children('/rg-client/subdir', :type => :path)
    assert_equal response.class, Array
    assert_include response, 'subsub'
  end

  def test_children_with_type_property
    api = ReportGridClientTest.build_test_client
    response = api.children('/', :type => :property)
    assert_equal response.class, Array
    assert_include response, '.test'
  end

  def test_children_with_property
    api = ReportGridClientTest.build_test_client
    response = api.children('/', :property => 'test')
    assert_equal response.class, Array
    assert_include response, '.test'
  end

  def test_property_count
    api = ReportGridClientTest.build_test_client
    response = api.property_count('/', 'test')
    assert_equal response.class, String
    assert_operator response.to_i, :>, 0
  end

  def test_property_series
    api = ReportGridClientTest.build_test_client
    response = api.property_series('/', 'test')
    assert_equal response.class, Array
    #assert response.include?(ReportGrid::Periodicity::ETERNITY)
    #assert response[ReportGrid::Periodicity::ETERNITY].class == Array
    #assert response[ReportGrid::Periodicity::ETERNITY].length > 0
  end

  def test_property_values
    api = ReportGridClientTest.build_test_client
    response = api.property_values('/', 'test.test')
    assert_equal response.class, Array
    assert_include response, 123
  end

  def test_property_value_count
    api = ReportGridClientTest.build_test_client
    response = api.property_value_count('/', 'test.test', 123)
    assert_equal response.class, String
    assert_operator response.to_i, :>, 0
  end

  def test_property_value_series
    api = ReportGridClientTest.build_test_client
    response = api.property_value_series('/', 'test.test', 123)
    assert response.class == Array
    #assert response.include?(ReportGrid::Periodicity::ETERNITY)
    #assert response[ReportGrid::Periodicity::ETERNITY].class == Array
    #assert response[ReportGrid::Periodicity::ETERNITY].length > 0
  end

  def test_search_count
    api = ReportGridClientTest.build_test_client
    response = api.search_count('/', :where=>[{:variable => 'test.test', :value => 123}])
    assert_equal response.class, String
    assert_operator response.to_i, :>, 0
  end

  def test_search_series
    api = ReportGridClientTest.build_test_client
    response = api.search_series('/', :where=>[{:variable => 'test.test', :value => 123}])
    assert_equal response.class, Array
    #assert response.include?(ReportGrid::Periodicity::ETERNITY)
    #assert response[ReportGrid::Periodicity::ETERNITY].class == Array
    #assert response[ReportGrid::Periodicity::ETERNITY].length > 0
  end
end
