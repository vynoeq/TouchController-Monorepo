"""Utils for converting Minecraft version to repository name"""

def version_to_repo_name(version, type):
    return "minecraft_%s_%s" % (version.replace(".", "_"), type)
