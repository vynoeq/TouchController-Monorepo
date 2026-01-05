#!/bin/bash

MODULES=(model-base model-loader model-bedrock model-assimp model-gltf model-pmd model-pmx model-vmd model-formats)
LOCAL_REPO="file://$HOME/.m2/repository"
SNAPSHOT_REPO="https://maven.fifthlight.top/snapshots"
RELEASE_REPO="https://maven.fifthlight.top/releases"

usage() {
    echo "Usage: $0 [-l] [-s] [-r]"
    echo "Options:"
    echo "  -l    Publish to local Maven repository"
    echo "  -s    Publish to snapshot Maven repository"
    echo "  -r    Publish to release Maven repository"
    echo "  -h    Show this help message"
    exit 1
}

if [ "$#" -eq 0 ]; then
    usage
fi

REPO_TYPE=""
while getopts "lsrh" opt; do
    case ${opt} in
        l)
            if [ -n "$REPO_TYPE" ]; then
                echo "Error: Only one repository type can be specified."
                usage
            fi
            REPO_TYPE="local"
            ;;
        s)
            if [ -n "$REPO_TYPE" ]; then
                echo "Error: Only one repository type can be specified."
                usage
            fi
            REPO_TYPE="snapshot"
            ;;
        r)
            if [ -n "$REPO_TYPE" ]; then
                echo "Error: Only one repository type can be specified."
                usage
            fi
            REPO_TYPE="release"
            ;;
        h)
            usage
            ;;
        *)
            usage
            ;;
    esac
done

case "$REPO_TYPE" in
    "local")
        TARGET_REPO="$LOCAL_REPO"
        ;;
    "snapshot")
        TARGET_REPO="$SNAPSHOT_REPO"
        ;;
    "release")
        TARGET_REPO="$RELEASE_REPO"
        ;;
    *)
        echo "Error: No repository type specified."
        usage
        ;;
esac

for module in "${MODULES[@]}"
do
    bazel run --define "maven_repo=$TARGET_REPO" //blazerod/model/$module:$module.publish || exit $?
done
