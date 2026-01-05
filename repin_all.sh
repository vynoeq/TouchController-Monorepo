#!/bin/bash

set -e

REPIN=1 bazel run @maven//:pin
REPIN=1 bazel run @maven_fabric_1_14_4//:pin
REPIN=1 bazel run @maven_fabric_1_16_5//:pin
REPIN=1 bazel run @maven_fabric_1_17_1//:pin
REPIN=1 bazel run @maven_fabric_1_18_1//:pin
REPIN=1 bazel run @maven_fabric_1_18_2//:pin
REPIN=1 bazel run @maven_fabric_1_19_2//:pin
REPIN=1 bazel run @maven_fabric_1_20_1//:pin
REPIN=1 bazel run @maven_fabric_1_20_4//:pin
REPIN=1 bazel run @maven_fabric_1_20_6//:pin
REPIN=1 bazel run @maven_fabric_1_21//:pin
REPIN=1 bazel run @maven_fabric_1_21_1//:pin
REPIN=1 bazel run @maven_fabric_1_21_3//:pin
REPIN=1 bazel run @maven_fabric_1_21_4//:pin
REPIN=1 bazel run @maven_fabric_1_21_5//:pin
REPIN=1 bazel run @maven_fabric_1_21_6//:pin
REPIN=1 bazel run @maven_fabric_1_21_7//:pin
REPIN=1 bazel run @maven_fabric_1_21_8//:pin
REPIN=1 bazel run @maven_fabric_1_21_9//:pin
REPIN=1 bazel run @maven_fabric_1_21_10//:pin
REPIN=1 bazel run @maven_fabric_1_21_11//:pin
bazel run @modrinth_pin//:pin
