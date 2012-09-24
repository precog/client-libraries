<?php

require_once('basetest.php');

class DeletePlanTest extends PrecogBaseTest {

    function setupAccount(){
        $email = json_encode(array("email"=>"fakeEmailAddress@precog.com"));
        $accountId = $this->api->createAccount($email);
        return $accountId;
    }

    function testDeletePlanCase()
    {
        $account = $this->api->setupAccount();
        $this->api->deletePlan($account);
        sleep(3);
        $result = $this->api->$describeAccount($account);
        $this->assertTrue($result["plan"] === "starter");
    }
}
?>
