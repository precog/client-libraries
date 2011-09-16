require 'test/unit'


class TestExecute < Test::Unit::TestCase

  def setup
    require 'reportgrid'

    @root_token_id = 'A3BC1539-E8A9-4207-BB41-3036EC2C6E6D'
    @test_token_id = nil

    api = ReportGrid::ReportGrid.new(@root_token_id)
    response = api.new_token('/ruby_test')
    assert response.length == @root_token_id.length

    @test_token_id = response
  end

  def test_token
    api = ReportGrid::ReportGrid.new(@root_token_id)
    response = api.token(@test_token_id)
    assert response.class == Hash
    assert response.include?('tokenId')
    assert response['tokenId'] == @test_token_id
  end

  def test_tokens
    api = ReportGrid::ReportGrid.new(@root_token_id)
    response = api.tokens
    assert response.class == Array
    assert response.include?(@test_token_id)
  end

  def test_track
    api = ReportGrid::ReportGrid.new(@test_token_id)
    api.track('/', 'test', {'test'=>123}, :rollup=>true)
  end

  def test_children
    api = ReportGrid::ReportGrid.new(@test_token_id)
    response = api.children('/')
    assert response.class == Array
    #assert response.length > 0
  end

  def test_children_with_type_path
    api = ReportGrid::ReportGrid.new(@test_token_id)
    response = api.children('/', :type=>'path')
    assert response.class == Array
    assert response == response.select { |obj| obj.end_with?('/') }
  end

  def test_children_with_type_property
    api = ReportGrid::ReportGrid.new(@test_token_id)
    response = api.children('/', :type=>'property')
    assert response.class == Array
    assert response == response.select { |obj| obj.start_with?('.') }
  end

  def test_children_with_property
    api = ReportGrid::ReportGrid.new(@test_token_id)
    response = api.children('/', :property=>'test')
    assert response.class == Array
    #assert response.length > 0
  end

  def test_property_count
    api = ReportGrid::ReportGrid.new(@test_token_id)
    response = api.property_count('/', 'test')
    #assert response.class == Fixnum
    assert response.class == String
  end

  def test_property_series
    api = ReportGrid::ReportGrid.new(@test_token_id)
    response = api.property_series('/', 'test')
    assert response.class == Array
    #assert response.include?(ReportGrid::Periodicity::ETERNITY)
    #assert response[ReportGrid::Periodicity::ETERNITY].class == Array
    #assert response[ReportGrid::Periodicity::ETERNITY].length > 0
  end

  def test_property_values
    api = ReportGrid::ReportGrid.new(@test_token_id)
    response = api.property_values('/', 'test.test')
    assert response.class == Array
    #assert response.include?(123)
  end

  def test_property_value_count
    api = ReportGrid::ReportGrid.new(@test_token_id)
    response = api.property_value_count('/', 'test.test', 123)
    #assert response.class == Fixnum
    assert response.class == String
    #assert response > 0
  end

  def test_property_value_series
    api = ReportGrid::ReportGrid.new(@test_token_id)
    response = api.property_value_series('/', 'test.test', 123)
    assert response.class == Array
    #assert response.include?(ReportGrid::Periodicity::ETERNITY)
    #assert response[ReportGrid::Periodicity::ETERNITY].class == Array
    #assert response[ReportGrid::Periodicity::ETERNITY].length > 0
  end

  def test_search_count
    api = ReportGrid::ReportGrid.new(@test_token_id)
    response = api.search_count('/', :where=>[{:variable => 'test.test', :value => 123}])
    #assert response.class == Fixnum
    assert response.class == String
    #assert response > 0
  end

  def test_search_series
    api = ReportGrid::ReportGrid.new(@test_token_id)
    response = api.search_series('/', :where=>[{:variable => 'test.test', :value => 123}])
    assert response.class == Array
    #assert response.include?(ReportGrid::Periodicity::ETERNITY)
    #assert response[ReportGrid::Periodicity::ETERNITY].class == Array
    #assert response[ReportGrid::Periodicity::ETERNITY].length > 0
  end

  def teardown
    api = ReportGrid::ReportGrid.new(@root_token_id)
    api.delete_token(@test_token_id)
    assert !api.tokens.include?(@test_token_id)
  end

end
