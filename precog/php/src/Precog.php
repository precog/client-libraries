<?php
/**
 * Provides access to the Precog API platform.
 *
 * Author: Alissa Pajer
 **/

define ("BASE_URL", "https://api.precog.com");
define ("DEFAULT_VERSION", 1);

class PrecogAPI {

    public $apiKey = null;
    public $baseUrl = null;
    public $version = null;
    public $isError = false;
    public $errorMessage = null;

    /*
     * Initialize a new PrecogAPI object
     *
     * @param String $apiKey
     * @param String $baseUrl
     *
     */
    public function __construct($apiKey, $baseUrl = BASE_URL, $version = DEFAULT_VERSION)
    {
        $this->apiKey  = $apiKey;
        $this->baseUrl = self::cleanPath($baseUrl);
        $this->version = $version;
    }

    // ***************************
    // ****** ACCOUNTS APIS ******
    // ***************************
    public static function listAccounts($email, $password, $baseUrl = BASE_URL, $version = DEFAULT_VERSION)
    {
        $url = self::baseActionUrl($baseUrl, $version, "accounts");
        return self::baseRestHelper($url, null, "GET", self::authHeaders($email, $password));
    }

    public static function createAccount($email, $password, $baseUrl = BASE_URL, $version = DEFAULT_VERSION)
    {
        $url = self::baseActionUrl($baseUrl, $version, "accounts");
        return self::baseRestHelper($url, array("email"=>$email, "password"=>$password), "POST");
    }

    public static function describeAccount($email, $password, $accountId, $baseUrl = BASE_URL, $version = DEFAULT_VERSION)
    {
        $url = self::baseActionUrl($baseUrl, $version, "accounts").$accountId;
        return self::baseRestHelper($url, null, "GET", self::authHeaders($email, $password));
    }

    public static function addGrantToAccount($email, $password, $accountId, $grantId, $baseUrl = BASE_URL, $version = DEFAULT_VERSION)
    {
        $url = self::baseActionUrl($baseUrl, $version, "accounts", $accountId)."grants/";
        return self::baseRestHelper($url, array("grantId"=>$grantId), "POST", self::authHeaders($email, $password));
    }

    public static function describePlan($email, $password, $accountId, $baseUrl = BASE_URL, $version = DEFAULT_VERSION)
    {
        $url = self::baseActionUrl($baseUrl, $version, "accounts", $accountId)."plan";
        return self::baseRestHelper($url, null, "GET", self::authHeaders($email, $password));
    }

    public static function changePlan($email, $password, $accountId, $plan, $baseUrl = BASE_URL, $version = DEFAULT_VERSION)
    {
        $url = self::baseActionUrl($baseUrl, $version, "accounts", $accountId)."plan";
        return self::baseRestHelper($url, array("type"=>$plan), "PUT", self::authHeaders($email, $password));
    }

    // ***************************
    // ****** INGEST APIS ********
    // ***************************

    public function ingestSync($path, $file, $ownerAccountId )
    {
       if(isset($ownerAccountId)) {
         $url = $this->actionUrl("ingest","sync/fs/").$path."?apiKey=".$this->apiKey."&ownerAccountId=".$ownerAccountId;
        $return = $this->restHelper($url, $file, "POST");
        return $return !== false;
       }
       $url = $this->actionUrl("ingest", "sync/fs").$path."?apiKey=".$this->apiKey;
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
        $path2  = $this->actionUrl("ingest", "sync/fs") . self::cleanPath($path) . "?apiKey=" . $this->apiKey;
        $return = $this->restHelper($path2, $event, "POST");
        return $return !== false;
    }

    public function asyncStore($path, $event)
    {
        $path2  = $this->actionUrl("ingest", "async/fs") . self::cleanPath($path) . "?apiKey=" . $this->apiKey;
        $return = $this->restHelper($path2, $event, "POST");
        return $return !== false;
    }

    public function delete($path)
    {
        $path2  = $this->actionUrl("ingest", "sync/fs") . self::cleanPath($path) . "?apiKey=" . $this->apiKey;
        $return = $this->restHelper($path2, null, "DELETE");
        return $return !== false;
    }

    // ***************************
    // ****** METADATA APIS ******
    // ***************************
    public function retrieveMetadata($path, $type = "")
    {
        $url = $this->actionUrl("meta", "fs") . self::cleanPath($path) . "?apiKey=".$this->apiKey."#".$type;
        $return = $this->restHelper($url, null, "GET");
        return $return;
    }

    /*
     * Returns an array of sub-paths
     * @params String - path
     *
     * @return Array - an array of values
     */
    public function listChildren($path)
    {
        $path = self::cleanPath($path);
        $path2  = $this->actionUrl("meta","fs")."$path?apiKey=" . $this->apiKey."#children";
        $return = $this->restHelper($path2, null, "GET");
        return $return ? $return['children'] : $return;
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
            "apiKey=" . $this->apiKey,
            "q=" . urlencode($quirrel)
        );
        if(isset($options["limit"])) {
            $params[] = "limit=" . $options["limit"];
        }

        if(isset($options["skip"])) {
            $params[] = "skip=" . $options["skip"];
        }
        if(isset($options["sortOn"])) {
            $params[] = "sortOn=" . urlencode(json_encode($options["sortOn"]));
            if(isset($options["sortOrder"])) {
                $params[] = "sortOrder=" . $options["sortOrder"];
            }
        }

        $path2  = $this->actionUrl("analytics", "fs")."?" .implode("&", $params);
        $return = $this->restHelper($path2, null, "GET");
        return $return;
    }

    // ***************************
    // ****** SECURITY APIS *****
    // ***************************
    public function listKeys()
    {
       $url = $this->actionUrl("security","apikeys" )."?apiKey=".$this->apiKey;
        $return = $this->restHelper($url, null, "GET");
        return $return;
    }

    public function createKey($grants)
    {
       $url = $this->actionUrl("security","apikeys")."?apiKey=".$this->apiKey;
        $return = $this->restHelper($url, $grants, "POST");
        return $return;
    }

    public function describeKey($apiKey)
    {
       $url = $this->actionUrl("security","apikeys").$apiKey."?apiKey=".$this->apiKey;
        $return = $this->restHelper($url, null, "GET");
        return $return;
    }

    public function deleteKey($apiKey)
    {
       $url = $this->actionUrl("security","apikeys").$apiKey."?apiKey=".$this->apiKey;
        $return = $this->restHelper($url, null, "DELETE");
        return $return !== false;
    }

    public function retrieveGrants($apiKey)
    {
       $url = $this->actionUrl("security","apikeys").$apiKey."/grants/?apiKey=".$this->apiKey;
        $return = $this->restHelper($url, null, "GET");
        return $return;
    }

    public function addGrantToKey($apiKey, $grant)
    {
       $url = $this->actionUrl("security","apikeys").$apiKey."/grants/?apiKey=".$this->apiKey;
        $return = $this->restHelper($url, $grant, "GET");
        return $return !== false;
    }

    public function removeGrant($apiKey, $grantId)
    {
       $url = $this->actionUrl("security","apikeys").$apiKey."/grants/".$grantId."?apiKey=".$this->apiKey;
        $return = $this->restHelper($url, null, "DELETE");
        return $return !== false;
    }

    public function createNewGrant($type)
    {
       $url = $this->actionUrl("security","grants")."?apiKey=".$this->apiKey;
        $return = $this->restHelper($url, $type, "POST");
        return $return;
    }

    public function describeGrant($grantId)
    {
       $url = $this->actionUrl("security", "grants").$grantId."?apiKey=".$this->apiKey;
        $return = $this->restHelper($url, null, "GET");
        return $return;
    }

    public function deleteGrant($grantId)
    {
       $url = $this->actionUrl("security","grants").$grantId."?apiKey=".$this->apiKey;
        $return = $this->restHelper($url, null, "DELETE");
        return $return !== false;
    }

    public function listChildrenGrant($grantId)
    {
       $url = $this->actionUrl("security","grants").$grantId."/children/?apiKey=".$this->apiKey;
        $return = $this->restHelper($url, null, "GET");
        return $return;
    }

    public function createChildGrant($grantId, $type)
    {
       $url = $this->actionUrl("security","grants").$grantId."/children/?apiKey=".$this->apiKey;
        $return = $this->restHelper($url, $type, "POST");
        return $return;
    }

    /*********************************
     **** PRIVATE helper function ****
     *********************************/
    private function restHelper($resturl, $params = null, $verb = 'GET', $headers = false) {
        $result = self::baseRestHelper($resturl, $params, $verb, $headers);
        if($result['ok']) {
            $this->isError = false;
            $this->errorMessage = null;
            return $result['data'];
        } else {
            $this->isError = true;
            $this->errorMessage = $result['error'];
            return false;
        }
    }

    private static function baseRestHelper($resturl, $params = null, $verb = 'GET', $headers = false) {
//echo("$verb $resturl\n");
//if($params) var_dump($params);
        $return = array('ok' => true);
        $http_params = array(
            'http' => array(
                'method'        => $verb,
                'ignore_errors' => false,
                'header'        => $headerString = self::getHeaderString($headers)
        ));
//var_dump($headerString);

        // workaround for php bug where http headers don't get sent in php 5.2
        if(version_compare(PHP_VERSION, '5.2.14') < 0) {
            ini_set('user_agent', 'PHP-SOAP/' . PHP_VERSION . "\r\n" . $headerString);
        }

        if ($params !== null && (($verb == 'POST') || ($verb == 'PUT'))) {
            $http_params['http']['content'] = json_encode($params);
        }

        $stream_context = stream_context_create($http_params);
        $file_pointer = @fopen($resturl, 'rb', false, $stream_context);
        if (!$file_pointer) {
            $stream_contents = false;
        } else {
            $stream_meta_data = stream_get_meta_data($file_pointer);
            $stream_contents = stream_get_contents($file_pointer);
            fclose($file_pointer);
        }
//if(isset($stream_contents)) var_dump($stream_contents);
//if(isset($stream_meta_data)) var_dump($stream_meta_data);
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
                    $return['data'] = $result;
                }
            /*
             * In the case of posting data (recordEvent) the API will return a 0
             * length response, in this scenario we're looking for the http 200
             * header code to indicate the data was successfully received.
             */
            } else {
                if (stripos($stream_meta_data['wrapper_data'][0], "200") !== false) {
                    $return['data'] = true;
                } else {
                    $return['ok'] = false;
                }
            }

        } else {
            /*
             * If there's an error message in the response
             * headers...send that back to the user
             */
            if (isset($http_response_header[0])) {
                $return['ok'] = false;
                $return['error'] = $http_response_header[0];
            } else {
                throw new Exception("$verb $resturl failed");
            }

        }
        return $return;
    }

    private function actionUrl($service, $action = false) {
       return self::baseActionUrl($this->baseUrl, $this->version, $service, $action);
    }

    private static function getHeaderString($headers) {
        if(!$headers) {
            $headers = array();
        }
        if(!isset($headers['Content-Type']))
            $headers['Content-Type'] = "application/json";
        $result = array();
        foreach ($headers as $key => $value) {
            $result[] = "$key: $value";
        }
        return implode("\r\n", $result);
    }

    private static function baseAuth($user, $password) {
        $tok = "$user:$password";
        return "Basic ".base64_encode($tok);
    }

    private static function cleanPath($path) {
        return trim($path, '/');
    }

    private static function baseActionUrl($baseUrl, $version, $service, $action = false) {
        return $baseUrl."/".$service."/v".$version."/".($action ? $action."/" : "");
    }

    private static function authHeaders($user, $password) {
        return array("Authorization" => self::baseAuth($user, $password));
    }
}
?>
