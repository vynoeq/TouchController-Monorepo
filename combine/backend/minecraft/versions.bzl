load("//rule:version_group.bzl", "version_entry")

versions = {
    "1.21.8": version_entry(
        deps = [
            "//combine/backend/minecraft/1.21.8/game:remapped_client_access_widened_named",
            "//game/1.21.8:game_client_libraries",
        ],
    ),
    "1.21.11": version_entry(
        deps = [
            "//combine/backend/minecraft/1.21.11/game:remapped_client_access_widened_named",
            "//game/1.21.11:game_client_libraries",
        ],
    ),
    "26.1": version_entry(
        deps = [
            "//combine/backend/minecraft/26.1/game:remapped_client_access_widened_named",
            "//game/26.1:game_client_libraries",
        ],
    ),
}
