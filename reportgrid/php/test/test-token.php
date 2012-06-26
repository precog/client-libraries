<?php

require_once('basetest.php');

class TestToken extends BaseTest {
	var $path = "/test/php/token";
	var $token;

	function setUp()
	{
		parent::setUp();
		$this->token = $this->rg->newToken($this->path);
		// A sleep is required to allow for propagation of token state through the cluster
		sleep(1);
		echo "Created token $this->token\n"; 
	}

	function tearDown()
	{
		echo "Deleting token $this->token\n"; 
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
		// A sleep is required to allow for propagation of token state through the cluster
		sleep(3);
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

		// A sleep is required to allow for propagation of token state through the cluster
		sleep(1);
		$info = $this->rg->token($this->token);
		$this->assertFalse($info);
	}

/*
	function testLossless()
	{
		$lossless = $this->rg->newToken("/", null, null, null, null, null, null, null, null, null, true); 
		$info = $this->rg->token($lossless);
		$this->assertTrue($info['limits']['lossless']);
		$this->rg->deleteToken($lossless);

		$notlossless = $this->rg->newToken("/", null, null, null, null, null, null, null, null, null, false); 
		$info = $this->rg->token($notlossless);
		$this->assertFalse($info['limits']['lossless']);
		$this->rg->deleteToken($notlossless);
	}
*/
}
?>
<!-- -*-  mode:php; tab-width:8; indent-tabs-mode:nil;  -*- -->
