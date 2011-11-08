<?php
/**
 * Provides access to the Report Grid API platform.
 *
 **/
 
/*
 * PUT LICENSE HERE
 */

define ("API_VERSION", "v1");
define ("BASE_URL", "http://api.reportgrid.com/services/analytics/");

class ReportGridAPI {
    
    private $_tokenID = null;
    
    public $isError = false;
    public $errorMessage = null;
    
    /*
     * Initialize a new ReportGridAPI object
     *
     * @param String $token_id
     * 
     */
    public function __construct($token_id) {
        $this->_tokenID = $token_id;
    }
    
    /*
     * Create a new token
     * 
     * @param String path         The path, relative to the parent's path, that will be associated with this tokenId
     * @param String expires 	  The expiration date of the token, measured in milliseconds from the start of the Unix Epoch, UTC time
     * @param String read         Does this token have read permissions
     * @param String write        Does this token have write permissions
     * @param String share        Does this token have share permissions
     * @param String explore      Does this token have explore permissions
     * @param String order        The maximum number of sets in an intersection query
     * @param String limit        The maximum number of properties associated with an events
     * @param String depth        The maximum depth of properties associated with events
     * @param String lossless     A bool value
     *
     * @return String token
     */
    public function newToken($path = "", $expires = null, $read = null, $write = null, $share = null, $explore = null, $order = null, $limit = null, $depth = null, $tags = null, $lossless = null) {

        $return_value = null;
        
        $params = array();
        $params['path'] = $path;

        $perms = array();
        if(null !== $read)
            $perms['read'] = $read;
        if(null !== $write)
            $perms['write'] = $write;
        if(null !== $share)
            $perms['share'] = $share;
        if(null !== $explore)
            $perms['explore'] = $explore;
        if(null !== $read)
            $params['permissions'] = $perms;
        if(null !== $expires)
            $params['expires'] = $expires;

        $limits = array();
        if(null !== $order)
            $limits['order'] = $order;
        if(null !== $limit)
            $limits['limit'] = $limit;
        if(null !== $depth)
            $limits['depth'] = $depth;
        if(null !== $tags)
            $limits['tags']  = $tags;
        if(null !== $lossless)
            $limits['lossless']  = $lossless;
        $params['limits'] = $limits;
        
        $result = $this->restHelper(BASE_URL . API_VERSION . "/tokens?tokenId=" . $this->_tokenID, $params, "POST");
        
        if (isset($result[0])) {
            $return_value = $result[0];
        }
        
        return $return_value;
        
    }
    
    /*
     * Return all tokens this->_tokenId is a parent of
     *
     * @returns Array - All tokens associated with this->_tokenId
     */
    public function getTokens() {

        $return_value = null;
        
        $return_value = $this->restHelper(BASE_URL . API_VERSION . "/tokens?tokenId=" . $this->_tokenID, null, "GET");
        
        return $return_value;
    }
    
    /*
     * Return an array of data about a specific token
     *
     * @param String - Token
     *
     * @return Array - All information about this token
     */
    public function tokenInfo($token) {

        $return_value = null;
        
        $return_value = $this->restHelper(BASE_URL . API_VERSION . "/tokens/" . $token . "?tokenId=" . $this->_tokenID, null, "GET");
        
        return $return_value;
    }
    
    /*
     * Delete an existing token
     *
     * @param String - Token
     *
     * @return int - 0/1.  0=fail 1=success
     */
    public function deleteToken($token) {

        $return_value = null;
        
        $return_value = $this->restHelper(BASE_URL . API_VERSION . "/tokens/" . $token . "?tokenId=" . $this->_tokenID, null, "DELETE");
        
        return $return_value;
    }
    
    /*
     * Record a new event
     *
     * @param String - path
     * @param Array - event data
     *
     * @return int - 0/1.  0=fail 1=success
     */
    public function recordEvent($path = "", $params = array()) {

        $return_value = false;
        $return_value = $this->restHelper(BASE_URL . API_VERSION . "/vfs/" . $path . "/?tokenId=" . $this->_tokenID, $params, "POST");
        
        return $return_value;
    }
    
    /*
     * Retrieve an event
     *
     * @param String - path
     * @param String - interaction
     * @param String - type
     * @param String - periodicity
     *
     * @return Array - event occurances
     */
    public function retrieveEvent($path = "", $interaction = "", $type = "", $periodicity = "eternity") {

        $return_value = null;
        
        if ($periodicity != "count") {
            $periodicity = "series/" . $periodicity;
        }
        
        $url = BASE_URL . API_VERSION . "/vfs/" . $path . "/" . $interaction . "/values/" . $type . "/" . $periodicity . "?tokenId=" . $this->_tokenID;
        $result = $this->restHelper($url, null, "GET", "json");
        
        if ($result) {
            $return_value = $result;
        }
        
        return $return_value;
    }    
    
    /*
     * Delete an existing event
     *
     * @param String - path
     * @param Array - event parameters
     *
     * @return int - 0/1.  0=fail 1=success
     */
    public function deleteEvents($path = "", $params = array()) {

        $return_value = false;
        $return_value = $this->restHelper(BASE_URL . API_VERSION . "/vfs/" . $path . "/?tokenId=" . $this->_tokenID, $params, "POST");
        
        return $return_value;
    }
    
    /*
     * Search events
     *
     * @param String - select
     * @param String - from
     * @param Array - event parameters
     *
     * @return Array - search results
     */
     function search($select = "", $from = "", $where = array()) {
        
        $return_value = null;
        
        $params = array();
        $params['select'] = $select;
        $params['from'] = $from;
        $params['where'] = $where;

        $result = $this->restHelper(BASE_URL . API_VERSION . "/search?tokenId=" . $this->_tokenID, $params, "POST");
        
        if ($result) {
            $return_value = $result;
        }
        
        return $return_value;

     }
     
/****************************************************************************/     
/****************************************************************************/     
     
    /*********************************
     **** PRIVATE helper function ****
     *********************************/
    private function restHelper($json_endpoint, $params = null, $verb = 'GET') {
        
        $return_value = null;
        
        $http_params = array(
            'http' => array(
                'method' => $verb,
                'ignore_errors' => false
        ));
        if ($params !== null) {
            if ( ($verb == 'POST') || ($verb == 'PUT') ) {
                
                $http_params['http']['content'] = json_encode($params);
                $http_params['http']['header'] = array("Content-Type: application/json");
                
            }//end if
        }//end if ($params !== null)
        
        $stream_context = stream_context_create($http_params);
        $file_pointer = fopen($json_endpoint, 'rb', false, $stream_context);

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
                
                $result = (array)json_decode($stream_contents);
                
                if ($result === null) {
                    error_log("Exception:  " . $stream_contents);
                } else {
                    $return_value = $result;
                }
            /*
             * In the case of posting data (recordEvent) the API will return a 0 
             * length response, in this scenario we're looking for the http 200 
             * header code to indicate the data was successfully received.
             */
            } else {
                
                if (stripos($stream_meta_data['wrapper_data'][0], "200") !== false) {
                    $return_value = true;
                } else {
                    $return_value = false;
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
                $return_value = false;
                
            } else {
                throw new Exception("$verb $json_endpoint failed");
            }
            
        }//end outer else
        
        return $return_value;
    }//end restHelper
}
?>
