package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User>  {
    val responseRepos = service.getOrgRepos(req.org)
    logRepos(req, responseRepos)
    val repos = responseRepos.bodyList()
    val allUsers = mutableListOf<User>()
    repos.map {
        GlobalScope.async(Dispatchers.Default) {
            delay(3000)
            val responseUsers = service.getRepoContributors(req.org, it.name)
            logUsers(it, responseUsers)
            val users = responseUsers.bodyList()
            allUsers += users
        }
    }.awaitAll()
    return allUsers.aggregate()

}