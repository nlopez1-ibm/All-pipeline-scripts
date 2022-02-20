@groovy.transform.BaseScript com.ibm.dbb.groovy.ScriptLoader baseScript

/************************************************************************************
 * This script publishes the outputs generated from a Package.groovy to artifactory.
 *
 * NJL: Customized  for Demo Purposes  (March 2021) 
 ************************************************************************************/

def opts				= parseInput(args)
def String 	url 		= opts.u 
def String 	apiKey 		= opts.k 
def String 	repo 		= opts.r   
def String  remotePath 	= opts.p
def tarFile 			= new File(opts.f)

//Call the ArtifactoryHelpers to publish the tar file
def artifactoryHelpers = loadScript(new File("ArtifactoryCurlHelpers.groovy"))
def artifactoryPullableURL = artifactoryHelpers.publish(url, repo, apiKey, remotePath, tarFile)
println " $tarFile published to Artifactory Repo $repo/$remotePath"

def parseInput(String[] cliArgs){
	def cli = new CliBuilder(usage: "package.groovy [options]")
	cli.u(longOpt:'url', args:1,required:true, argName:'url','The artifactory root url')
	cli.k(longOpt:'apikey', args:1, required:true, argName:'apikey','The artifactory apikey')
	cli.r(longOpt:'repo', args:1, required:true, argName:'repo','The artifactory repo')
	cli.f(longOpt:'tarFile', args:1, required:true, argName:'file','The DBB TarFile package')
	cli.p(longOpt:'remotePath', args:1, required:true, argName:'Path','The artifactory path')
	 
	cli.h(longOpt:'help', 'Prints this message')
	def opts = cli.parse(cliArgs)

	if (opts.h) { 
		cli.usage()
		System.exit(0)
	}	 
	return opts
}