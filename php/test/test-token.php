<?php

require_once('basetest.php');

class TestToken extends BaseTest {
	var $path = "/test/php/token";
	var $token;

	function setUp()
	{
		parent::setUp();
		$this->token = $this->rg->newToken($this->path);
	}

	function tearDown()
	{
		$this->rg->deleteToken($this->token);
	}

	function testCreateToken()
	{
		$this->assertNotNull($this->token);
		$this->assertIsA($this->token, "string");
	}

	function testDeleteToken()
	{
		$this->assertIsA($this->rg->token($this->token),"Array");
		$this->assertTrue($this->rg->deleteToken($this->token));
		$this->assertNotNull($this->rg->token($this->token));
		$this->assertFalse($this->rg->deleteToken($this->token));
	}

	function testChildren()
	{
		$list = $this->rg->tokens();
		$this->assertIsA($list, "array");
		$this->assertTrue(($count = count($list)) > 0);
	}

	function testTokenInfo()
	{
		$info = $this->rg->token($this->token);
		$this->assertIsA($info, "Array");

		$this->assertTrue($this->rg->deleteToken($this->token));

		$info = $this->rg->token($this->token);
		$this->assertFalse($info);
	}
}