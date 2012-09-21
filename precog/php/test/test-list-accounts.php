<?php

require_once('basetest.php');

class TestListAccounts extends PrecogBaseTest {

        $accountId = createAccount(fakeEmailAddress@precog.com);
        return $accountId;
    
    function testList()
    {
        $list = listAccounts($accountId);
        $this->assertTrue($accountId === $list);
    }
}
?>
