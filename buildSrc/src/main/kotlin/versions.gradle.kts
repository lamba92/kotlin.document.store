group = "io.github.lamba92"

val githubRef =
    System.getenv("GITHUB_EVENT_NAME")
        ?.takeIf { it == "release" }
        ?.let { System.getenv("GITHUB_REF") }
        ?.removePrefix("refs/tags/")
        ?.removePrefix("v")

version =
    when {
        githubRef != null -> githubRef
        else -> "2.0-SNAPSHOT"
    }