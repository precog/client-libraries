import reportgrid
import time


ROOT_TOKEN_ID = 'A3BC1539-E8A9-4207-BB41-3036EC2C6E6D'
HOST = 'localhost'
PORT = 30020
PATH_PREFIX = ''

def setup_module(module):
    module.TestReportGrid.root_api = reportgrid.ReportGrid(ROOT_TOKEN_ID, HOST, PORT, PATH_PREFIX)
    response = module.TestReportGrid.root_api.new_token(path='/python_test')
    assert len(response) == len(ROOT_TOKEN_ID)

    module.TestReportGrid.test_token_id = response
    module.TestReportGrid.test_api = reportgrid.ReportGrid(module.TestReportGrid.test_token_id, HOST, PORT, PATH_PREFIX)

    module.TestReportGrid.test_api.track(
        path='/',
        name='pytest',
        properties={'pyprop': 123},
        rollup=True)

    module.TestReportGrid.test_api.track(
        path='/py-client',
        name='pytest',
        properties={'pyprop': 456},
        rollup=True)

    time.sleep(20)

def teardown_module(module):
    module.TestReportGrid.root_api.delete_token(token_id=module.TestReportGrid.test_token_id)
    assert module.TestReportGrid.test_token_id not in module.TestReportGrid.root_api.tokens()

class TestReportGrid: 
    def test_token(self):
        response = self.root_api.token(self.test_token_id)
        assert type(response) is dict
        assert 'tokenId' in response
        assert response['tokenId'] == self.test_token_id

    def test_tokens(self):
        response = self.root_api.tokens()
        assert type(response) is list
        assert self.test_token_id in response

    def test_children(self):
        response = self.test_api.children(path='/')
        assert type(response) is list
        assert '.pytest' in response

    def test_children_with_type_path(self):
        response = self.test_api.children(path='/', type='path')
        assert type(response) is list
        assert u'py-client' in response

    def test_children_with_type_property(self):
        response = self.test_api.children(path='/', type='property')
        assert type(response) is list
        assert '.pytest' in response

    def test_children_with_property(self):
        response = self.test_api.children(path='/', property='pytest')
        assert type(response) is list
        assert '.pyprop' in response

    def test_property_count(self):
        response = self.test_api.property_count(path='/', property='pytest')
        assert type(response) is int
        assert response > 0

    def test_property_series(self):
        response = self.test_api.property_series(path='/', property='pytest')
        assert type(response) is list
        #assert reportgrid.Periodicity.Eternity in response
        #assert type(response[reportgrid.Periodicity.Eternity]) is list
        #assert len(response[reportgrid.Periodicity.Eternity]) > 0

    def test_property_values(self):
        response = self.test_api.property_values(path='/', property='pytest.pyprop')
        assert type(response) is list
        assert 123 in response

    def test_property_value_count(self):
        response = self.test_api.property_value_count(path='/', property='pytest.pyprop', value=123)
        assert type(response) is int
        assert response > 0

    def test_property_value_series(self):
        response = self.test_api.property_value_series(path='/', property='pytest.pyprop', value=123)
        assert type(response) is list
        #assert reportgrid.Periodicity.Eternity in response
        #assert type(response[reportgrid.Periodicity.Eternity]) is list
        #assert len(response[reportgrid.Periodicity.Eternity]) > 0

    def test_search_count(self):
        response = self.test_api.search_count(path='/', where=[{"variable":".pytest.pyprop", "value":123}])
        assert type(response) is int
        assert response > 0

    def test_search_series(self):
        response = self.test_api.search_series(path='/', where=[{"variable":".pytest.pyprop", "value":123}])
        assert type(response) is list
        #assert reportgrid.Periodicity.Eternity in response
        #assert type(response[reportgrid.Periodicity.Eternity]) is list
        #assert len(response[reportgrid.Periodicity.Eternity]) > 0

