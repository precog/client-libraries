import reportgrid


ROOT_TOKEN_ID = 'A3BC1539-E8A9-4207-BB41-3036EC2C6E6D'
TEST_TOKEN_ID = None


def setup():
    api = reportgrid.ReportGrid(ROOT_TOKEN_ID)
    response = api.new_token(path='/python_test')
    assert len(response) == len(ROOT_TOKEN_ID)

    global TEST_TOKEN_ID
    TEST_TOKEN_ID = response

def test_token():
    api = reportgrid.ReportGrid(ROOT_TOKEN_ID)
    response = api.token(TEST_TOKEN_ID)
    assert type(response) is dict
    assert 'tokenId' in response
    assert response['tokenId'] == TEST_TOKEN_ID

def test_tokens():
    api = reportgrid.ReportGrid(ROOT_TOKEN_ID)
    response = api.tokens()
    assert type(response) is list
    assert TEST_TOKEN_ID in response

def test_track():
    api = reportgrid.ReportGrid(TEST_TOKEN_ID)
    api.track(
        path='/',
        name='test',
        properties={'test': 123},
        rollup=True)

def test_children():
    api = reportgrid.ReportGrid(TEST_TOKEN_ID)
    response = api.children(path='/')
    assert type(response) is list
    #assert len(response) > 0

def test_children_with_type_path():
    api = reportgrid.ReportGrid(TEST_TOKEN_ID)
    response = api.children(path='/', type='path')
    assert type(response) is list
    assert response == filter(lambda x: x.endswith('/'), response)

def test_children_with_type_property():
    api = reportgrid.ReportGrid(TEST_TOKEN_ID)
    response = api.children(path='/', type='property')
    assert type(response) is list
    assert response == filter(lambda x: x.startswith('.'), response)

def test_children_with_property():
    api = reportgrid.ReportGrid(TEST_TOKEN_ID)
    response = api.children(path='/', property='test')
    assert type(response) is list
    #assert len(response) > 0

def test_property_count():
    api = reportgrid.ReportGrid(TEST_TOKEN_ID)
    response = api.property_count(path='/', property='test')
    assert type(response) is int
    #assert response > 0

def test_property_series():
    api = reportgrid.ReportGrid(TEST_TOKEN_ID)
    response = api.property_series(path='/', property='test')
    assert type(response) is list
    #assert reportgrid.Periodicity.Eternity in response
    #assert type(response[reportgrid.Periodicity.Eternity]) is list
    #assert len(response[reportgrid.Periodicity.Eternity]) > 0

def test_property_values():
    api = reportgrid.ReportGrid(TEST_TOKEN_ID)
    response = api.property_values(path='/', property='test.test')
    assert type(response) is list
    #assert 123 in response

def test_property_value_count():
    api = reportgrid.ReportGrid(TEST_TOKEN_ID)
    response = api.property_value_count(path='/', property='test.test', value=123)
    assert type(response) is int
    #assert response > 0

def test_property_value_series():
    api = reportgrid.ReportGrid(TEST_TOKEN_ID)
    response = api.property_value_series(path='/', property='test.test', value=123)
    assert type(response) is list
    #assert reportgrid.Periodicity.Eternity in response
    #assert type(response[reportgrid.Periodicity.Eternity]) is list
    #assert len(response[reportgrid.Periodicity.Eternity]) > 0

def test_search_count():
    api = reportgrid.ReportGrid(TEST_TOKEN_ID)
    response = api.search_count(path='/', where=[{"variable":"test.test", "value":123}])
    assert type(response) is int
    #assert response > 0

def test_search_series():
    api = reportgrid.ReportGrid(TEST_TOKEN_ID)
    response = api.search_series(path='/', where=[{"variable":"test.test", "value":123}])
    assert type(response) is list
    #assert reportgrid.Periodicity.Eternity in response
    #assert type(response[reportgrid.Periodicity.Eternity]) is list
    #assert len(response[reportgrid.Periodicity.Eternity]) > 0

def teardown():
    api = reportgrid.ReportGrid(ROOT_TOKEN_ID)
    api.delete_token(token_id=TEST_TOKEN_ID)
    assert TEST_TOKEN_ID not in api.tokens()
