<?php
/**
 * Provides access to the Precog API platform.
 *
 * Author: Alissa Pajer
 **/

define ("BASE_URL", "http://api.precog.com");

class PrecogAPI {
 
    private $_apiKey = null;
    private $_baseUrl = null;
    private $_version = null;
    public $isError = false;
    public $errorMessage = null;

    /*
     * Initialize a new PrecogAPI object
     *
     * @param String $token_id
     * @param String $baseurl
     *
     */
    public function __construct($token_id, $baseurl = BASE_URL, $version = 1) 
    {
        $this->_apiKey = $token_id;
        $this->_baseUrl = $this->cleanPath($baseurl);
        $this->_version = $version;
    }

    // ***************************
    // ****** ACCOUNTS APIS ******
    // ***************************
    public function listAccounts($id) 
    {
        $url = $this->servicePath("accounts").$id; 
        $return = $this->restHelper($url, null, "GET");
        return $return !== false;
    }

    public function createAccount($email) 
    {
        $url = $this->servicePath("accounts"); 
        $return = $this->restHelper($url, array("email"=>$email), "POST");
        return $return !== false;
    }

    public function describeAccount($id) 
    {
        $url = $this->servicePath("accounts").$id."/"; 
        $return = $this->restHelper($url, null, "GET");
        return $return !== false;
    }

    public function addGrantToAccount($id, $grantId) 
    {
        $url = $this->servicePath("accounts").$id."/"."grants/"; 
        $return = $this->restHelper($url, array("grantId"=>$grandId), "POST");
        return $return !== false;
    }

     public function changePlan($id, $plan) 
    {
        $url = $this->servicePath("accounts").$id."/"."plan/"; 
        $return = $this->restHelper($url, array("type"=>$plan), "PUT");
        return $return !== false;
    }

     public function deletePlan($id) 
    {
        $url = $this->servicePath("accounts").$id."/"."plan/"; 
        $return = $this->restHelper($url, null, "DELETE");
        return $return !== false;
    }

      public function deleteAccount($id) 
    {
        $url = $this->servicePath("accounts").$id."/"; 
        $return = $this->restHelper($url, null, "DELETE");
        return $return !== false;
    }

    // ***************************
    // ****** INGEST APIS ********
    // ***************************

     public function ingestAsync($path, $apiKey, $file, $ownerAccountId ) 
    {
       if(isset($ownerAccountId)){
         $url = $this->servicePath("ingest")."async/fs/".$path."?apiKey=".$apiKey."&ownerAccountId=".$ownerAccountId; 
        $return = $this->restHelper($url, $file, "POST");
        return $return !== false;
       }
       $url = $this->servicePath("ingest")."async/fs/".$path."?apiKey=".$apiKey; 
        $return = $this->restHelper($url, $file, "POST");
        return $return !== false;
       
    }

       public function ingestSync($path, $apiKey, $file, $ownerAccountId ) 
    {
       if(isset($ownerAccountId)){
         $url = $this->servicePath("ingest")."sync/fs/".$path."?apiKey=".$apiKey."&ownerAccountId=".$ownerAccountId; 
        $return = $this->restHelper($url, $file, "POST");
        return $return !== false;
       }
       $url = $this->servicePath("ingest")."sync/fs/".$path."?apiKey=".$apiKey; 
        $return = $this->restHelper($url, $file, "POST");
        return $return !== false;
       
    }

        /*
     * Record a new event
     *
     * @param String $path The path in which to store this event
     * @param Array $events event data
     *
     * @return Bool - success/failure
     */

    public function store($path, $event) 
    {
        $path2  = $this->servicePath("fs") . $this->cleanPath($path) . "?apiKey=" . $this->_apiKey;
        $return = $this->restHelper($path2, $event, "POST");
        return $return !== false;
    }

    // ***************************
    // ****** METADATA APIS ******
    // ***************************
     public function retrieveMetadata($path, $type = "") 
    {
       $url = $this->servicePath("meta")."fs/".$path."#".$type; 
        $return = $this->restHelper($url, $file, "GET");
        return $return !== false;  
    }



    // ***************************
    // ****** ANALYTICS APIS *****
    // ***************************

    /*
     * Returns the value of the query
     * @params String - raw Quirrel
     *
     * @return Array - an array of values
     */
  
    public function query($quirrel, $options = array())
    {
        $params = array(
            "apiKey=" . $this->_apiKey, 
            "q=" . urlencode($quirrel)
        );
        if(isset($options["limit"])){
            $params[] = "limit=" . $options["limit"];
        }

        if(isset($options["skip"])){
            $params[] = "skip=" . $options["skip"];
        }
        if(isset($options["sortOn"])){
            $params[] = "sortOn=" . urlencode(json_encode($options["sortOn"]));
            if(isset($options["sortOrder"])){
                $params[] = "sortOrder=" . $options["sortOrder"];
            }
        }

        $path2  = $this->servicePath("fs")."?" .implode("&", $params);
        $return = $this->restHelper($path2, null, "GET");
        return $return;
    }

  

    // ***************************
    // ****** SECURITY APIS *****
    // ***************************
    public function listKeys($apiKey) 
    {
       $url = $this->servicePath("security")."apikeys/?apiKey=".$apiKey; 
        $return = $this->restHelper($url, null, "GET");
        return $return !== false;  
    }

    public function createKey($apiKey, $grants) 
    {
       $url = $this->servicePath("security")."apikeys/?apiKey=".$apiKey; 
        $return = $this->restHelper($url, $grants, "POST");
        return $return !== false;  
    }

    public function describeKey($apiKey, $authorizingKey) 
    {
       $url = $this->servicePath("security")."apikeys/".$apiKey."?apiKey=".$authorizingKey; 
        $return = $this->restHelper($url, null, "GET");
        return $return !== false;  
    }

    public function deleteKey($apiKey, $authorizingKey) 
    {
       $url = $this->servicePath("security")."apikeys/".$apiKey."?apiKey=".$authorizingKey; 
        $return = $this->restHelper($url, null, "DELETE");
        return $return !== false;  
    }

    public function retrieveGrants($apiKey, $authorizingKey) 
    {
       $url = $this->servicePath("security")."apikeys/".$apiKey."/grants/?apiKey=".$authorizingKey; 
        $return = $this->restHelper($url, null, "GET");
        return $return !== false;  
    }

    public function addGrantToKey($apiKey, $authorizingKey, $grant) 
    {
       $url = $this->servicePath("security")."apikeys/".$apiKey."/grants/?apiKey=".$authorizingKey; 
        $return = $this->restHelper($url, $grant, "GET");
        return $return !== false;  
    }

    public function removeGrant($apiKey, $authorizingKey, $grantId) 
    {
       $url = $this->servicePath("security")."apikeys/".$apiKey."/grants/".$grantId."?apiKey=".$authorizingKey; 
        $return = $this->restHelper($url, null, "DELETE");
        return $return !== false;  
    }

    public function createNewGrant($apiKey, $type) 
    {
       $url = $this->servicePath("security")."grants/?apiKey=".$apiKey; 
        $return = $this->restHelper($url, $type, "POST");
        return $return !== false;  
    }

    public function describeGrant($apiKey, $grantId) 
    {
       $url = $this->servicePath("security")."grants/".$grantId."?apiKey=".$apiKey; 
        $return = $this->restHelper($url, null, "GET");
        return $return !== false;  
    }

    public function deleteGrant($apiKey, $grantId) 
    {
       $url = $this->servicePath("security")."grants/".$grantId."?apiKey=".$apiKey; 
        $return = $this->restHelper($url, null, "DELETE");
        return $return !== false;  
    }

    public function listChildrenGrant($apiKey, $grantId) 
    {
       $url = $this->servicePath("security")."grants/".$grantId."/children/?apiKey=".$apiKey; 
        $return = $this->restHelper($url, null, "GET");
        return $return !== false;  
    }

      public function createChildGrant($apiKey, $grantId, $type) 
    {
       $url = $this->servicePath("security")."grants/".$grantId."/children/?apiKey=".$apiKey; 
        $return = $this->restHelper($url, $type, "POST");
        return $return !== false;  
    }


    /*
     * Returns an array of sub-paths
     * @params String - path
     *
     * @return Array - an array of values
     */
    public function listChildren($path)
    {
        $path = $this->cleanPath($path);
        $path2  = $this->servicePath("fs")."$path?apiKey=" . $this->_apiKey;
        $return = $this->restHelper($path2, null, "GET");
        return $return;
    }

       public function delete($path)
    {
        $path2  = $this->servicePath("fs") . $this->cleanPath($path) . "?apiKey=" . $this->_apiKey;
        var_dump($path2);
        $return = $this->restHelper($path2, null, "DELETE");
        return $return !== false;
    }

    /*********************************
     **** PRIVATE helper function ****
     *********************************/
    private function restHelper($json_endpoint, $params = null, $verb = 'GET') {
        $return = null;

        $http_params = array(
            'http' => array(
                'method' => $verb,
                'ignore_errors' => false
        ));
        if ($params !== null) {
            if ( ($verb == 'POST') || ($verb == 'PUT') ) {


                $header = "Content-Type: application/json";
                $http_params['http']['content'] = json_encode($params);
                $http_params['http']['header'] = $header;
                // workaround for php bug where http headers don't get sent in php 5.2
                if(version_compare(PHP_VERSION, '5.2.14') < 0){
                    ini_set('user_agent', 'PHP-SOAP/' . PHP_VERSION . "\r\n" . $header);
                }
            }//end if
        }//end if ($params !== null)

        $stream_context = stream_context_create($http_params);
        $file_pointer = @fopen($json_endpoint, 'rb', false, $stream_context);
        if (!$file_pointer) {
            $stream_contents = false;
        } else {
            $stream_meta_data = stream_get_meta_data($file_pointer);
            $stream_contents = stream_get_contents($file_pointer);
        }
        if ($stream_contents !== false) {

            /*
             * In the case of we're receiving stream data back from the API,
             * json decode it here.
             */
            if (strlen($stream_contents) > 0) {

                $result = json_decode($stream_contents, true);

                if ($result === null) {
                    error_log("Exception:  " . $stream_contents);
                } else {
                    $return = $result;
                }
            /*
             * In the case of posting data (recordEvent) the API will return a 0
             * length response, in this scenario we're looking for the http 200
             * header code to indicate the data was successfully received.
             */
            } else {

                if (stripos($stream_meta_data['wrapper_data'][0], "200") !== false) {
                    $return = true;
                } else {
                    $return = false;
                }//end inner else
            }//end middle else

        } else {

            /*
             * If there's an error message in the response
             * headers...send that back to the user
             */
            if (isset($http_response_header[0])) {

                $this->isError = true;
                $this->errorMessage = $http_response_header[0];
                $return = false;

            } else {
                throw new Exception("$verb $json_endpoint failed");
            }

        }//end outer else
        return $return;
    }//end restHelper

    private function cleanPath($path)
    {
        return trim($path, '/');
    }
    private function servicePath($service){
        return $this->_baseUrl."/".$service."/v".$this->_version."/";
    }
}
?>
