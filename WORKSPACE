workspace(name="renetty")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

RULES_JVM_EXTERNAL_TAG = "3.2"
RULES_JVM_EXTERNAL_SHA = "82262ff4223c5fda6fb7ff8bd63db8131b51b413d26eb49e3131037e79e324af"

http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")

JETTY_VERSION = "9.4.29.v20200521"

maven_install(
    # If you update an artifact below run the following command to resolve the updated dependencies.
    # bazel run @unpinned_maven//:pin
    artifacts = [
        maven.artifact("io.projectreactor.netty", "reactor-netty", "0.9.8.RELEASE"),
        maven.artifact("org.eclipse.jetty", "jetty-servlet", JETTY_VERSION),
        maven.artifact("org.eclipse.jetty.http2", "http2-client", JETTY_VERSION),
        maven.artifact("org.eclipse.jetty.http2", "http2-server", JETTY_VERSION),
        maven.artifact("org.msgpack", "msgpack-core", "0.8.20"),
    ],
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
    maven_install_json = "@renetty//:maven_install.json",
)

load("@maven//:defs.bzl", "pinned_maven_install")
pinned_maven_install()
