package tasks

import contributors.GitHubService
import contributors.RequestData
import contributors.User
import contributors.logRepos
import contributors.logUsers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    val responseRepos = service.getOrgRepos(req.org)
    logRepos(req, responseRepos)
    val repos = responseRepos.bodyList()
    val allUsers = mutableListOf<User>()
    repos.map {
        async(Dispatchers.Default) {
            delay(3000)
            val responseUsers = service.getRepoContributors(req.org, it.name)
            logUsers(it, responseUsers)
            val users = responseUsers.bodyList()
            allUsers += users
        }
    }.awaitAll()
    return@coroutineScope allUsers.aggregate()

}