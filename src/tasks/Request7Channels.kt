package tasks

import contributors.GitHubService
import contributors.RequestData
import contributors.User
import contributors.logRepos
import contributors.logUsers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.ConcurrentHashMap

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit,
) {
    coroutineScope {
        val responseRepos = service.getOrgRepos(req.org)
        logRepos(req, responseRepos)
        val repos = responseRepos.bodyList()
        val channel = Channel<List<User>>(Channel.UNLIMITED)
        repos.forEach {
            async {
                val responseUsers = service.getRepoContributors(req.org, it.name)
                logUsers(it, responseUsers)
                channel.send(responseUsers.bodyList())
            }
        }
        var allUsers = emptyList<User>()
        repeat(repos.size) {
            val users = channel.receive()
            allUsers = (allUsers + users).aggregate()
            updateResults(allUsers, it == repos.lastIndex)
        }
    }
}
