package tasks

import contributors.GitHubService
import contributors.RequestData
import contributors.User
import contributors.logRepos
import contributors.logUsers

suspend fun loadContributorsSuspend(service: GitHubService, req: RequestData): List<User> {
    val responseRepos = service.getOrgRepos(req.org)
    logRepos(req, responseRepos)
    val repos = responseRepos.bodyList()
    val allUsers = mutableListOf<User>()
    repos.forEach {
        val responseUsers = service.getRepoContributors(req.org, it.name)
        logUsers(it, responseUsers)
        val users = responseUsers.bodyList()
        allUsers += users
    }
    return allUsers.aggregate()
}