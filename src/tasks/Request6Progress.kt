package tasks

import contributors.GitHubService
import contributors.RequestData
import contributors.User
import contributors.logRepos
import contributors.logUsers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.ConcurrentHashMap

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit,
) = coroutineScope {
    val responseRepos = service.getOrgRepos(req.org)
    logRepos(req, responseRepos)
    val repos = responseRepos.bodyList()

    val allUsers = ConcurrentHashMap<String, Int>()
    repos.map {
        async {
            val responseUsers = service.getRepoContributors(req.org, it.name)
            logUsers(it, responseUsers)
            val users = responseUsers.bodyList()
            users.forEach {
                allUsers[it.login] = allUsers.getOrDefault(it.login, 0) + it.contributions
            }
            updateResults(allUsers.map { User(it.key, it.value) }.sortedByDescending { it.contributions }, false)
        }
    }.awaitAll()
    updateResults(allUsers.map { User(it.key, it.value) }.sortedByDescending { it.contributions }, true)
}
