@groovy.transform.BaseScript com.ibm.dbb.groovy.ScriptLoader baseScript
import java.security.MessageDigest
import groovy.json.JsonSlurper

/************************************************************************************
 *
 * Provide helpers to interact with Artifactory Repository through REST service.
 * This class provides 3 helpers:
 * 1.  Publishing a file on zFS to an artifactory repository.
 * 2.  Downloading a remote file from artifactory repository to zFS.
 * 3.  Retrieve the latest uploaded artifact from a remote path in artifactory.
 ************************************************************************************/


/**
 * Publish a file from HFS to an artifactory repository at location specified in remoteFilePath
 */
def publish(serverUrl, repo, apiKey, remoteFilePath, File localFile)
{
    //Validate to make sure all required fields are specified
    assert serverUrl != null, "Need to specify a valid URL to artifactory server"
    assert repo != null, "Need to specify a valid artifactory repository"
    assert apiKey != null, "Need to specify a valid API key to authenticate with $repo"
    assert remoteFilePath != null, "Need to specify the path of the source file"
    assert localFile != null && localFile.exists(), "Target local file must exist"
    
    //Artifactory URL must end with '/'
    def url = serverUrl.endsWith('/') ? serverUrl : serverUrl + '/'

    //Create SHA1 and MD5 checksums to be published along with the file
    def sha1 = getChecksum(localFile)
    def md5 = getChecksum(localFile, "MD5")
       
    def filePath = "$repo/$remoteFilePath" 
	
	// execute upload command
	def cmd = [ "curl",
		"--silent",
        "--insecure",
		"-H",
	    "X-JFrog-Art-Api:$apiKey",
		"-H",
		"X-Checksum-MD5:$md5",
		"-H",
		"X-Checksum-Sha1:$sha1",
		"-T",
		localFile,
		"$serverUrl/$repo/$remoteFilePath"
    	]

	def cmdStr = "";
	cmd.each{ 
		if ( it.toString().startsWith("X-JFrog-Art-Api") )
			cmdStr = cmdStr + "X-JFrog-Art-Api:XXXX "
		else	
			cmdStr = cmdStr + it + " "
	}
	
	println cmdStr
		
	def out = new StringBuffer()
	def err = new StringBuffer()
	
	def process = cmd.execute()
	process.waitForProcessOutput(out, err)
		
	def rc = process.exitValue();
	
	if(rc!=0){
		print "ERR:\n" + err
		print "OUT:\n" + out
		System.exit(rc)
	}
	
	def jsonSlurper = new JsonSlurper()
	def parsedReport = jsonSlurper.parseText(out.toString())
	
	if ( parsedReport.errors != null ) {
		print "ERR:\n" + err
		print "OUT:\n" + out
		System.exit(1)
	}
	
	assert parsedReport.uri != null: "Artifactory did not return a URI"

    println "Successfully publish file $localFile to $filePath"
	
	return parsedReport.uri
}

/**
 * Download a remote file from an artifactory server to HFS
 */
def download(serverUrl, repo, apiKey, remoteFilePath, File localFile)
{
//    //Validate to make sure all required fields are specified
//    assert serverUrl != null, "Need to specify a valid URL to artifactory server"
//    assert repo != null, "Need to specify a valid artifactory repository"
//    assert apiKey != null, "Need to specify a valid API key to authenticate with $repo"
//    assert remoteFilePath != null, "Need to specify the path of the source file"
//    assert localFile != null, "Need to specify the target local file"
//    
//    def url = serverUrl.endsWith('/') ? serverUrl : serverUrl + '/'
//    def filePath = "$repo/$remoteFilePath"
//    
//    def restClient = new RESTClient(url)
//    restClient.encoder.'application/zip' = this.&encodeZipFile
//    def response = restClient.get(path: filePath, headers: ['X-JFrog-Art-Api' : apiKey, responseContentType: 'application/zip'])
//    
//    assert response.isSuccess(), "Failed to retrieve file $filePath"
//    
//    //Write the contents to the local file
//    def inputStream = response.data    
//    !localFile.exists() ?: localFile.delete()
//    localFile << inputStream.bytes
//    
//    //Retrieve the remote file checksums to do comparison to ensure
//    //the transfer is complete
//    def expectedSha1 = response.headers['X-Checksum-Sha1'].value
//    def expectedMd5 = response.headers['X-Checksum-Md5'].value    
//    def actualSha1 = getChecksum(localFile)
//    def actualMd5 = getChecksum(localFile, "MD5")    
//    assert actualSha1 == expectedSha1 && actualMd5 == expectedMd5, "The downloaded file $localFile does not have the right checksum"
//    
//    println "Successfully download $filePath to $localFile"
}

/**
 * Return the latest published artifact in a location
 */
def getLatestPublished(serverUrl, repo, apiKey, remotePath)
{
//    //Validate to make sure all required fields are specified
//    assert serverUrl != null, "Need to specify a valid URL to artifactory server"
//    assert repo != null, "Need to specify a valid artifactory repository"
//    assert apiKey != null, "Need to specify a valid API key to authenticate with $repo"
//    assert remotePath != null, "Need to specify the path where the files were published"
//    
//    def url = serverUrl.endsWith('/') ? serverUrl : serverUrl + '/'
//    def client = new RESTClient(url)
//    try
//    {
//        client.get(path: "api/storage/$repo/$remotePath", queryString: 'lastModified', headers: ['X-JFrog-Art-Api' : apiKey], contentType: 'application/json') { response, json ->
//            def uri = json.uri
//            uri = uri.substring(uri.lastIndexOf('/')+1)
//            return uri;
//        }
//    }
//    catch (HttpResponseException e)
//    {
//        println e
//        return null
//    }
}


def getChecksum(File file, type = 'SHA-1')
{
    assert file.exists(), "$file does not exist"
    
    def digest = MessageDigest.getInstance(type)
    digest.update(file.bytes)
    return new BigInteger(1,digest.digest()).toString(16)
}

def static encodeZipFile(Object data) throws UnsupportedEncodingException
{
//    def entity = new FileEntity((File) data, 'application/zip');
//    entity.setContentType('application/zip');
//    return entity
}
