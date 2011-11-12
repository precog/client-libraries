<?php

require_once('basetest.php');

class TestQuery extends BaseTest {
	public static $path = "/test/php/query";

	function testSeries()
	{
		$path = TestQuery::$path;
		$series = $this->rg->series($path, 'impression');
		$this->assertIsA($series, "Array");
		$this->assertTrue(count($series) > 0);
	}

	function testSearchCount()
	{
		$path = TestQuery::$path;

		$chrome = $this->rg->search($path, array('.impression.browser' => 'Chrome'));
		$os = $this->rg->search($path, array('.impression.browser' => 'Chrome', '.impression.os' => 'OSX'));
		$this->assertTrue($chrome > 0);
		$this->assertTrue($os > 0);
		$this->assertTrue($chrome > $os);
	}

	function testSearchSeries()
	{
		$path = TestQuery::$path;
		$series = $this->rg->search($path, array('.impression.browser' => 'Chrome'), 'day');
		$this->assertIsA($series, "Array");
		$this->assertTrue(count($series) > 0);
	}

	function testIntersect()
	{
		$path = TestQuery::$path;
		$series = $this->rg->intersect($path, array(array(property => '.impression.browser', limit => 3, order => 'descending')));
		$this->assertIsA($series, "Array");
		$this->assertTrue(count($series) > 0);
	}

	function testHisto()
	{
		$path = TestQuery::$path;
		$hist = $this->rg->histogram($path, 'impression', 'browser');
		$this->assertIsA($hist, "Array");
	}

	function testValues()
	{
		$valids = array('Chrome', 'Firefox', 'IE', 'Safari');
		$path = TestQuery::$path;
		$values = $this->rg->values($path, 'impression', 'browser');
		foreach($valids as $valid)
			$this->assertTrue(in_array($valid, $values));

		$top = $this->rg->values($path, 'impression', 'browser', 2);
		$this->assertEqual(2, count($top));

		foreach($top as $value)
			$this->assertTrue($this->remove($value, $values));
		
		$bottom = $this->rg->values($path, 'impression', 'browser', 1, false);
		$this->assertEqual(1, count($bottom));

		foreach($bottom as $value)
			$this->assertTrue($this->remove($value, $values));
	}

	function testCount()
	{
		$path = TestQuery::$path;
		$count = $this->rg->count($path, '.impression');

		$this->assertIsA($count, "Int");

		$this->assertTrue(0 < $count);
		$this->assertTrue($count == $this->rg->count($path, 'impression'));

		$this->assertTrue(0 < ($pcount = $this->rg->count($path, 'impression', 'browser')) && $pcount <= $count);

		$this->assertTrue(0 < ($vcount = $this->rg->count($path, 'impression', 'browser', 'Chrome')) && $vcount < $pcount);
	}

	function testChildren()
	{
		$path = TestQuery::$path;
		$child = basename($path);
		$parent = dirname($path);

		$children = $this->rg->children($path); // all

		$this->assertIsA($children, 'Array');
		$this->assertTrue(in_array('.impression', $children));

		$children = $this->rg->children($path, 'path');
		$this->assertFalse(in_array('.impression', $children));

		$children = $this->rg->children($path, 'property');
		$this->assertTrue(in_array('.impression', $children));

		$children = $this->rg->children($path, 'property', '.impression');
		$this->assertTrue(in_array('.browser', $children));
		$this->assertTrue(in_array('.os', $children));

		$children = $this->rg->children($parent); // all

		$this->assertTrue(in_array($child, $children));
		$this->assertFalse(in_array('.impression', $children));
	}

	private function remove($needle, &$arr)
	{
		for($i = 0; $i < count($arr); $i++)
		{
			if($arr[$i] == $needle)
			{
				array_splice($arr, $i, 1);
				return true;
			}
		}
		return false;
	}
}

if(isset($_GET['builddata']) || (isset($argc) && in_array('--builddata', $argv)))
{
	$browsers = array('Chrome', 'Chrome', 'Chrome', 'Chrome', 'IE', 'IE', 'IE', 'Safari', 'Safari', 'Firefox');
	$oses = array('Win', 'Win', 'Win', 'OSX', 'OSX', 'iOS');
	$path = TestQuery::$path;
	$rg = BaseTest::createApi();

	for($i = 0; $i < 50; $i++)
	{
		$browser = ($browser = next($browsers)) ? $browser : reset($browsers);
		$os = ($os = next($oses)) ? $os : reset($oses);
		
		if(!$rg->track($path, array(
			'impression' => array(
				'browser' => $browser,
				'os' => $os
			)
		))) {
			var_dump($rg->errorMessage);
		}
	}
	exit;
}