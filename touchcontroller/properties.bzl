"Common properties in TouchController"
touchcontroller_version = "0.3.1-dev"
touchcontroller_state = "alpha"
touchcontroller_description = "Bring controlling style of bedrock version to Java version!"
touchcontroller_license = "LGPL-3.0-or-later"
touchcontroller_license_link = "https://www.gnu.org/licenses/lgpl-3.0.html"
touchcontroller_homepage = "https://github.com/TouchController/TouchController"
touchcontroller_source = "https://github.com/TouchController/TouchController"
touchcontroller_issue_tracker = "https://github.com/TouchController/TouchController/issues"
touchcontroller_authors = ["fifth_light", "bukeyu"]
touchcontroller_contributors = [
    "fengzhou0w0",
    "white_elephant",
    "APX201",
    "Handsoneprin",
    "StarmanMine142",
    "Xaerfly",
    "LanYun2022",
    "Lessebq",
]

touchcontroller_fabric_libraries = {
    "//combine/theme/blackstone:vanilla_lib": "combine-theme-blackstone:=",
    "@maven//:androidx_compose_runtime_runtime_saveable_desktop": "androidx_compose_runtime_runtime_saveable_desktop:1.10.2",
    "@maven//:androidx_savedstate_savedstate_desktop": "androidx_savedstate_savedstate_desktop:1.3.2",
    "@maven//:androidx_savedstate_savedstate_compose_desktop": "androidx_savedstate_savedstate_compose_desktop:1.3.2",
    "@maven//:androidx_lifecycle_lifecycle_common_jvm": "androidx_lifecycle_lifecycle_common_jvm:2.9.4",
    "@maven//:androidx_lifecycle_lifecycle_runtime_compose_desktop": "androidx_lifecycle_lifecycle_runtime_compose_desktop:2.9.4",
    "@maven//:org_jetbrains_kotlinx_kotlinx_collections_immutable_jvm": "org_jetbrains_kotlinx_kotlinx_collections_immutable_jvm:0.4.0",
    "@maven//:cafe_adriel_voyager_voyager_core_desktop": "cafe_adriel_voyager_voyager_core_desktop:1.1.0-beta03",
    "@maven//:cafe_adriel_voyager_voyager_navigator_desktop": "cafe_adriel_voyager_voyager_navigator_desktop:1.1.0-beta03",
    "@maven//:cafe_adriel_voyager_voyager_screenmodel_desktop": "cafe_adriel_voyager_voyager_screenmodel_desktop:1.1.0-beta03",
}
