<?php

require_once('simpletest/autorun.php');

class AllTests extends TestSuite {
    function AllTests() {
        $this->TestSuite('All tests');
        $this->addFile('test-create-account.php');
        $this->addFile('test-add-grant-to-account.php');
        $this->addFile('test-describe-account.php');
        $this->addFile('test-describe-plan.php');
        $this->addFile('test-change-password.php');
        $this->addFile('test-change-plan.php');
        $this->addFile('test-delete-plan.php');

        $this->addFile('test-create-key.php');
    	$this->addFile('test-describe-key.php');
        $this->addFile('test-list-keys.php');
        $this->addFile('test-delete-key.php');
        $this->addFile('test-retrieve-grants.php');
        $this->addFile('test-delete-grant.php');
        $this->addFile('test-list-grant-children.php');
        $this->addFile('test-create-grant-child.php');
        $this->addFile('test-add-grant-to-key.php');
        $this->addFile('test-create-grant.php');
        $this->addFile('test-describe-grant.php');

        $this->addFile('test-store.php');
        $this->addFile('test-ingest.php');
        
        $this->addFile('test-delete-path.php');

        $this->addFile('test-retrieve-metadata.php');

        $this->addFile('test-query.php');
        $this->addFile('test-limit.php');
        $this->addFile('test-skip.php');
        $this->addFile('test-sort.php');
    }
}